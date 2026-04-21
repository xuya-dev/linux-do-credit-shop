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
            "ldc_oauth_authorize_url", "ldc_oauth_token_url",
            "ldc_admin_usernames"
    );

    private final ShopSettingService shopSettingService;

    /**
     * 获取所有配置 / Get all settings
     * 返回所有系统配置项（敏感值已脱敏），映射为类型化的 SettingsResult
     * Returns all system settings (sensitive values masked), mapped to a typed SettingsResult
     */
    @GetMapping
    public R<SettingsResult> getAll() {
        return R.ok(toSettingsResult(shopSettingService.getAllSettingsMasked()));
    }

    /**
     * 批量更新配置 / Batch update settings
     * 只更新白名单内的配置键，防止非法字段写入
     * Only updates whitelisted setting keys to prevent illegal field writes
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
        result.setLdcOAuthClientId(map.get("ldc_oauth_client_id"));
        result.setLdcOAuthClientSecret(map.get("ldc_oauth_client_secret"));
        result.setLdcOAuthRedirectUri(map.get("ldc_oauth_redirect_uri"));
        result.setLdcOAuthAuthorizeUrl(map.get("ldc_oauth_authorize_url"));
        result.setLdcOAuthTokenUrl(map.get("ldc_oauth_token_url"));
        result.setLdcAdminUsernames(map.get("ldc_admin_usernames"));
        return result;
    }
}
