package dev.xuya.ldcshop.results;

import lombok.Data;

/**
 * 支付提交结果 / Payment Submit Result
 * 发起支付后返回的信息，前端用于跳转
 *
 * @author xuya
 */
@Data
public class PaymentSubmitResult {

    /** 订单编号 / Order Number */
    private String orderNo;

    /** 支付跳转URL / Payment Redirect URL */
    private String payUrl;
}
