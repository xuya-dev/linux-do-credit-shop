package dev.xuya.ldcshop.results;

import lombok.Data;

/**
 * 管理端设置结果 / Admin Settings Result
 * 仅包含数据库管理的业务配置（商店信息、支付配置）
 * OAuth 和管理员配置通过 ENV 环境变量管理，不在此返回
 *
 * @author xuya
 */
@Data
public class SettingsResult {

    /** 商店名称 / Shop Name */
    private String shopName;
    /** 商店描述 / Shop Description */
    private String shopDescription;
    /** 商店Logo / Shop Logo URL */
    private String shopLogo;
    /** 商店公告 / Shop Notice */
    private String shopNotice;

    /** 支付客户端ID / Payment Client ID */
    private String ldcPaymentClientId;
    /** 支付客户端密钥（脱敏）/ Payment Client Secret (masked) */
    private String ldcPaymentClientSecret;
    /** Ed25519私钥（脱敏）/ Ed25519 Private Key (masked) */
    private String ldcPaymentPrivateKey;
    /** Ed25519公钥 / Ed25519 Public Key */
    private String ldcPaymentPublicKey;
    /** 支付网关URL / Payment Gateway URL */
    private String ldcPaymentGatewayUrl;
    /** 异步通知URL / Payment Notify URL */
    private String ldcPaymentNotifyUrl;
    /** 同步跳转URL / Payment Return URL */
    private String ldcPaymentReturnUrl;
}
