package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卡密列表结果（脱敏）/ Card List Result (masked)
 * 列表接口不返回 cardContent，仅详情接口返回
 *
 * @author xuya
 */
@Data
public class CardListResult {

    /** 卡密ID / Card ID */
    private Long id;
    /** 商品ID / Product ID */
    private Long productId;
    /** 卡密状态 / Card status */
    private Integer status;
    /** 关联订单ID / Associated order ID */
    private Long orderId;
    /** 售出时间 / Sold time */
    private LocalDateTime soldAt;
    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
