package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
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
import dev.xuya.ldcshop.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
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

    /** 争议状态名称映射 / Dispute status name mapping */
    private static final Map<Integer, String> STATUS_NAMES = Map.of(
            0, "待处理 / Pending",
            1, "已同意(退款) / Accepted",
            2, "已拒绝 / Rejected",
            3, "平台介入 / Platform Intervention"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDispute(DisputeCreateParams params) {
        Long userId = UserContextUtil.getCurrentUserId();

        // 验证订单 / Verify order
        Order order = orderMapper.selectById(params.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
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
        return dispute.getId();
    }

    @Override
    public DisputeResult getDisputeDetail(Long id) {
        Dispute dispute = disputeMapper.selectById(id);
        if (dispute == null) {
            throw new BusinessException(ResultCode.DISPUTE_NOT_FOUND);
        }
        return convertToResult(dispute);
    }

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
        return disputePage.convert(this::convertToResult);
    }

    @Override
    public IPage<DisputeResult> pageAdminDisputes(int page, int size, Integer status) {
        LambdaQueryWrapper<Dispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Dispute::getStatus, status);
        }
        wrapper.orderByAsc(Dispute::getStatus).orderByDesc(Dispute::getCreatedAt);

        IPage<Dispute> disputePage = disputeMapper.selectPage(new Page<>(page, size), wrapper);
        return disputePage.convert(this::convertToResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDispute(Long id, DisputeHandleParams params) {
        Dispute dispute = disputeMapper.selectById(id);
        if (dispute == null) {
            throw new BusinessException(ResultCode.DISPUTE_NOT_FOUND);
        }
        if (dispute.getStatus() != Dispute.STATUS_PENDING) {
            throw new BusinessException(ResultCode.DISPUTE_STATUS_ERROR);
        }

        dispute.setStatus(params.getStatus());
        dispute.setAdminNote(params.getAdminNote());
        dispute.setHandledBy(UserContextUtil.getCurrentUserId());
        dispute.setHandledAt(LocalDateTime.now());

        disputeMapper.updateById(dispute);

        // 如果同意退款，更新订单状态 / If accepted, update order status
        if (params.getStatus() == Dispute.STATUS_ACCEPTED) {
            Order order = orderMapper.selectById(dispute.getOrderId());
            if (order != null) {
                order.setPaymentStatus(Order.PAYMENT_REFUNDED);
                orderMapper.updateById(order);
            }
        }
    }

    private DisputeResult convertToResult(Dispute dispute) {
        DisputeResult result = BeanUtil.copyProperties(dispute, DisputeResult.class);
        result.setStatusName(STATUS_NAMES.getOrDefault(dispute.getStatus(), "未知 / Unknown"));

        // 查询关联订单 / Query related order
        Order order = orderMapper.selectById(dispute.getOrderId());
        if (order != null) {
            result.setOrderNo(order.getOrderNo());
            result.setProductName(order.getProductName());
        }

        // 查询发起人 / Query initiator
        User user = userMapper.selectById(dispute.getUserId());
        if (user != null) {
            result.setUsername(user.getUsername());
        }

        // 查询处理人 / Query handler
        if (dispute.getHandledBy() != null) {
            User handler = userMapper.selectById(dispute.getHandledBy());
            if (handler != null) {
                result.setHandlerName(handler.getUsername());
            }
        }

        return result;
    }
}
