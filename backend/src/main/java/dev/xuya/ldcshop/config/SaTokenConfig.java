package dev.xuya.ldcshop.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.ldcshop.interceptor.LoginCheckInterceptor;
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
 * Web MVC 配置类 / Web MVC Configuration
 * 配置拦截器、跨域、静态资源、SPA路由转发
 *
 * @author xuya
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    /**
     * 注册拦截器 / Register Interceptors
     * - Sa-Token 拦截器：用于校验登录和角色权限
     * - 登录检查拦截器：用于刷新会话等
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sa-Token 拦截器，校验登录 / Sa-Token interceptor for login check
        registry.addInterceptor(new SaInterceptor(handle -> {
            StpUtil.checkLogin();
        }))
        .addPathPatterns("/admin/**", "/user/**")
        .excludePathPatterns(
                "/auth/**",
                "/public/**",
                "/order/notify",
                "/doc.html",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**"
        );

        // 登录检查拦截器 / Login check interceptor
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/user/**", "/admin/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/public/**",
                        "/order/notify"
                );
    }

    /**
     * 静态资源映射 / Static Resource Mapping
     * 将前端构建产物从 classpath:/static/ 提供服务
     * Serve frontend build output from classpath:/static/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前端静态资源 / Frontend static resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    /**
                     * SPA 路由转发 / SPA Route Forwarding
                     * 所有未匹配到静态文件的请求转发到 index.html
                     * Forward all unmatched requests to index.html for SPA routing
                     */
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        // 如果文件存在且可读，直接返回 / If file exists and readable, return it
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // API 路径不转发 / Don't forward API paths
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        // 其他路径转发到 index.html (SPA 路由) / Forward to index.html (SPA routing)
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
