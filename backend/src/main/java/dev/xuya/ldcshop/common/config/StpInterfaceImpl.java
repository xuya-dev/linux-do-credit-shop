package dev.xuya.ldcshop.common.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限/角色接口实现 / Sa-Token Permission/Role Interface Implementation
 * 为 Sa-Token 框架提供当前登录用户的权限列表和角色列表
 *
 * @author xuya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    /**
     * 获取权限列表（当前项目未使用细粒度权限）/ Get permission list
     *
     * @param loginId   登录ID / Login ID
     * @param loginType 登录类型 / Login type
     * @return 权限列表（空）/ Permission list (empty)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 获取角色列表 / Get role list
     * 优先从数据库获取最新角色，并同步更新到 Session 缓存
     *
     * @param loginId   登录ID / Login ID
     * @param loginType 登录类型 / Login type
     * @return 角色列表 / Role list
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Object sessionRole = StpUtil.getSessionByLoginId(loginId).get("role");
            Long userId = Long.valueOf(loginId.toString());
            User user = userMapper.selectById(userId);
            if (user != null) {
                String dbRole = user.getRole();
                if (sessionRole == null || !sessionRole.equals(dbRole)) {
                    StpUtil.getSessionByLoginId(loginId).set("role", dbRole);
                }
                return List.of(dbRole);
            }
            if (sessionRole != null) {
                return List.of(sessionRole.toString());
            }
        } catch (Exception e) {
            log.warn("Failed to get role for loginId={}: {}", loginId, e.getMessage());
        }
        return Collections.emptyList();
    }
}
