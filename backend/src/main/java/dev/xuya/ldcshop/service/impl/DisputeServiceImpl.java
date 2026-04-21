package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xuya.ldcshop.common.util.I18nUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.common.util.EntityUtil;
import dev.xuya.ldcshop.entity.Dispute;
import dev.xuya.ldcshop.entity.Order;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.DisputeMapper;
import dev.xuya.ldcshop.mapper.OrderMapper;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.params.DisputeCreateParams;
import dev.xuya.ldcshop.params.DisputeHandleParams;
import dev.xuya.ldcshop.results.DisputeResult;
import dev.xuya.ldcshop.service.DisputeService;
import dev.xuya.ldcshop.service.OrderService;
import dev.xuya.ldcshop.common.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 争议服务实现 / Dispute Service Implementation
 *
 * @author xuya
 */
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeMapper disputeMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final OrderService orderService;

    /** 争议状态名称 / Dispute status name keys */
    private static final Map<Integer, String> STATUS_KEYS = Map.of(
            0, "dispute.status_pending",
            1, "dispute.status_accepted",
            2, "dispute.status_rejected",
            3, "dispute.status_platform"
    );

    /**
     * 创建争议 / Create a dispute
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDispute(DisputeCreateParams params) {
        Long userId = UserContextUtil.getCurrentUserId();

        // 验证订单 / Verify order
        Order order = EntityUtil.requireNonNull(orderMapper.selectById(params.getOrderId()), ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getPaymentStatus() != Order.PAYMENT_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 检查是否已有争议 / Check existing dispute
        long count = disputeMapper.selectCount(
                new LambdaQueryWrapper<Dispute>()
                        .eq(Dispute::getOrderId, params.getOrderId())
                        .eq(Dispute::getStatus, Dispute.STATUS_PENDING));
        if (count > 0) {
            throw new BusinessException(ResultCode.DISPUTE_ALREADY_EXISTS);
        }

        Dispute dispute = new Dispute();
        dispute.setOrderId(params.getOrderId());
        dispute.setUserId(userId);
        dispute.setReason(params.getReason());
        if (params.getEvidence() != null) {
            dispute.setEvidence(cn.hutool.json.JSONUtil.toJsonStr(params.getEvidence()));
        }
        dispute.setStatus(Dispute.STATUS_PENDING);

        disputeMapper.insert(dispute);
        AuditLog.log("createDispute", "disputeId=" + dispute.getId() + ", orderId=" + params.getOrderId());
        return dispute.getId();
    }

    /**
     * 获取争议详情 / Get dispute detail
     */
    @Override
    public DisputeResult getDisputeDetail(Long id) {
        Dispute dispute = EntityUtil.requireNonNull(disputeMapper.selectById(id), ResultCode.DISPUTE_NOT_FOUND);
        return convertToResult(dispute);
    }

    /**
     * 用户争议分页查询 / User-side dispute pagination
     */
    @Override
    public IPage<DisputeResult> pageUserDisputes(int page, int size, Integer status) {
        Long userId = UserContextUtil.getCurrentUserId();
        LambdaQueryWrapper<Dispute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dispute::getUserId, userId);
        if (status != null) {
            wrapper.eq(Dispute::getStatus, status);
        }
        wrapper.orderByDesc(Dispute::getCreatedAt);

        IPage<Dispute> disputePage = disputeMapper.selectPage(new Page<>(page, size), wrapper);
        return convertPage(disputePage);
    }

    /**
     * 管理端争议分页查询 / Admin-side dispute pagination
     */
    @Override
    public IPage<DisputeResult> pageAdminDisputes(int page, int size, Integer status) {
        LambdaQueryWrapper<Dispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Dispute::getStatus, status);
        }
        wrapper.orderByAsc(Dispute::getStatus).orderByDesc(Dispute::getCreatedAt);

        IPage<Dispute> disputePage = disputeMapper.selectPage(new Page<>(page, size), wrapper);
        return convertPage(disputePage);
    }

    /**
     * 处理争议 / Handle a dispute
     * 接受争议时自动触发全额退款 / Accepting a dispute automatically triggers a full refund
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDispute(Long id, DisputeHandleParams params) {
        Dispute dispute = EntityUtil.requireNonNull(disputeMapper.selectById(id), ResultCode.DISPUTE_NOT_FOUND);
        if (dispute.getStatus() != Dispute.STATUS_PENDING) {
            throw new BusinessException(ResultCode.DISPUTE_STATUS_ERROR);
        }

        dispute.setStatus(params.getStatus());
        dispute.setAdminNote(params.getAdminNote());
        dispute.setHandledBy(UserContextUtil.getCurrentUserId());
        dispute.setHandledAt(LocalDateTime.now());

        disputeMapper.updateById(dispute);
        AuditLog.log("handleDispute", "disputeId=" + id + ", status=" + params.getStatus());

        // 如果接受，触发全额退款（LDC API + 卡密释放 + 库存恢复）/ If accepted, trigger full refund (LDC API + card release + stock restore)
        if (params.getStatus() == Dispute.STATUS_ACCEPTED) {
            Order order = orderMapper.selectById(dispute.getOrderId());
            if (order != null && order.getPaymentStatus() == Order.PAYMENT_PAID) {
                orderService.refundOrder(order.getId());
            }
        }
    }

    /**
     * 转换争议分页 / Convert dispute page
     */
    private IPage<DisputeResult> convertPage(IPage<Dispute> page) {
        List<Dispute> disputes = page.getRecords();
        if (disputes.isEmpty()) {
            return page.convert(d -> null);
        }

        Set<Long> orderIds = disputes.stream().map(Dispute::getOrderId).collect(Collectors.toSet());
        Set<Long> userIds = disputes.stream().map(Dispute::getUserId).collect(Collectors.toSet());
        disputes.stream().map(Dispute::getHandledBy).filter(Objects::nonNull).forEach(userIds::add);

        Map<Long, Order> orderMap = orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, o -> o));
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return page.convert(dispute -> toResult(dispute, orderMap, userMap));
    }

    /**
     * 转换单个争议为结果对象 / Convert single dispute to result object
     */
    private DisputeResult convertToResult(Dispute dispute) {
        Map<Long, Order> orderMap = orderMapper.selectBatchIds(Set.of(dispute.getOrderId())).stream()
                .collect(Collectors.toMap(Order::getId, o -> o));
        Set<Long> userIds = new HashSet<>();
        userIds.add(dispute.getUserId());
        if (dispute.getHandledBy() != null) userIds.add(dispute.getHandledBy());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return toResult(dispute, orderMap, userMap);
    }

    /**
     * 争议转结果的统一转换方法 / Unified dispute-to-result conversion
     */
    private DisputeResult toResult(Dispute dispute, Map<Long, Order> orderMap, Map<Long, User> userMap) {
        DisputeResult result = BeanUtil.copyProperties(dispute, DisputeResult.class);
        result.setStatusName(I18nUtil.get(STATUS_KEYS.getOrDefault(dispute.getStatus(), "dispute.status_unknown")));

        Order order = orderMap.get(dispute.getOrderId());
        if (order != null) {
            result.setOrderNo(order.getOrderNo());
            result.setProductName(order.getProductName());
        }

        User user = userMap.get(dispute.getUserId());
        if (user != null) {
            result.setUsername(user.getUsername());
        }

        if (dispute.getHandledBy() != null) {
            User handler = userMap.get(dispute.getHandledBy());
            if (handler != null) {
                result.setHandlerName(handler.getUsername());
            }
        }

        return result;
    }
}
