package dev.xuya.ldcshop.results;

import lombok.Data;

/**
 * 登录结果 / Login Result
 * OAuth 登录成功后返回的信息
 *
 * @author xuya
 */
@Data
public class LoginResult {

    /** Sa-Token 令牌 / Sa-Token */
    private String token;

    /** 用户信息 / User Info */
    private UserInfoResult user;

    /** 是否为新用户 / Is New User */
    private Boolean isNewUser;
}
