package dev.xuya.ldcshop.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.config.LdcShopProperties;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.params.OAuthCallbackParams;
import dev.xuya.ldcshop.results.LoginResult;
import dev.xuya.ldcshop.results.UserInfoResult;
import dev.xuya.ldcshop.service.AuthService;
import dev.xuya.ldcshop.service.UserService;
import dev.xuya.ldcshop.util.UserContextUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现 / Authentication Service Implementation
 * 处理 LINUX DO OAuth2.0 登录流程
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LdcShopProperties properties;
    private final UserMapper userMapper;
    private final UserService userService;

    /**
     * 获取 OAuth 授权地址 / Get OAuth authorize URL
     * 构建 LINUX DO Connect 的授权跳转链接
     */
    @Override
    public String getAuthorizeUrl(String redirectUri) {
        LdcShopProperties.OAuth oauth = properties.getOauth();
        String redirect = cn.hutool.core.util.StrUtil.isNotBlank(redirectUri)
                ? redirectUri : oauth.getRedirectUri();

        return oauth.getAuthorizeUrl()
                + "?response_type=code"
                + "&client_id=" + oauth.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8)
                + "&scope=read";
    }

    /**
     * 处理 OAuth 回调 / Handle OAuth callback
     * 1. 使用授权码换取 Access Token
     * 2. 使用 Token 获取用户信息
     * 3. 创建或更新本地用户
     * 4. 执行 Sa-Token 登录
     */
    @Override
    public LoginResult handleCallback(OAuthCallbackParams params) {
        LdcShopProperties.OAuth oauth = properties.getOauth();

        // 第一步：使用授权码换取 Access Token / Step 1: Exchange code for token
        String accessToken = exchangeToken(params.getCode(), params.getRedirectUri(), oauth);
        if (cn.hutool.core.util.StrUtil.isBlank(accessToken)) {
            throw new BusinessException(ResultCode.OAUTH_TOKEN_ERROR);
        }

        // 第二步：获取用户信息 / Step 2: Get user info
        JSONObject userInfo = fetchUserInfo(accessToken, oauth);
        if (userInfo == null) {
            throw new BusinessException(ResultCode.OAUTH_USER_INFO_ERROR);
        }

        // 第三步：创建或更新本地用户 / Step 3: Create or update local user
        User user = syncUser(userInfo);

        // 第四步：执行登录 / Step 4: Login
        StpUtil.login(user.getId());
        UserContextUtil.setUserSession(user);

        // 构建返回结果 / Build result
        LoginResult result = new LoginResult();
        result.setToken(StpUtil.getTokenValue());
        result.setUser(userService.getUserInfo(user.getId()));
        result.setIsNewUser(false); // 简化逻辑，不区分新老用户 / Simplified, no new/old user distinction

        log.info("用户登录成功 / User login success: userId={}, username={}", user.getId(), user.getUsername());
        return result;
    }

    /**
     * 获取当前登录用户信息 / Get current logged-in user info
     */
    @Override
    public UserInfoResult getCurrentUserInfo() {
        Long userId = UserContextUtil.getCurrentUserId();
        return userService.getUserInfo(userId);
    }

    /**
     * 退出登录 / Logout
     */
    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 使用授权码换取 Access Token / Exchange authorization code for access token
     */
    private String exchangeToken(String code, String redirectUri, LdcShopProperties.OAuth oauth) {
        String redirect = cn.hutool.core.util.StrUtil.isNotBlank(redirectUri)
                ? redirectUri : oauth.getRedirectUri();

        Map<String, Object> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("code", code);
        tokenParams.put("client_id", oauth.getClientId());
        tokenParams.put("client_secret", oauth.getClientSecret());
        tokenParams.put("redirect_uri", redirect);

        try {
            String response = HttpUtil.post(oauth.getTokenUrl(), tokenParams);
            JSONObject json = JSONUtil.parseObj(response);
            return json.getStr("access_token");
        } catch (Exception e) {
            log.error("获取Access Token失败 / Failed to get access token", e);
            return null;
        }
    }

    /**
     * 获取 LINUX DO 用户信息 / Fetch LINUX DO user info
     */
    private JSONObject fetchUserInfo(String accessToken, LdcShopProperties.OAuth oauth) {
        try {
            String response = HttpUtil.createGet(oauth.getUserInfoUrl())
                    .header("Authorization", "Bearer " + accessToken)
                    .execute()
                    .body();
            return JSONUtil.parseObj(response);
        } catch (Exception e) {
            log.error("获取用户信息失败 / Failed to get user info", e);
            return null;
        }
    }

    /**
     * 同步用户信息到本地数据库 / Sync user info to local database
     */
    private User syncUser(JSONObject userInfo) {
        String ldcUid = userInfo.getStr("id");
        String username = userInfo.getStr("username");
        String nickname = userInfo.getStr("name", username);
        String avatar = userInfo.getStr("avatar_url");
        String email = userInfo.getStr("email");
        Integer trustLevel = userInfo.getInt("trust_level", 0);

        // 查找已存在用户 / Find existing user
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getLdcUid, ldcUid)
        );

        boolean isNewUser = false;
        if (user == null) {
            // 新用户注册 / New user registration
            user = new User();
            user.setLdcUid(ldcUid);
            user.setUsername(username);
            user.setRole(determineRole(username));
            user.setStatus(1);
            isNewUser = true;
        }

        // 更新用户信息 / Update user info
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setEmail(email);
        user.setTrustLevel(trustLevel);
        user.setLastLoginAt(LocalDateTime.now());

        user = userService.createOrUpdateUser(user);
        return user;
    }

    /**
     * 判断用户角色 / Determine user role
     * 如果用户名在管理员列表中，则赋予admin角色
     */
    private String determineRole(String username) {
        String adminUsernames = properties.getAdmin().getInitialUsernames();
        if (cn.hutool.core.util.StrUtil.isNotBlank(adminUsernames)) {
            for (String adminName : adminUsernames.split(",")) {
                if (username.equalsIgnoreCase(adminName.trim())) {
                    return "admin";
                }
            }
        }
        return "user";
    }
}
