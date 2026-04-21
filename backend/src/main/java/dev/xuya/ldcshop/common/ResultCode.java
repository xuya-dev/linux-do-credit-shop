package dev.xuya.ldcshop.common;

import dev.xuya.ldcshop.common.util.I18nUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举 / Response Status Code Enum
 * message 字段为 i18n key，由 I18nUtil 根据请求头 Accept-Language 解析为对应语言
 *
 * @author xuya
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    FAIL(500, "fail"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not_found"),
    BAD_REQUEST(400, "bad_request"),
    TOO_MANY_REQUESTS(429, "too_many_requests"),

    USER_NOT_FOUND(1001, "user.not_found"),
    USER_DISABLED(1002, "user.disabled"),
    USER_ALREADY_EXISTS(1003, "user.already_exists"),
    OAUTH_FAIL(1004, "oauth.fail"),
    OAUTH_TOKEN_ERROR(1005, "oauth.token_error"),
    OAUTH_USER_INFO_ERROR(1006, "oauth.user_info_error"),

    PRODUCT_NOT_FOUND(2001, "product.not_found"),
    PRODUCT_OFF_SHELF(2002, "product.off_shelf"),
    PRODUCT_STOCK_INSUFFICIENT(2003, "product.stock_insufficient"),
    CATEGORY_NOT_FOUND(2004, "category.not_found"),

    ORDER_NOT_FOUND(3001, "order.not_found"),
    ORDER_STATUS_ERROR(3002, "order.status_error"),
    ORDER_ALREADY_PAID(3003, "order.already_paid"),
    ORDER_CREATE_FAIL(3004, "order.create_fail"),
    ORDER_PAY_FAIL(3005, "order.pay_fail"),
    ORDER_REFUND_FAIL(3006, "order.refund_fail"),

    CARD_NOT_FOUND(4001, "card.not_found"),
    CARD_STOCK_INSUFFICIENT(4002, "card.stock_insufficient"),
    CARD_IMPORT_FAIL(4003, "card.import_fail"),

    PAY_SIGN_ERROR(5001, "pay.sign_error"),
    PAY_VERIFY_ERROR(5002, "pay.verify_error"),
    PAY_CONFIG_ERROR(5003, "pay.config_error"),

    DISPUTE_NOT_FOUND(6001, "dispute.not_found"),
    DISPUTE_ALREADY_EXISTS(6002, "dispute.already_exists"),
    DISPUTE_STATUS_ERROR(6003, "dispute.status_error"),

    ANNOUNCEMENT_NOT_FOUND(7001, "announcement.not_found"),

    SETTING_NOT_FOUND(8001, "setting.not_found");

    private final int code;
    private final String key;

    /**
     * 获取国际化消息 / Get localized message
     * 通过 I18nUtil 根据当前请求的 Accept-Language 自动解析
     *
     * @return 国际化后的消息文本 / Localized message text
     */
    public String getMessage() {
        return I18nUtil.get(key);
    }
}
