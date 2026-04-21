package dev.xuya.ldcshop.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LDC Shop 自定义配置属性 / LDC Shop Custom Configuration Properties
 * 映射 application.yml 中 ldc-shop 前缀的配置项
 * 仅包含基础设施配置（OAuth、管理员等登录前所需的配置）
 * 业务配置（支付、商店信息等）通过管理后台存入数据库
 *
 * @author xuya
 */
@Data
@Component
@ConfigurationProperties(prefix = "ldc-shop")
public class LdcShopProperties {

    /**
     * LINUX DO OAuth 配置（登录前必须可用）/ LINUX DO OAuth Config (must be available before login)
     */
    private OAuth oauth = new OAuth();

    /**
     * 管理员配置（登录时判断角色）/ Admin Config (used during login to determine role)
     */
    private Admin admin = new Admin();

    /**
     * CORS 跨域配置 / CORS Configuration
     */
    private Cors cors = new Cors();

    /**
     * 限流配置 / Rate Limit Configuration
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 订单配置 / Order Configuration
     */
    private Order order = new Order();

    /**
     * 卡密加密密钥 / Card Content Encryption Secret
     */
    private String cardEncryptSecret = "please-change-this-secret-in-production";

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
     * 管理员配置 / Admin Configuration
     */
    @Data
    public static class Admin {
        /** 管理员用户名（逗号分隔）/ Admin Usernames (comma separated) */
        private String initialUsernames;
    }

    /**
     * CORS 跨域配置 / CORS Configuration
     */
    @Data
    public static class Cors {
        /** 允许的来源列表 / Allowed origins list */
        private List<String> allowedOrigins = List.of("http://localhost:3000");
    }

    @Data
    public static class RateLimit {
        /** 窗口内最大请求数 / Max requests per window */
        private int maxRequests = 60;
        /** 窗口时间(秒) / Window duration in seconds */
        private int windowSeconds = 60;
    }

    @Data
    public static class Order {
        /** 未支付订单自动关闭超时(分钟) / Auto-close timeout for unpaid orders (minutes) */
        private int autoCloseMinutes = 30;
    }
}
