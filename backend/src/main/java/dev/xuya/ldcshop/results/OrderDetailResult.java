package dev.xuya.ldcshop.results;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情结果 / Order Detail Result
 * 返回给前端的订单详细信息
 *
 * @author xuya
 */
@Data
public class OrderDetailResult {

    /** 订单ID / Order ID */
    private Long id;

    /** 订单编号 / Order Number */
    private String orderNo;

    /** 买家用户ID / Buyer User ID */
    private Long userId;

    /** 买家用户名 / Buyer Username */
    private String buyerName;

    /** 商品ID / Product ID */
    private Long productId;

    /** 商品名称 / Product Name */
    private String productName;

    /** 商品类型 / Product Type */
    private Integer productType;

    /** 商品封面图 / Product Cover Image */
    private String productCoverImage;

    /** 购买数量 / Quantity */
    private Integer quantity;

    /** 单价 / Unit Price */
    private BigDecimal unitPrice;

    /** 总金额 / Total Amount */
    private BigDecimal totalAmount;

    /** LDC平台交易号 / LDC Trade Number */
    private String ldcTradeNo;

    /** 支付状态: 0=待支付 1=已支付 2=已退款 / Payment Status */
    private Integer paymentStatus;

    /** 发货状态: 0=待发货 1=已发货 2=已完成 / Delivery Status */
    private Integer deliveryStatus;

    /** 配送信息 / Delivery Info */
    private String deliveryInfo;

    /** 联系方式 / Contact Info */
    private String contactInfo;

    /** 买家备注 / Buyer Remark */
    private String remark;

    /** 管理员备注 / Admin Remark */
    private String adminRemark;

    /** 卡密内容列表（虚拟商品，仅买家可见） / Card Contents (virtual product, buyer only) */
    private List<String> cardContents;

    /** 支付时间 / Paid Time */
    private LocalDateTime paidAt;

    /** 发货时间 / Delivered Time */
    private LocalDateTime deliveredAt;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    private LocalDateTime updatedAt;
}
