package dev.xuya.ldcshop.common.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.config.LdcShopProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器 / Rate Limit Interceptor
 * 基于 Redis 对用户或 IP 进行滑动窗口限流，超出限制返回 429
 *
 * @author xuya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final LdcShopProperties properties;

    /**
     * 请求预处理：检查限流 / Pre-handle: check rate limit
     * 使用 Redis INCR + EXPIRE 实现固定窗口计数器
     *
     * @param request  HTTP 请求 / HTTP request
     * @param response HTTP 响应 / HTTP response
     * @param handler  处理器 / Handler
     * @return true 放行，false 被限流 / true to proceed, false if rate limited
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId;

        int maxRequests = properties.getRateLimit().getMaxRequests();
        int windowSeconds = properties.getRateLimit().getWindowSeconds();

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        if (count != null && count > maxRequests) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            R<Void> result = R.fail(ResultCode.TOO_MANY_REQUESTS);
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            log.warn("Rate limit exceeded: clientId={}, count={}", clientId, count);
            return false;
        }
        return true;
    }

    /**
     * 获取客户端标识（已登录用户使用用户ID，否则使用IP地址）
     * Get client identifier (logged-in user ID or IP address)
     *
     * @param request HTTP 请求 / HTTP request
     * @return 客户端标识 / Client identifier
     */
    private String getClientId(HttpServletRequest request) {
        try {
            return "user:" + StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return "ip:" + request.getRemoteAddr();
        }
    }
}
