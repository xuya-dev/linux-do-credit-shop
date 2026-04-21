package dev.xuya.ldcshop.common.util;

import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.entity.User;

/**
 * 用户上下文工具类 / User Context Utility
 * 获取当前登录用户信息
 *
 * @author xuya
 */
public class UserContextUtil {

    /**
     * 获取当前登录用户ID / Get current logged-in user ID
     *
     * @return 用户ID / User ID
     */
    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前登录用户角色 / Get current logged-in user role
     *
     * @return 角色 / Role
     */
    public static String getCurrentUserRole() {
        try {
            return (String) StpUtil.getSession().get("role");
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 判断当前用户是否为管理员 / Check if current user is admin
     *
     * @return 是否管理员 / Is admin
     */
    public static boolean isAdmin() {
        return "admin".equals(getCurrentUserRole());
    }

    /**
     * 检查当前用户是否为管理员，否则抛出异常 / Require admin role or throw exception
     */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 将用户信息存入会话 / Store user info in session
     *
     * @param user 用户实体 / User entity
     */
    public static void setUserSession(User user) {
        StpUtil.getSession().set("userId", user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());
        StpUtil.getSession().set("avatar", user.getAvatar());
        StpUtil.getSession().set("status", user.getStatus());
    }
}
