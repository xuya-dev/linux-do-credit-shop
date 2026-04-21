package dev.xuya.ldcshop.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 安全配置校验器 / Security Configuration Validator
 * 应用启动时检查 JWT 密钥是否已正确配置
 *
 * @author xuya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityConfigValidator implements ApplicationRunner {

    private final Environment env;

    /**
     * 启动时执行安全校验 / Run security validation on startup
     *
     * @param args 应用启动参数 / Application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        String jwtSecret = env.getProperty("sa-token.jwt-secret-key", "");
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        if (jwtSecret.isBlank()) {
            if (isProd) {
                throw new IllegalStateException("SA_TOKEN_JWT_SECRET must be set in production! Add it to your .env file.");
            }
            log.warn("SA_TOKEN_JWT_SECRET is not set. Using auto-generated token. Set it via environment variable for security.");
        }
    }
}
