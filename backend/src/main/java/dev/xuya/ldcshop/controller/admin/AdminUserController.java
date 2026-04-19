package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.results.UserInfoResult;
import dev.xuya.ldcshop.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-用户管理控制器 / Admin User Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-用户 / Admin Users")
public class AdminUserController {

    private final UserService userService;

    /**
     * 用户列表 / User list
     */
    @GetMapping
    public R<IPage<UserInfoResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        return R.ok(userService.pageUsers(page, size, keyword, role));
    }

    /**
     * 更新用户状态 / Update user status
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return R.ok();
    }

    /**
     * 更新用户角色 / Update user role
     */
    @PutMapping("/{id}/role")
    public R<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateUserRole(id, role);
        return R.ok();
    }
}
