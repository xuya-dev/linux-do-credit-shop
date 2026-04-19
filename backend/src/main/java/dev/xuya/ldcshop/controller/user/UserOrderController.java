package dev.xuya.ldcshop.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.OrderCreateParams;
import dev.xuya.ldcshop.results.OrderDetailResult;
import dev.xuya.ldcshop.results.PaymentSubmitResult;
import dev.xuya.ldcshop.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户端-订单控制器 / User Order Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
@Tag(name = "用户端-订单 / User Orders")
public class UserOrderController {

    private final OrderService orderService;

    /**
     * 创建订单 / Create order
     */
    @PostMapping
    public R<OrderDetailResult> create(@Valid @RequestBody OrderCreateParams params) {
        return R.ok(orderService.createOrder(params));
    }

    /**
     * 我的订单列表 / My orders
     */
    @GetMapping
    public R<IPage<OrderDetailResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Integer deliveryStatus) {
        return R.ok(orderService.pageUserOrders(page, size, paymentStatus, deliveryStatus));
    }

    /**
     * 订单详情 / Order detail
     */
    @GetMapping("/{id}")
    public R<OrderDetailResult> detail(@PathVariable Long id) {
        return R.ok(orderService.getOrderDetail(id));
    }

    /**
     * 发起支付 / Initiate payment
     */
    @PostMapping("/{id}/pay")
    public R<PaymentSubmitResult> pay(@PathVariable Long id) {
        return R.ok(orderService.initiatePayment(id));
    }
}
