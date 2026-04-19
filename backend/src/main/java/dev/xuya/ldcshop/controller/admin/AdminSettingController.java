package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.service.ShopSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端-系统设置控制器 / Admin Settings Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-设置 / Admin Settings")
public class AdminSettingController {

    private final ShopSettingService shopSettingService;

    /**
     * 获取所有配置 / Get all settings
     */
    @GetMapping
    public R<Map<String, String>> getAll() {
        return R.ok(shopSettingService.getAllSettings());
    }

    /**
     * 批量更新配置 / Batch update settings
     */
    @PutMapping
    public R<Void> batchUpdate(@RequestBody Map<String, String> settings) {
        shopSettingService.batchUpdateSettings(settings);
        return R.ok();
    }
}
