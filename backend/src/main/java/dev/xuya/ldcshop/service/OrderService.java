package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.entity.Order;
import dev.xuya.ldcshop.params.OrderCreateParams;
import dev.xuya.ldcshop.params.OrderDeliveryParams;
import dev.xuya.ldcshop.results.OrderDetailResult;
import dev.xuya.ldcshop.results.PaymentSubmitResult;

/**
 * 订单服务接口 / Order Service Interface
 *
 * @author xuya
 */
public interface OrderService {

    /**
     * 创建订单 / Create order
     *
     * @param params 订单参数 / Order params
     * @return 订单详情 / Order detail
     */
    OrderDetailResult createOrder(OrderCreateParams params);

    /**
     * 发起支付 / Initiate payment
     *
     * @param orderId 订单ID / Order ID
     * @return 支付提交结果 / Payment submit result
     */
    PaymentSubmitResult initiatePayment(Long orderId);

    /**
     * 处理支付回调通知 / Handle payment callback notification
     *
     * @param params 回调参数 / Callback params
     * @return 处理结果（返回success给LDC） / Result (return "success" to LDC)
     */
    String handlePaymentNotify(java.util.Map<String, String> params);

    /**
     * 获取订单详情 / Get order detail
     *
     * @param orderId 订单ID / Order ID
     * @return 订单详情 / Order detail
     */
    OrderDetailResult getOrderDetail(Long orderId);

    /**
     * 根据订单编号获取订单详情 / Get order detail by order number
     *
     * @param orderNo 订单编号 / Order number
     * @return 订单详情 / Order detail
     */
    OrderDetailResult getOrderByOrderNo(String orderNo);

    /**
     * 用户订单分页 / User orders page
     */
    IPage<OrderDetailResult> pageUserOrders(int page, int size, Integer paymentStatus, Integer deliveryStatus);

    /**
     * 管理端订单分页 / Admin orders page
     */
    IPage<OrderDetailResult> pageAdminOrders(int page, int size, Integer paymentStatus,
                                              Integer deliveryStatus, String keyword);

    /**
     * 管理员发货 / Admin deliver order
     *
     * @param orderId 订单ID / Order ID
     * @param params  发货参数 / Delivery params
     */
    void deliverOrder(Long orderId, OrderDeliveryParams params);

    /**
     * 管理员退款 / Admin refund order
     *
     * @param orderId 订单ID / Order ID
     */
    void refundOrder(Long orderId);
}
