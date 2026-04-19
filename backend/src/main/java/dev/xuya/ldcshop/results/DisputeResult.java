package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 争议结果 / Dispute Result
 * 返回给前端的争议信息
 *
 * @author xuya
 */
@Data
public class DisputeResult {

    /** 争议ID / Dispute ID */
    private Long id;

    /** 订单ID / Order ID */
    private Long orderId;

    /** 订单编号 / Order Number */
    private String orderNo;

    /** 商品名称 / Product Name */
    private String productName;

    /** 发起用户ID / Initiator User ID */
    private Long userId;

    /** 发起用户名 / Initiator Username */
    private String username;

    /** 争议原因 / Dispute Reason */
    private String reason;

    /** 证据图片URL / Evidence Image URLs */
    private String evidence;

    /** 状态 / Status */
    private Integer status;

    /** 状态名称 / Status Name */
    private String statusName;

    /** 管理员处理备注 / Admin Note */
    private String adminNote;

    /** 处理人 / Handler */
    private String handlerName;

    /** 处理时间 / Handled Time */
    private LocalDateTime handledAt;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    private LocalDateTime updatedAt;
}
