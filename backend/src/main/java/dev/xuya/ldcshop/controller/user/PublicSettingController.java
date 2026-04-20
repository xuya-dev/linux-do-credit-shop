package dev.xuya.ldcshop.controller.user;

import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.service.ShopSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
     * 只返回前端展示需要的公开配置项（如：site_name, site_logo等）
     */
    @GetMapping
    public R<Map<String, String>> getPublic() {
        Map<String, String> allSettings = shopSettingService.getAllSettings();
        Map<String, String> publicSettings = new HashMap<>();
        
        // 白名单过滤，只返回安全的公开设置
        String[] allowedKeys = {"site_name", "site_logo", "site_announcement", "site_footer"};
        for (String key : allowedKeys) {
            if (allSettings.containsKey(key)) {
                publicSettings.put(key, allSettings.get(key));
            }
        }
        
        return R.ok(publicSettings);
    }
}
