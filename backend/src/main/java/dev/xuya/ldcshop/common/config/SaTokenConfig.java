package dev.xuya.ldcshop.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.ldcshop.common.interceptor.LoginCheckInterceptor;
import dev.xuya.ldcshop.common.interceptor.RateLimitInterceptor;
import dev.xuya.ldcshop.common.interceptor.TraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Sa-Token 与 Web MVC 配置类 / Sa-Token and Web MVC Configuration
 * 注册拦截器链（链路追踪、登录校验、限流、Sa-Token 鉴权），
 * 并配置前端 SPA 路由的静态资源回退策略
 *
 * @author xuya
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final TraceIdInterceptor traceIdInterceptor;

    /**
     * 注册拦截器 / Register interceptors
     * 按顺序添加链路追踪、Sa-Token 鉴权、登录校验和限流拦截器
     *
     * @param registry 拦截器注册表 / Interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(new SaInterceptor(handle -> {
            StpUtil.checkLogin();
        }))
        .addPathPatterns("/api/admin/**", "/api/user/**")
        .excludePathPatterns(
                "/api/auth/**",
                "/api/public/**",
                "/api/order/notify",
                "/actuator/**",
                "/doc.html",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**"
        );

        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/api/user/**", "/api/admin/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/public/**",
                        "/api/order/notify"
                );

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/user/orders", "/api/user/orders/**",
                        "/api/user/disputes", "/api/user/disputes/**",
                        "/api/auth/**",
                        "/api/user/products", "/api/user/products/**")
                .excludePathPatterns(
                        "/api/order/notify"
                );
    }

    /**
     * 配置静态资源处理 / Configure static resource handling
     * 支持 SPA 前端路由的 HTML5 History 模式回退
     *
     * @param registry 资源处理器注册表 / Resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
