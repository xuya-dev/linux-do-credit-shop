package dev.xuya.ldcshop.controller.user;

import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.OAuthCallbackParams;
import dev.xuya.ldcshop.results.LoginResult;
import dev.xuya.ldcshop.results.UserInfoResult;
import dev.xuya.ldcshop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 / Auth Controller
 * 处理 OAuth 登录、获取用户信息、退出登录
 *
 * @author xuya
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证 / Authentication", description = "OAuth登录、用户信息、退出 / OAuth login, user info, logout")
public class AuthController {

    private final AuthService authService;

    /**
     * 获取 OAuth 授权地址 / Get OAuth authorize URL
     */
    @GetMapping("/authorize-url")
    @Operation(summary = "获取OAuth授权地址 / Get OAuth authorize URL")
    public R<String> getAuthorizeUrl(@RequestParam(required = false) String redirectUri) {
        return R.ok(authService.getAuthorizeUrl(redirectUri));
    }

    /**
     * OAuth 回调处理 / OAuth callback handler
     */
    @PostMapping("/callback")
    @Operation(summary = "OAuth回调 / OAuth callback")
    public R<LoginResult> callback(@Valid @RequestBody OAuthCallbackParams params) {
        return R.ok(authService.handleCallback(params));
    }

    /**
     * 获取当前用户信息 / Get current user info
     */
    @GetMapping("/userinfo")
    @Operation(summary = "获取当前用户信息 / Get current user info")
    public R<UserInfoResult> getUserInfo() {
        return R.ok(authService.getCurrentUserInfo());
    }

    /**
     * 退出登录 / Logout
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录 / Logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
