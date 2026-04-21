package dev.xuya.ldcshop.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 链路追踪 ID 拦截器 / Trace ID Interceptor
 * 为每个请求生成或读取链路追踪 ID，并写入 MDC 和响应头
 *
 * @author xuya
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    /**
     * 请求预处理：设置链路追踪 ID / Pre-handle: set trace ID
     * 优先读取请求头 X-Request-Id，否则生成新的 16 位 ID
     *
     * @param request  HTTP 请求 / HTTP request
     * @param response HTTP 响应 / HTTP response
     * @param handler  处理器 / Handler
     * @return 始终放行 / Always true
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader("X-Request-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        MDC.put(TRACE_ID, traceId);
        response.setHeader("X-Request-Id", traceId);
        return true;
    }

    /**
     * 请求完成后清理 MDC / Clean up MDC after request completion
     *
     * @param request  HTTP 请求 / HTTP request
     * @param response HTTP 响应 / HTTP response
     * @param handler  处理器 / Handler
     * @param ex       异常（如有）/ Exception if any
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(TRACE_ID);
    }
}
