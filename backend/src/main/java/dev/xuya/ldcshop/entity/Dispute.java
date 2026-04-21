package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 争议实体 / Dispute Entity
 * 对应数据库 dispute 表
 *
 * @author xuya
 */
@Data
@TableName("ldc_dispute")
public class Dispute {

    /** 争议ID / Dispute ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID / Order ID */
    private Long orderId;

    /** 发起用户ID / Initiator User ID */
    private Long userId;

    /** 争议原因 / Dispute Reason */
    private String reason;

    /** 证据(图片URL列表JSON) / Evidence(Image URLs JSON) */
    private String evidence;

    /** 状态: 0=待处理 1=已同意(退款) 2=已拒绝 3=平台介入 / Status */
    private Integer status;

    /** 管理员处理备注 / Admin Processing Note */
    private String adminNote;

    /** 处理人ID / Handler ID */
    private Long handledBy;

    /** 处理时间 / Handled Time */
    private LocalDateTime handledAt;

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
    // 争议状态常量 / Dispute Status Constants
    // ============================================================
    /** 待处理 / Pending */
    public static final int STATUS_PENDING = 0;
    /** 已同意(退款) / Accepted(Refund) */
    public static final int STATUS_ACCEPTED = 1;
    /** 已拒绝 / Rejected */
    public static final int STATUS_REJECTED = 2;
    /** 平台介入 / Platform Intervention */
    public static final int STATUS_PLATFORM = 3;
}
