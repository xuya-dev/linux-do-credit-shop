package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 / Order Entity
 * 对应数据库 orders 表
 *
 * @author xuya
 */
@Data
@TableName("ldc_order")
public class Order {

    /** 订单ID / Order ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 / Order Number */
    private String orderNo;

    /** 买家用户ID / Buyer User ID */
    private Long userId;

    /** 商品ID / Product ID */
    private Long productId;

    /** 商品名称(快照) / Product Name(Snapshot) */
    private String productName;

    /** 商品类型: 1=虚拟 2=实物 / Product Type */
    private Integer productType;

    /** 购买数量 / Quantity */
    private Integer quantity;

    /** 单价(积分) / Unit Price(Credits) */
    private BigDecimal unitPrice;

    /** 总金额(积分) / Total Amount(Credits) */
    private BigDecimal totalAmount;

    /** LDC平台交易号 / LDC Trade Number */
    private String ldcTradeNo;

    /** 商户业务单号 / Merchant Out Trade Number */
    private String ldcOutTradeNo;

    /** 支付状态: 0=待支付 1=已支付 2=已退款 / Payment Status */
    private Integer paymentStatus;

    /** 发货状态: 0=待发货 1=已发货 2=已完成 / Delivery Status */
    private Integer deliveryStatus;

    /** 配送信息(JSON) / Delivery Info(JSON) */
    private String deliveryInfo;

    /** 联系方式 / Contact Info */
    private String contactInfo;

    /** 买家备注 / Buyer Remark */
    private String remark;

    /** 管理员备注 / Admin Remark */
    private String adminRemark;

    /** 支付时间 / Paid Time */
    private LocalDateTime paidAt;

    /** 发货时间 / Delivered Time */
    private LocalDateTime deliveredAt;

    /** 创建时间 / Created Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 / Logical Delete */
    @TableLogic
    private Integer deleted;

    // ============================================================
    // 支付状态常量 / Payment Status Constants
    // ============================================================
    /** 待支付 / Pending Payment */
    public static final int PAYMENT_PENDING = 0;
    /** 已支付 / Paid */
    public static final int PAYMENT_PAID = 1;
    /** 已退款 / Refunded */
    public static final int PAYMENT_REFUNDED = 2;
    /** 已取消 / Cancelled */
    public static final int PAYMENT_CANCELLED = 3;

    // ============================================================
    // 发货状态常量 / Delivery Status Constants
    // ============================================================
    /** 待发货 / Pending Delivery */
    public static final int DELIVERY_PENDING = 0;
    /** 已发货 / Delivered */
    public static final int DELIVERY_DELIVERED = 1;
    /** 已完成 / Completed */
    public static final int DELIVERY_COMPLETED = 2;
}
