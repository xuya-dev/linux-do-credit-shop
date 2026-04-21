package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.AdminSettingsUpdateParams;
import dev.xuya.ldcshop.results.SettingsResult;
import dev.xuya.ldcshop.service.ShopSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 管理端-系统设置控制器 / Admin Settings Controller
 * 仅管理数据库中的业务配置（商店信息、支付配置）
 * OAuth 和管理员配置通过 ENV 环境变量管理（登录前所需）
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
            "ldc_payment_gateway_url", "ldc_payment_notify_url", "ldc_payment_return_url"
    );

    private final ShopSettingService shopSettingService;

    /**
     * 获取所有配置 / Get all settings
     */
    @GetMapping
    public R<SettingsResult> getAll() {
        return R.ok(toSettingsResult(shopSettingService.getAllSettingsMasked()));
    }

    /**
     * 批量更新配置 / Batch update settings
     */
    @PutMapping
    public R<Void> batchUpdate(@Valid @RequestBody AdminSettingsUpdateParams params) {
        Map<String, String> settings = params.getSettings();
        settings.entrySet().removeIf(e -> !ALLOWED_KEYS.contains(e.getKey()));
        shopSettingService.batchUpdateSettings(settings);
        return R.ok();
    }

    /**
     * 将扁平的键值对映射为 SettingsResult 对象 / Map flat key-value pairs to a SettingsResult
     */
    private SettingsResult toSettingsResult(Map<String, String> map) {
        SettingsResult result = new SettingsResult();
        result.setShopName(map.get("shop_name"));
        result.setShopDescription(map.get("shop_description"));
        result.setShopLogo(map.get("shop_logo"));
        result.setShopNotice(map.get("shop_notice"));
        result.setLdcPaymentClientId(map.get("ldc_payment_client_id"));
        result.setLdcPaymentClientSecret(map.get("ldc_payment_client_secret"));
        result.setLdcPaymentPrivateKey(map.get("ldc_payment_private_key"));
        result.setLdcPaymentPublicKey(map.get("ldc_payment_public_key"));
        result.setLdcPaymentGatewayUrl(map.get("ldc_payment_gateway_url"));
        result.setLdcPaymentNotifyUrl(map.get("ldc_payment_notify_url"));
        result.setLdcPaymentReturnUrl(map.get("ldc_payment_return_url"));
        return result;
    }
}
