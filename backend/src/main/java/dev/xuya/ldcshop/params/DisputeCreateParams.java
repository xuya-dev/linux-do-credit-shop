package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建争议参数 / Create Dispute Params
 * 用户发起争议时提交的参数
 *
 * @author xuya
 */
@Data
public class DisputeCreateParams {

    /** 订单ID / Order ID */
    @NotNull(message = "订单ID不能为空 / Order ID is required")
    private Long orderId;

    /** 争议原因 / Dispute Reason */
    @NotBlank(message = "争议原因不能为空 / Dispute reason is required")
    private String reason;

    /** 证据图片URL列表 / Evidence Image URLs */
    private List<String> evidence;
}
