package dev.xuya.ldcshop.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单发货参数 / Order Delivery Params
 * 管理员为实物商品订单发货时提交的参数
 *
 * @author xuya
 */
@Data
public class OrderDeliveryParams {

    /** 物流信息 / Logistics Info */
    private String deliveryInfo;

    /** 管理员备注 / Admin Remark */
    private String adminRemark;
}
