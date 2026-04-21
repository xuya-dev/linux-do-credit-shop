package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卡密详情结果 / Card Detail Result
 * 管理端查看单张卡密的完整信息（含解密后的卡密内容）
 *
 * @author xuya
 */
@Data
public class CardDetailResult {

    /** 卡密ID / Card ID */
    private Long id;

    /** 商品ID / Product ID */
    private Long productId;

    /** 卡密内容（解密后）/ Card content (decrypted) */
    private String cardContent;

    /** 状态: 0=可用 1=已售 2=已禁用 / Status */
    private Integer status;

    /** 关联订单ID / Related Order ID */
    private Long orderId;

    /** 售出时间 / Sold Time */
    private LocalDateTime soldAt;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;
}
