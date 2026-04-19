package dev.xuya.ldcshop.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Object role = StpUtil.getSessionByLoginId(loginId).get("role");
            if (role != null) {
                return List.of(role.toString());
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }
}
