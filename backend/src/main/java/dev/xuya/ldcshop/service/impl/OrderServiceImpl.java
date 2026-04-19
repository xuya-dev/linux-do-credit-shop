package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.config.LdcShopProperties;
import dev.xuya.ldcshop.entity.*;
import dev.xuya.ldcshop.mapper.*;
import dev.xuya.ldcshop.params.OrderCreateParams;
import dev.xuya.ldcshop.params.OrderDeliveryParams;
import dev.xuya.ldcshop.results.OrderDetailResult;
import dev.xuya.ldcshop.results.PaymentSubmitResult;
import dev.xuya.ldcshop.service.OrderService;
import dev.xuya.ldcshop.service.ProductCardService;
import dev.xuya.ldcshop.util.Ed25519Util;
import dev.xuya.ldcshop.util.OrderUtil;
import dev.xuya.ldcshop.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单服务实现 / Order Service Implementation
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrderCardMapper orderCardMapper;
    private final ProductCardMapper productCardMapper;
    private final ProductCardService productCardService;
    private final LdcShopProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResult createOrder(OrderCreateParams params) {
        Long userId = UserContextUtil.getCurrentUserId();

        // 查询商品 / Query product
        Product product = productMapper.selectById(params.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (product.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
        }

        // 检查库存 / Check stock
        if (product.getProductType() == 1) {
            // 虚拟商品检查卡密库存 / Virtual product check card stock
            int availableCount = productCardService.getAvailableCount(product.getId());
            if (availableCount < params.getQuantity()) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
            }
        } else {
            if (product.getStock() < params.getQuantity()) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
            }
        }

        // 创建订单 / Create order
        Order order = new Order();
        order.setOrderNo(OrderUtil.generateOrderNo());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductType(product.getProductType());
        order.setQuantity(params.getQuantity());
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(params.getQuantity())));
        order.setPaymentStatus(Order.PAYMENT_PENDING);
        order.setDeliveryStatus(Order.DELIVERY_PENDING);
        order.setContactInfo(params.getContactInfo());
        order.setRemark(params.getRemark());
        order.setDeliveryInfo(params.getDeliveryInfo());
        order.setLdcOutTradeNo(OrderUtil.generateOutTradeNo());

        orderMapper.insert(order);

        // 扣减实物商品库存 / Deduct physical product stock
        if (product.getProductType() == 2) {
            product.setStock(product.getStock() - params.getQuantity());
            productMapper.updateById(product);
        }

        return convertToDetailResult(order, false);
    }

    @Override
    public PaymentSubmitResult initiatePayment(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getPaymentStatus() != Order.PAYMENT_PENDING) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }

        // 验证订单归属 / Verify order ownership
        Long currentUserId = UserContextUtil.getCurrentUserId();
        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        LdcShopProperties.Payment payment = properties.getPayment();

        // 构建支付参数 / Build payment params
        Map<String, Object> payParams = new LinkedHashMap<>();
        payParams.put("client_id", payment.getClientId());
        payParams.put("type", "ldcpay");
        payParams.put("out_trade_no", order.getLdcOutTradeNo());
        payParams.put("money", order.getTotalAmount().setScale(2).toPlainString());
        payParams.put("order_name", order.getProductName());
        payParams.put("notify_url", payment.getNotifyUrl());
        payParams.put("return_url", payment.getReturnUrl() + "?orderNo=" + order.getOrderNo());

        // 生成签名 / Generate signature
        String signString = Ed25519Util.buildSignString(payParams, payment.getClientSecret());
        payParams.put("sign", Ed25519Util.sign(payment.getClientSecret(), signString));

        // 提交支付请求 / Submit payment request
        try {
            String response = HttpUtil.post(payment.getGatewayUrl() + "/pay/submit.php", payParams);
            log.info("支付请求已发送 / Payment request sent: orderNo={}", order.getOrderNo());

            PaymentSubmitResult result = new PaymentSubmitResult();
            result.setOrderNo(order.getOrderNo());
            // LDC 会返回跳转地址 / LDC returns redirect URL
            result.setPayUrl(payment.getGatewayUrl() + "/paying?order_no=" + order.getLdcOutTradeNo());
            return result;
        } catch (Exception e) {
            log.error("支付请求失败 / Payment request failed", e);
            throw new BusinessException(ResultCode.ORDER_PAY_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePaymentNotify(Map<String, String> params) {
        LdcShopProperties.Payment payment = properties.getPayment();

        // 验证签名 / Verify signature
        String sign = params.get("sign");
        String signString = Ed25519Util.buildSignString(params, payment.getClientSecret());
        // 注意：实际生产中需要使用 LDC 平台公钥验签 / Note: In production, use LDC platform public key
        log.info("收到支付回调 / Payment callback received: params={}", params);

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");

        if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
            return "ignored";
        }

        // 查找订单 / Find order
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getLdcOutTradeNo, outTradeNo));
        if (order == null) {
            log.warn("回调订单不存在 / Callback order not found: outTradeNo={}", outTradeNo);
            return "ignored";
        }

        if (order.getPaymentStatus() != Order.PAYMENT_PENDING) {
            return "success";
        }

        // 更新订单状态 / Update order status
        order.setPaymentStatus(Order.PAYMENT_PAID);
        order.setLdcTradeNo(tradeNo);
        order.setPaidAt(LocalDateTime.now());

        // 虚拟商品自动发卡 / Virtual product auto delivery
        if (order.getProductType() == 1) {
            productCardService.allocateCards(order.getId(), order.getProductId(), order.getQuantity());
            order.setDeliveryStatus(Order.DELIVERY_COMPLETED);
            order.setDeliveredAt(LocalDateTime.now());
        } else {
            order.setDeliveryStatus(Order.DELIVERY_PENDING);
        }

        orderMapper.updateById(order);

        // 更新商品销量 / Update product sold count
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setSoldCount(product.getSoldCount() + order.getQuantity());
            if (product.getProductType() == 2) {
                product.setStock(Math.max(0, product.getStock() - order.getQuantity()));
            }
            productMapper.updateById(product);
        }

        log.info("订单支付成功 / Order payment success: orderNo={}", order.getOrderNo());
        return "success";
    }

    @Override
    public OrderDetailResult getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        boolean showCards = order.getUserId().equals(UserContextUtil.getCurrentUserId())
                || UserContextUtil.isAdmin();
        return convertToDetailResult(order, showCards);
    }

    @Override
    public OrderDetailResult getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        boolean showCards = order.getUserId().equals(UserContextUtil.getCurrentUserId())
                || UserContextUtil.isAdmin();
        return convertToDetailResult(order, showCards);
    }

    @Override
    public IPage<OrderDetailResult> pageUserOrders(int page, int size, Integer paymentStatus, Integer deliveryStatus) {
        Long userId = UserContextUtil.getCurrentUserId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (paymentStatus != null) {
            wrapper.eq(Order::getPaymentStatus, paymentStatus);
        }
        if (deliveryStatus != null) {
            wrapper.eq(Order::getDeliveryStatus, deliveryStatus);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return orderPage.convert(o -> convertToDetailResult(o, true));
    }

    @Override
    public IPage<OrderDetailResult> pageAdminOrders(int page, int size, Integer paymentStatus,
                                                      Integer deliveryStatus, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (paymentStatus != null) {
            wrapper.eq(Order::getPaymentStatus, paymentStatus);
        }
        if (deliveryStatus != null) {
            wrapper.eq(Order::getDeliveryStatus, deliveryStatus);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or().like(Order::getProductName, keyword));
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return orderPage.convert(o -> convertToDetailResult(o, true));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(Long orderId, OrderDeliveryParams params) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getPaymentStatus() != Order.PAYMENT_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setDeliveryStatus(Order.DELIVERY_DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        if (params != null) {
            order.setDeliveryInfo(params.getDeliveryInfo());
            order.setAdminRemark(params.getAdminRemark());
        }
        orderMapper.updateById(order);
        log.info("订单已发货 / Order delivered: orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getPaymentStatus() != Order.PAYMENT_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setPaymentStatus(Order.PAYMENT_REFUNDED);
        orderMapper.updateById(order);
        log.info("订单已退款 / Order refunded: orderId={}", orderId);
    }

    /**
     * 转换为订单详情结果 / Convert to order detail result
     */
    private OrderDetailResult convertToDetailResult(Order order, boolean showCards) {
        OrderDetailResult result = BeanUtil.copyProperties(order, OrderDetailResult.class);

        // 查询买家信息 / Query buyer info
        User buyer = userMapper.selectById(order.getUserId());
        if (buyer != null) {
            result.setBuyerName(buyer.getUsername());
        }

        // 查询商品封面 / Query product cover
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            result.setProductCoverImage(product.getCoverImage());
        }

        // 虚拟商品展示卡密内容 / Show card content for virtual products
        if (showCards && order.getPaymentStatus() == Order.PAYMENT_PAID && order.getProductType() == 1) {
            List<OrderCard> orderCards = orderCardMapper.selectList(
                    new LambdaQueryWrapper<OrderCard>().eq(OrderCard::getOrderId, order.getId()));
            List<String> cardContents = orderCards.stream()
                    .map(oc -> {
                        ProductCard card = productCardMapper.selectById(oc.getCardId());
                        return card != null ? card.getCardContent() : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            result.setCardContents(cardContents);
        }

        return result;
    }
}
