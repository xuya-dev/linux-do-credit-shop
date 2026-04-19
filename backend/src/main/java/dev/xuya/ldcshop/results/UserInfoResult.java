package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息结果 / User Info Result
 * 返回给前端的用户信息
 *
 * @author xuya
 */
@Data
public class UserInfoResult {

    /** 用户ID / User ID */
    private Long id;

    /** LINUX DO 用户ID / LINUX DO User ID */
    private String ldcUid;

    /** 用户名 / Username */
    private String username;

    /** 昵称 / Nickname */
    private String nickname;

    /** 头像URL / Avatar URL */
    private String avatar;

    /** 邮箱 / Email */
    private String email;

    /** 信任等级 / Trust Level */
    private Integer trustLevel;

    /** 角色: admin/user / Role */
    private String role;

    /** 状态 / Status */
    private Integer status;

    /** 最后登录时间 / Last Login Time */
    private LocalDateTime lastLoginAt;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;
}
