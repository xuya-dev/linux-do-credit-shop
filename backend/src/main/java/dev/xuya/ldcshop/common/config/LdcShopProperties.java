package dev.xuya.ldcshop.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

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
