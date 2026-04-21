package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.service.ShopSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 管理端-系统设置控制器 / Admin Settings Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-设置 / Admin Settings")
public class AdminSettingController {

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "shop_name", "shop_description", "shop_logo", "shop_notice",
            "ldc_payment_client_id", "ldc_payment_client_secret", "ldc_payment_private_key", "ldc_payment_public_key",
            "ldc_payment_gateway_url", "ldc_payment_notify_url", "ldc_payment_return_url",
            "ldc_oauth_client_id", "ldc_oauth_client_secret", "ldc_oauth_redirect_uri",
            "ldc_oauth_authorize_url", "ldc_oauth_token_url"
    );

    private final ShopSettingService shopSettingService;

    @GetMapping
    public R<Map<String, String>> getAll() {
        Map<String, String> settings = shopSettingService.getAllSettingsMasked();
        settings.entrySet().removeIf(e -> !ALLOWED_KEYS.contains(e.getKey()));
        return R.ok(settings);
    }

    @PutMapping
    public R<Void> batchUpdate(@RequestBody Map<String, String> settings) {
        settings.entrySet().removeIf(e -> !ALLOWED_KEYS.contains(e.getKey()));
        shopSettingService.batchUpdateSettings(settings);
        return R.ok();
    }
}
