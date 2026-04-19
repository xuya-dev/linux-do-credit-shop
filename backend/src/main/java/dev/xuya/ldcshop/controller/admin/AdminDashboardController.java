package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.results.DashboardResult;
import dev.xuya.ldcshop.service.StatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-数据看板控制器 / Admin Dashboard Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-看板 / Admin Dashboard")
public class AdminDashboardController {

    private final StatisticsService statisticsService;

    /**
     * 获取看板数据 / Get dashboard data
     */
    @GetMapping
    public R<DashboardResult> getDashboard() {
        return R.ok(statisticsService.getDashboard());
    }
}
