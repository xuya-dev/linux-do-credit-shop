package dev.xuya.ldcshop.results;

import lombok.Data;

/**
 * 管理端设置结果 / Admin Settings Result
 * 所有配置项（敏感值已脱敏）
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

    /** OAuth客户端ID / OAuth Client ID */
    private String ldcOAuthClientId;
    /** OAuth客户端密钥（脱敏）/ OAuth Client Secret (masked) */
    private String ldcOAuthClientSecret;
    /** OAuth回调地址 / OAuth Redirect URI */
    private String ldcOAuthRedirectUri;
    /** OAuth授权地址 / OAuth Authorize URL */
    private String ldcOAuthAuthorizeUrl;
    /** OAuth令牌地址 / OAuth Token URL */
    private String ldcOAuthTokenUrl;

    /** 管理员用户名列表 / Admin Usernames */
    private String ldcAdminUsernames;
}
