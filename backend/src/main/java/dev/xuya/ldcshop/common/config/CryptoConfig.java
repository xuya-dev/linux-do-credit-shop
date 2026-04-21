package dev.xuya.ldcshop.common.config;

import dev.xuya.ldcshop.common.util.CryptoUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 加密工具配置类 / Crypto Utility Configuration
 * 将卡密加密密钥注入 CryptoUtil 并注册为 Spring Bean
 *
 * @author xuya
 */
@Configuration
public class CryptoConfig {
    /**
     * 创建 CryptoUtil Bean / Create CryptoUtil Bean
     *
     * @param properties 应用配置属性 / Application properties
     * @return CryptoUtil 实例 / CryptoUtil instance
     */
    @Bean
    public CryptoUtil cryptoUtil(LdcShopProperties properties) {
        return new CryptoUtil(properties.getCardEncryptSecret());
    }
}
