package dev.xuya.ldcshop.config;

import cn.dev33.satoken.servlet.filter.SaServletFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaFilterConfig {

    @Bean
    public SaServletFilter getSaServletFilter() {
        SaServletFilter filter = new SaServletFilter();
        filter.addInclude("/**");
        filter.addExclude(
                "/api/auth/**",
                "/api/public/**",
                "/api/order/notify",
                "/**/*.html",
                "/**/*.js",
                "/**/*.css",
                "/**/*.ico",
                "/**/*.png",
                "/**/*.jpg",
                "/**/*.svg",
                "/**/*.woff",
                "/**/*.woff2",
                "/**/*.ttf"
        );
        filter.setAuth(obj -> {
        });
        return filter;
    }
}
