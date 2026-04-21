package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.common.util.EntityUtil;
import dev.xuya.ldcshop.entity.*;
import dev.xuya.ldcshop.mapper.*;
import dev.xuya.ldcshop.params.OrderCreateParams;
import dev.xuya.ldcshop.params.OrderDeliveryParams;
import dev.xuya.ldcshop.results.OrderDetailResult;
import dev.xuya.ldcshop.results.PaymentSubmitResult;
import dev.xuya.ldcshop.service.OrderService;
import dev.xuya.ldcshop.service.ProductCardService;
import dev.xuya.ldcshop.service.ShopSettingService;
import dev.xuya.ldcshop.common.util.CryptoUtil;
import dev.xuya.ldcshop.common.util.Ed25519Util;
import dev.xuya.ldcshop.common.util.OrderUtil;
import dev.xuya.ldcshop.common.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ShopSettingService shopSettingService;
    private final CryptoUtil cryptoUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResult createOrder(OrderCreateParams params) {
        Long userId = UserContextUtil.getCurrentUserId();

        Product product = EntityUtil.requireNonNull(productMapper.selectById(params.getProductId()), ResultCode.PRODUCT_NOT_FOUND);
        if (product.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
        }

        if (product.getProductType() == 1) {
            int availableCount = productCardService.getAvailableCount(product.getId());
            if (availableCount < params.getQuantity()) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
            }
        } else {
            int rows = productMapper.deductStock(product.getId(), params.getQuantity());
            if (rows == 0) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
            }
        }

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

        return convertToDetailResult(order, false);
    }

    @Override
    public PaymentSubmitResult initiatePayment(Long orderId) {
        Order order = EntityUtil.requireNonNull(orderMapper.selectById(orderId), ResultCode.ORDER_NOT_FOUND);
        if (order.getPaymentStatus() != Order.PAYMENT_PENDING) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }

        Long currentUserId = UserContextUtil.getCurrentUserId();
        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 从数据库读取支付配置 / Load payment config from DB
        String clientId = shopSettingService.getSettingOrDefault("ldc_payment_client_id", "");
        String clientSecret = shopSettingService.getSettingOrDefault("ldc_payment_client_secret", "");
        String privateKey = shopSettingService.getSettingOrDefault("ldc_payment_private_key", "");
        String gatewayUrl = shopSettingService.getSettingOrDefault("ldc_payment_gateway_url", "https://credit.linux.do/epay");
        String notifyUrl = shopSettingService.getSettingOrDefault("ldc_payment_notify_url", "");
        String returnUrl = shopSettingService.getSettingOrDefault("ldc_payment_return_url", "");

        if (StrUtil.isBlank(clientId)) {
            log.error("LDC payment client_id not configured");
            throw new BusinessException(ResultCode.PAY_CONFIG_ERROR);
        }
        if (StrUtil.isBlank(clientSecret)) {
            log.error("LDC payment client_secret not configured");
            throw new BusinessException(ResultCode.PAY_CONFIG_ERROR);
        }
        if (StrUtil.isBlank(privateKey)) {
            log.error("LDC payment private_key not configured");
            throw new BusinessException(ResultCode.PAY_CONFIG_ERROR);
        }

        Map<String, Object> payParams = new LinkedHashMap<>();
        payParams.put("client_id", clientId);
        payParams.put("type", "ldcpay");
        payParams.put("out_trade_no", order.getLdcOutTradeNo());
        payParams.put("money", order.getTotalAmount().setScale(2).toPlainString());
        payParams.put("order_name", order.getProductName());
        if (StrUtil.isNotBlank(notifyUrl)) {
            payParams.put("notify_url", notifyUrl);
        }
        if (StrUtil.isNotBlank(returnUrl)) {
            payParams.put("return_url", returnUrl + "?orderNo=" + order.getOrderNo());
        }

        String signString = Ed25519Util.buildSignString(payParams, clientSecret);
        log.info("LDC payment sign string: {}", signString);
        payParams.put("sign", Ed25519Util.sign(privateKey, signString));

        try {
            HttpResponse httpResponse = HttpRequest.post(gatewayUrl + "/pay/submit.php")
                    .form(payParams)
                    .setFollowRedirects(false)
                    .execute();

            int status = httpResponse.getStatus();
            String body = httpResponse.body();
            log.info("Payment response: status={}, body={}", status, body);

            PaymentSubmitResult result = new PaymentSubmitResult();
            result.setOrderNo(order.getOrderNo());

            if (status == 302 || status == 301) {
                result.setPayUrl(httpResponse.header("Location"));
            } else {
                log.error("LDC payment error: status={}, body={}", status, body);
                throw new BusinessException(ResultCode.ORDER_PAY_FAIL);
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Payment request failed", e);
            throw new BusinessException(ResultCode.ORDER_PAY_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePaymentNotify(Map<String, String> params) {
        String clientSecret = shopSettingService.getSettingOrDefault("ldc_payment_client_secret", "");

        log.info("Payment callback received: outTradeNo={}", params.get("out_trade_no"));

        String publicKey = shopSettingService.getSettingOrDefault("ldc_payment_public_key", "");
        String sign = params.get("sign");
        String signString = Ed25519Util.buildSignString(params, clientSecret);

        if (StrUtil.isNotBlank(publicKey) && StrUtil.isNotBlank(sign)) {
            if (!Ed25519Util.verify(publicKey, signString, sign)) {
                log.warn("Payment callback signature verification failed: outTradeNo={}", params.get("out_trade_no"));
                return "fail";
            }
        } else {
            log.error("Payment callback rejected: public key or sign is empty. outTradeNo={}", params.get("out_trade_no"));
            return "fail";
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");

        if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) {
            return "ignored";
        }

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getLdcOutTradeNo, outTradeNo));
        if (order == null) {
            log.warn("Callback order not found: outTradeNo={}", outTradeNo);
            return "ignored";
        }

        int deliveryStatus = order.getProductType() == 1 ? Order.DELIVERY_COMPLETED : Order.DELIVERY_PENDING;
        LocalDateTime deliveredAt = order.getProductType() == 1 ? LocalDateTime.now() : null;

        int rows = orderMapper.atomicConfirmPayment(outTradeNo, tradeNo, deliveryStatus, deliveredAt);
        if (rows == 0) {
            log.info("Payment already processed: outTradeNo={}", outTradeNo);
            return "success";
        }

        if (order.getProductType() == 1) {
            productCardService.allocateCards(order.getId(), order.getProductId(), order.getQuantity());
        }

        productMapper.incrementSoldCount(order.getProductId(), order.getQuantity());

        log.info("Order payment success: orderNo={}", order.getOrderNo());
        return "success";
    }

    @Override
    public OrderDetailResult getOrderDetail(Long orderId) {
        Order order = EntityUtil.requireNonNull(orderMapper.selectById(orderId), ResultCode.ORDER_NOT_FOUND);
        boolean showCards = order.getUserId().equals(UserContextUtil.getCurrentUserId())
                || UserContextUtil.isAdmin();
        return convertToDetailResult(order, showCards);
    }

    @Override
    public OrderDetailResult getOrderByOrderNo(String orderNo) {
        Order order = EntityUtil.requireNonNull(orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)), ResultCode.ORDER_NOT_FOUND);
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
        Page<OrderDetailResult> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        resultPage.setRecords(batchConvertToDetailResults(orderPage.getRecords(), true));
        return resultPage;
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
        Page<OrderDetailResult> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        resultPage.setRecords(batchConvertToDetailResults(orderPage.getRecords(), true));
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(Long orderId, OrderDeliveryParams params) {
        Order order = EntityUtil.requireNonNull(orderMapper.selectById(orderId), ResultCode.ORDER_NOT_FOUND);
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
        AuditLog.log("deliverOrder", "orderId=" + orderId);
    }

    @Override
    public void refundOrder(Long orderId) {
        Order order = EntityUtil.requireNonNull(orderMapper.selectById(orderId), ResultCode.ORDER_NOT_FOUND);
        if (order.getPaymentStatus() != Order.PAYMENT_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        callLdcRefundApi(order);
        doRefundDbOperations(order);
    }

    private void callLdcRefundApi(Order order) {
        if (StrUtil.isBlank(order.getLdcTradeNo())) {
            return;
        }

        String clientId = shopSettingService.getSettingOrDefault("ldc_payment_client_id", "");
        String clientSecret = shopSettingService.getSettingOrDefault("ldc_payment_client_secret", "");
        String gatewayUrl = shopSettingService.getSettingOrDefault("ldc_payment_gateway_url", "https://credit.linux.do/epay");

        Map<String, Object> refundParams = new LinkedHashMap<>();
        refundParams.put("pid", clientId);
        refundParams.put("key", clientSecret);
        refundParams.put("trade_no", order.getLdcTradeNo());
        refundParams.put("money", order.getTotalAmount().setScale(2).toPlainString());

        try {
            String response = HttpUtil.post(gatewayUrl + "/api.php", refundParams);
            log.info("LDC refund response: {}", response);
            if (StrUtil.isNotBlank(response) && !response.contains("\"code\":1")) {
                log.error("LDC refund failed: {}", response);
                throw new BusinessException(ResultCode.ORDER_PAY_FAIL);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("LDC refund request error", e);
            throw new BusinessException(ResultCode.ORDER_PAY_FAIL);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void doRefundDbOperations(Order order) {
        if (order.getProductType() == 1) {
            List<OrderCard> orderCards = orderCardMapper.selectList(
                    new LambdaQueryWrapper<OrderCard>().eq(OrderCard::getOrderId, order.getId()));
            List<Long> cardIds = orderCards.stream().map(OrderCard::getCardId).collect(Collectors.toList());
            if (!cardIds.isEmpty()) {
                productCardMapper.batchReleaseCards(cardIds);
            }
            orderCardMapper.delete(new LambdaQueryWrapper<OrderCard>().eq(OrderCard::getOrderId, order.getId()));
        }

        if (order.getProductType() == 2) {
            productMapper.restoreStock(order.getProductId(), order.getQuantity());
            productMapper.decrementSoldCount(order.getProductId(), order.getQuantity());
        }

        order.setPaymentStatus(Order.PAYMENT_REFUNDED);
        orderMapper.updateById(order);
        AuditLog.log("refundOrder", "orderId=" + order.getId());
    }

    /**
     * 批量转换订单为详情结果（解决 N+1 查询）/ Batch convert orders to detail results (fixes N+1 queries)
     */
    private List<OrderDetailResult> batchConvertToDetailResults(List<Order> orders, boolean showCards) {
        if (orders.isEmpty()) return List.of();

        Set<Long> userIds = orders.stream().map(Order::getUserId).collect(Collectors.toSet());
        Set<Long> productIds = orders.stream().map(Order::getProductId).collect(Collectors.toSet());

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return orders.stream().map(order -> {
            OrderDetailResult result = BeanUtil.copyProperties(order, OrderDetailResult.class);

            User buyer = userMap.get(order.getUserId());
            if (buyer != null) result.setBuyerName(buyer.getUsername());

            Product product = productMap.get(order.getProductId());
            if (product != null) result.setProductCoverImage(product.getCoverImage());

            return result;
        }).collect(Collectors.toList());
    }

    /**
     * 转换为订单详情结果 / Convert to order detail result
     */
    private OrderDetailResult convertToDetailResult(Order order, boolean showCards) {
        OrderDetailResult result = BeanUtil.copyProperties(order, OrderDetailResult.class);

        User buyer = userMapper.selectById(order.getUserId());
        if (buyer != null) {
            result.setBuyerName(buyer.getUsername());
        }

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            result.setProductCoverImage(product.getCoverImage());
        }

        if (showCards && order.getPaymentStatus() == Order.PAYMENT_PAID && order.getProductType() == 1) {
            List<OrderCard> orderCards = orderCardMapper.selectList(
                    new LambdaQueryWrapper<OrderCard>().eq(OrderCard::getOrderId, order.getId()));
            if (!orderCards.isEmpty()) {
                List<Long> cardIds = orderCards.stream().map(OrderCard::getCardId).collect(Collectors.toList());
                Map<Long, ProductCard> cardMap = productCardMapper.selectBatchIds(cardIds).stream()
                        .collect(Collectors.toMap(ProductCard::getId, c -> c));
                List<String> cardContents = orderCards.stream()
                        .map(oc -> {
                            ProductCard card = cardMap.get(oc.getCardId());
                            return card != null ? cryptoUtil.decrypt(card.getCardContent()) : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                result.setCardContents(cardContents);
            }
        }

        return result;
    }
}
