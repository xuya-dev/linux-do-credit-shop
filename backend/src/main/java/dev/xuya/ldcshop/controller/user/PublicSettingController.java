package dev.xuya.ldcshop.controller.user;

import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.results.PublicSettingsResult;
import dev.xuya.ldcshop.service.ShopSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公开-系统设置控制器 / Public Settings Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/public/settings")
@RequiredArgsConstructor
@Tag(name = "公开-设置 / Public Settings")
public class PublicSettingController {

    private final ShopSettingService shopSettingService;

    /**
     * 获取公开配置 / Get public settings
     * 只返回前端展示需要的公开配置项（商店名称、描述、Logo、公告），不含任何敏感信息
     * Returns only the public-facing settings (shop name, description, logo, notice) with no sensitive data
     */
    @GetMapping
    public R<PublicSettingsResult> getPublic() {
        Map<String, String> allSettings = shopSettingService.getAllSettings();
        return R.ok(toPublicSettingsResult(allSettings));
    }

    /**
     * 将白名单键映射为 PublicSettingsResult / Map whitelisted keys to a PublicSettingsResult
     */
    private PublicSettingsResult toPublicSettingsResult(Map<String, String> map) {
        PublicSettingsResult result = new PublicSettingsResult();
        result.setShopName(map.get("shop_name"));
        result.setShopDescription(map.get("shop_description"));
        result.setShopLogo(map.get("shop_logo"));
        result.setShopNotice(map.get("shop_notice"));
        return result;
    }
}
