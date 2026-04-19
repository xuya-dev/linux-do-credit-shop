package dev.xuya.ldcshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LDC Shop 自定义配置属性 / LDC Shop Custom Configuration Properties
 * 映射 application.yml 中 ldc-shop 前缀的配置项
 *
 * @author xuya
 */
@Data
@Component
@ConfigurationProperties(prefix = "ldc-shop")
public class LdcShopProperties {

    /**
     * LINUX DO OAuth 配置 / LINUX DO OAuth Configuration
     */
    private OAuth oauth = new OAuth();

    /**
     * LDC Credit 支付配置 / LDC Credit Payment Configuration
     */
    private Payment payment = new Payment();

    /**
     * 管理员配置 / Admin Configuration
     */
    private Admin admin = new Admin();

    /**
     * OAuth 配置 / OAuth Configuration
     */
    @Data
    public static class OAuth {
        /** OAuth 客户端ID / OAuth Client ID */
        private String clientId;
        /** OAuth 客户端密钥 / OAuth Client Secret */
        private String clientSecret;
        /** 回调地址 / Redirect URI */
        private String redirectUri;
        /** 授权地址 / Authorize URL */
        private String authorizeUrl;
        /** 令牌地址 / Token URL */
        private String tokenUrl;
        /** 用户信息地址 / User Info URL */
        private String userInfoUrl;
    }

    /**
     * 支付配置 / Payment Configuration
     */
    @Data
    public static class Payment {
        /** LDC 客户端ID / LDC Client ID */
        private String clientId;
        /** LDC 客户端密钥 / LDC Client Secret */
        private String clientSecret;
        /** LDC 网关地址 / LDC Gateway URL */
        private String gatewayUrl;
        /** 异步通知地址 / Async Notify URL */
        private String notifyUrl;
        /** 同步跳转地址 / Sync Return URL */
        private String returnUrl;
    }

    /**
     * 管理员配置 / Admin Configuration
     */
    @Data
    public static class Admin {
        /** 初始管理员用户名（逗号分隔） / Initial Admin Usernames (comma separated) */
        private String initialUsernames;
    }
}
