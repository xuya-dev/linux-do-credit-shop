package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 处理争议参数 / Handle Dispute Params
 * 管理员处理争议时提交的参数
 *
 * @author xuya
 */
@Data
public class DisputeHandleParams {

    /** 处理状态: 1=同意(退款) 2=拒绝 3=平台介入 / Status: 1=accept 2=reject 3=platform */
    @NotNull(message = "{validation.handle_result_required}")
    @jakarta.validation.constraints.Max(value = 3, message = "{validation.handle_result_invalid}")
    @jakarta.validation.constraints.Min(value = 1, message = "{validation.handle_result_invalid}")
    private Integer status;

    /** 管理员处理备注 / Admin Processing Note */
    private String adminNote;
}
