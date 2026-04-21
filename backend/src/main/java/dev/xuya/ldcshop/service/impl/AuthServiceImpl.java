package dev.xuya.ldcshop.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.config.LdcShopProperties;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.params.OAuthCallbackParams;
import dev.xuya.ldcshop.results.LoginResult;
import dev.xuya.ldcshop.results.UserInfoResult;
import dev.xuya.ldcshop.service.AuthService;
import dev.xuya.ldcshop.service.UserService;
import dev.xuya.ldcshop.common.util.UserContextUtil;
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
 * 处理 LINUX DO OAuth2.0 登录流程，OAuth 配置从 ENV/YAML 读取（登录前必须可用）
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
     */
    @Override
    public String getAuthorizeUrl(String redirectUri) {
        LdcShopProperties.OAuth oauth = properties.getOauth();
        String redirect = oauth.getRedirectUri();
        if (StrUtil.isNotBlank(redirectUri) && redirectUri.startsWith(oauth.getRedirectUri())) {
            redirect = redirectUri;
        }

        return oauth.getAuthorizeUrl()
                + "?response_type=code"
                + "&client_id=" + oauth.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8)
                + "&scope=read";
    }

    /**
     * 处理 OAuth 回调 / Handle OAuth callback
     */
    @Override
    public LoginResult handleCallback(OAuthCallbackParams params) {
        LdcShopProperties.OAuth oauth = properties.getOauth();

        String accessToken = exchangeToken(params.getCode(), params.getRedirectUri(), oauth);
        if (StrUtil.isBlank(accessToken)) {
            throw new BusinessException(ResultCode.OAUTH_TOKEN_ERROR);
        }

        JSONObject userInfo = fetchUserInfo(accessToken, oauth);
        if (userInfo == null) {
            throw new BusinessException(ResultCode.OAUTH_USER_INFO_ERROR);
        }

        User user = syncUser(userInfo);

        StpUtil.login(user.getId());
        UserContextUtil.setUserSession(user);

        LoginResult result = new LoginResult();
        result.setToken(StpUtil.getTokenValue());
        result.setUser(userService.getUserInfo(user.getId()));
        result.setIsNewUser(false);

        AuditLog.log("login", "userId=" + user.getId() + ", username=" + user.getUsername());
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
        AuditLog.log("logout", "userId=" + UserContextUtil.getCurrentUserId());
        StpUtil.logout();
    }

    /**
     * 使用授权码换取 Access Token / Exchange authorization code for access token
     */
    private String exchangeToken(String code, String redirectUri, LdcShopProperties.OAuth oauth) {
        String redirect = StrUtil.isNotBlank(redirectUri) ? redirectUri : oauth.getRedirectUri();

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
            log.error("Failed to get access token", e);
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
            log.error("Failed to get user info", e);
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

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getLdcUid, ldcUid)
        );

        if (user == null) {
            user = new User();
            user.setLdcUid(ldcUid);
            user.setUsername(username);
            user.setRole(determineRole(username));
            user.setStatus(1);
        }

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
     */
    private String determineRole(String username) {
        String adminUsernames = properties.getAdmin().getInitialUsernames();
        if (StrUtil.isNotBlank(adminUsernames)) {
            for (String adminName : adminUsernames.split(",")) {
                if (username.equalsIgnoreCase(adminName.trim())) {
                    return "admin";
                }
            }
        }
        return "user";
    }
}
