package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.OrderDeliveryParams;
import dev.xuya.ldcshop.results.OrderDetailResult;
import dev.xuya.ldcshop.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-订单管理控制器 / Admin Order Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-订单 / Admin Orders")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public R<IPage<OrderDetailResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Integer deliveryStatus,
            @RequestParam(required = false) String keyword) {
        return R.ok(orderService.pageAdminOrders(page, size, paymentStatus, deliveryStatus, keyword));
    }

    @GetMapping("/{id}")
    public R<OrderDetailResult> detail(@PathVariable Long id) {
        return R.ok(orderService.getOrderDetail(id));
    }

    /**
     * 发货 / Deliver order
     */
    @PutMapping("/{id}/deliver")
    public R<Void> deliver(@PathVariable Long id, @RequestBody OrderDeliveryParams params) {
        orderService.deliverOrder(id, params);
        return R.ok();
    }

    /**
     * 退款 / Refund order
     */
    @PutMapping("/{id}/refund")
    public R<Void> refund(@PathVariable Long id) {
        orderService.refundOrder(id);
        return R.ok();
    }
}
