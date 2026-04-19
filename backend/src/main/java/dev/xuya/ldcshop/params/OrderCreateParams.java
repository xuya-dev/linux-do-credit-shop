package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单参数 / Create Order Params
 * 用户下单时提交的参数
 *
 * @author xuya
 */
@Data
public class OrderCreateParams {

    /** 商品ID / Product ID */
    @NotNull(message = "商品ID不能为空 / Product ID is required")
    private Long productId;

    /** 购买数量 / Quantity */
    @NotNull(message = "购买数量不能为空 / Quantity is required")
    @Min(value = 1, message = "购买数量至少为1 / Minimum quantity is 1")
    private Integer quantity;

    /** 联系方式 / Contact Info */
    private String contactInfo;

    /** 买家备注 / Buyer Remark */
    private String remark;

    /** 配送信息（实物商品） / Delivery Info (for physical products) */
    private String deliveryInfo;
}
