package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 / User Entity
 * 对应数据库 user 表，存储 LINUX DO OAuth 同步的用户信息
 *
 * @author xuya
 */
@Data
@TableName("ldc_user")
public class User {

    /** 用户ID / User ID */
    @TableId(type = IdType.AUTO)
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

    /** 角色: admin/user / Role: admin/user */
    private String role;

    /** 状态: 0=禁用 1=正常 / Status: 0=disabled 1=enabled */
    private Integer status;

    /** 最后登录时间 / Last Login Time */
    private LocalDateTime lastLoginAt;

    /** 创建时间 / Created Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除: 0=未删除 1=已删除 / Logical Delete */
    @TableLogic
    private Integer deleted;
}
