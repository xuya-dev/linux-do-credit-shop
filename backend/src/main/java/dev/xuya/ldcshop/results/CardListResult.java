package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卡密列表结果 / Card List Result
 *
 * @author xuya
 */
@Data
public class CardListResult {

    /** 卡密ID / Card ID */
    private Long id;
    /** 商品ID / Product ID */
    private Long productId;
    /** 卡密内容 / Card content */
    private String cardContent;
    /** 卡密状态 / Card status */
    private Integer status;
    /** 关联订单ID / Associated order ID */
    private Long orderId;
    /** 售出时间 / Sold time */
    private LocalDateTime soldAt;
    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
