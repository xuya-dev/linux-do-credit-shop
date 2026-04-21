package dev.xuya.ldcshop.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类 / CORS Configuration
 * 根据应用配置属性设置允许的跨域来源、请求头和请求方法
 *
 * @author xuya
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final LdcShopProperties properties;

    /**
     * 创建 CORS 过滤器 Bean / Create CORS Filter Bean
     *
     * @return 配置好的 CorsFilter / Configured CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        for (String origin : properties.getCors().getAllowedOrigins()) {
            config.addAllowedOrigin(origin);
        }

        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");

        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
