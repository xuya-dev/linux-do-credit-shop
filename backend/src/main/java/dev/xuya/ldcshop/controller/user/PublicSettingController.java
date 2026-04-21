package dev.xuya.ldcshop.controller.user;

import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.service.ShopSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

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

    private static final Set<String> PUBLIC_KEYS = Set.of(
            "shop_name", "shop_description", "shop_logo", "shop_notice"
    );

    private final ShopSettingService shopSettingService;

    /**
     * 获取公开配置 / Get public settings
     * 只返回前端展示需要的公开配置项（商店名称、描述、Logo、公告），不含任何敏感信息
     */
    @GetMapping
    public R<Map<String, String>> getPublic() {
        Map<String, String> allSettings = shopSettingService.getAllSettings();
        allSettings.entrySet().removeIf(e -> !PUBLIC_KEYS.contains(e.getKey()));
        return R.ok(allSettings);
    }
}
