package dev.xuya.ldcshop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录检查拦截器 / Login Check Interceptor
 * 用于检查用户状态及刷新会话信息
 *
 * @author xuya
 */
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    /**
     * 请求前置处理 / Pre-handle request
     * 检查用户登录状态是否有效
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Sa-Token 已在 SaInterceptor 中处理登录校验
        // 此处可扩展：检查用户是否被封禁、刷新用户信息缓存等
        return true;
    }
}
