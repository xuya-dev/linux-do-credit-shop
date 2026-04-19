package dev.xuya.ldcshop.controller.user;

import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付回调控制器 / Payment Callback Controller
 * 接收 LDC Credit 平台的异步通知（无需登录验证）
 *
 * @author xuya
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "支付回调 / Payment Callback")
public class PaymentCallbackController {

    private final OrderService orderService;

    /**
     * LDC 异步通知回调 / LDC async notification callback
     * 该接口不需要登录验证，由 LDC 平台调用
     */
    @GetMapping("/notify")
    public String handleNotify(@RequestParam Map<String, String> params) {
        log.info("收到LDC支付回调 / Received LDC payment callback: {}", params);
        try {
            return orderService.handlePaymentNotify(params);
        } catch (Exception e) {
            log.error("处理支付回调异常 / Error processing payment callback", e);
            return "fail";
        }
    }
}
