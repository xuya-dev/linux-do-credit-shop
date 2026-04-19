package dev.xuya.ldcshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类 / CORS Configuration
 * 允许前端开发服务器跨域访问后端接口
 *
 * @author xuya
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器 / Configure CORS Filter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（开发环境） / Allow all origins (development)
        config.addAllowedOriginPattern("*");
        // 允许所有请求头 / Allow all headers
        config.addAllowedHeader("*");
        // 允许所有请求方法 / Allow all methods
        config.addAllowedMethod("*");
        // 允许携带凭证 / Allow credentials
        config.setAllowCredentials(true);
        // 预检请求缓存时间 / Preflight cache duration
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
