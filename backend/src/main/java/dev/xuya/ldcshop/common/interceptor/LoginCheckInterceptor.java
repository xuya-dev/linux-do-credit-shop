package dev.xuya.ldcshop.common.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录状态校验拦截器 / Login Status Check Interceptor
 * 检查已登录用户是否被禁用，若被禁用则强制下线并返回 401
 *
 * @author xuya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    /**
     * 请求预处理：校验已登录用户状态 / Pre-handle: verify logged-in user status
     * 先查 Session 缓存，缓存命中且状态正常则放行；否则回查数据库
     *
     * @param request  HTTP 请求 / HTTP request
     * @param response HTTP 响应 / HTTP response
     * @param handler  处理器 / Handler
     * @return true 放行，false 拦截 / true to proceed, false to block
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!StpUtil.isLogin()) {
            return true;
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 先检查 Session 缓存，仅在状态为禁用时才查数据库 / Check session cache first, only hit DB if status is disabled
        Object statusObj = StpUtil.getSession().get("status");
        if (statusObj != null && Integer.valueOf(statusObj.toString()) == 1) {
            return true;
        }

        // 回退到数据库查询 / Fallback to DB check
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            StpUtil.logout(userId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(R.fail(401, "User disabled or not found")));
            return false;
        }

        // 更新 Session 缓存 / Update session cache
        StpUtil.getSession().set("status", user.getStatus());
        return true;
    }
}
