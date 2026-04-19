package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OAuth 回调参数 / OAuth Callback Params
 * 接收 LINUX DO OAuth 回调的授权码
 *
 * @author xuya
 */
@Data
public class OAuthCallbackParams {

    /** 授权码 / Authorization Code */
    @NotBlank(message = "授权码不能为空 / Authorization code is required")
    private String code;

    /** 回调地址（可选覆盖） / Redirect URI (optional override) */
    private String redirectUri;
}
