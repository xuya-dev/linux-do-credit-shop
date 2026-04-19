package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "处理结果不能为空 / Handle result is required")
    private Integer status;

    /** 管理员处理备注 / Admin Processing Note */
    private String adminNote;
}
