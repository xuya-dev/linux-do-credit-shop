package dev.xuya.ldcshop.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举 / Response Status Code Enum
 * 定义所有API接口的响应状态码及中英文描述
 *
 * @author xuya
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ============================================================
    // 通用状态码 / Common Status Codes
    // ============================================================
    SUCCESS(200, "操作成功 / Success"),
    FAIL(500, "操作失败 / Operation Failed"),
    UNAUTHORIZED(401, "未登录或登录已过期 / Unauthorized or Session Expired"),
    FORBIDDEN(403, "没有访问权限 / Access Denied"),
    NOT_FOUND(404, "资源不存在 / Resource Not Found"),
    BAD_REQUEST(400, "请求参数错误 / Bad Request"),
    TOO_MANY_REQUESTS(429, "请求过于频繁 / Too Many Requests"),

    // ============================================================
    // 用户模块 / User Module (1xxx)
    // ============================================================
    USER_NOT_FOUND(1001, "用户不存在 / User Not Found"),
    USER_DISABLED(1002, "用户已被禁用 / User Disabled"),
    USER_ALREADY_EXISTS(1003, "用户已存在 / User Already Exists"),
    OAUTH_FAIL(1004, "OAuth认证失败 / OAuth Authentication Failed"),
    OAUTH_TOKEN_ERROR(1005, "OAuth令牌获取失败 / OAuth Token Error"),
    OAUTH_USER_INFO_ERROR(1006, "OAuth用户信息获取失败 / OAuth User Info Error"),

    // ============================================================
    // 商品模块 / Product Module (2xxx)
    // ============================================================
    PRODUCT_NOT_FOUND(2001, "商品不存在 / Product Not Found"),
    PRODUCT_OFF_SHELF(2002, "商品已下架 / Product Off Shelf"),
    PRODUCT_STOCK_INSUFFICIENT(2003, "商品库存不足 / Insufficient Stock"),
    CATEGORY_NOT_FOUND(2004, "分类不存在 / Category Not Found"),

    // ============================================================
    // 订单模块 / Order Module (3xxx)
    // ============================================================
    ORDER_NOT_FOUND(3001, "订单不存在 / Order Not Found"),
    ORDER_STATUS_ERROR(3002, "订单状态异常 / Order Status Error"),
    ORDER_ALREADY_PAID(3003, "订单已支付 / Order Already Paid"),
    ORDER_CREATE_FAIL(3004, "订单创建失败 / Order Creation Failed"),
    ORDER_PAY_FAIL(3005, "支付发起失败 / Payment Initiation Failed"),
    ORDER_REFUND_FAIL(3006, "退款失败 / Refund Failed"),

    // ============================================================
    // 卡密模块 / Card Module (4xxx)
    // ============================================================
    CARD_NOT_FOUND(4001, "卡密不存在 / Card Not Found"),
    CARD_STOCK_INSUFFICIENT(4002, "卡密库存不足 / Card Stock Insufficient"),
    CARD_IMPORT_FAIL(4003, "卡密导入失败 / Card Import Failed"),

    // ============================================================
    // 支付模块 / Payment Module (5xxx)
    // ============================================================
    PAY_SIGN_ERROR(5001, "签名生成失败 / Signature Generation Failed"),
    PAY_VERIFY_ERROR(5002, "回调验签失败 / Callback Verification Failed"),
    PAY_CONFIG_ERROR(5003, "支付配置错误 / Payment Config Error"),

    // ============================================================
    // 争议模块 / Dispute Module (6xxx)
    // ============================================================
    DISPUTE_NOT_FOUND(6001, "争议不存在 / Dispute Not Found"),
    DISPUTE_ALREADY_EXISTS(6002, "该订单已存在争议 / Dispute Already Exists for This Order"),
    DISPUTE_STATUS_ERROR(6003, "争议状态异常 / Dispute Status Error"),

    // ============================================================
    // 公告模块 / Announcement Module (7xxx)
    // ============================================================
    ANNOUNCEMENT_NOT_FOUND(7001, "公告不存在 / Announcement Not Found"),

    // ============================================================
    // 设置模块 / Settings Module (8xxx)
    // ============================================================
    SETTING_NOT_FOUND(8001, "配置项不存在 / Setting Not Found");

    /**
     * 状态码 / Status Code
     */
    private final int code;

    /**
     * 提示信息（中英文） / Message (Bilingual)
     */
    private final String message;
}
