package dev.xuya.ldcshop.service;

import dev.xuya.ldcshop.params.OAuthCallbackParams;
import dev.xuya.ldcshop.results.LoginResult;
import dev.xuya.ldcshop.results.UserInfoResult;

/**
 * 认证服务接口 / Authentication Service Interface
 * 处理 LINUX DO OAuth 登录相关业务
 *
 * @author xuya
 */
public interface AuthService {

    /**
     * 获取 OAuth 授权地址 / Get OAuth authorize URL
     *
     * @param redirectUri 回调地址（可选覆盖） / Redirect URI (optional override)
     * @return 授权地址 / Authorize URL
     */
    String getAuthorizeUrl(String redirectUri);

    /**
     * 处理 OAuth 回调，完成登录 / Handle OAuth callback and login
     *
     * @param params 回调参数 / Callback params
     * @return 登录结果 / Login result
     */
    LoginResult handleCallback(OAuthCallbackParams params);

    /**
     * 获取当前登录用户信息 / Get current logged-in user info
     *
     * @return 用户信息 / User info
     */
    UserInfoResult getCurrentUserInfo();

    /**
     * 退出登录 / Logout
     */
    void logout();
}
