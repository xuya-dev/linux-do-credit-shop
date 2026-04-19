package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 卡密导入参数 / Card Import Params
 * 管理员批量导入卡密时提交的参数
 *
 * @author xuya
 */
@Data
public class CardImportParams {

    /** 商品ID / Product ID */
    @NotNull(message = "商品ID不能为空 / Product ID is required")
    private Long productId;

    /** 卡密内容列表 / Card Content List */
    @NotEmpty(message = "卡密内容不能为空 / Card content list is required")
    private List<@NotBlank(message = "卡密内容不能包含空行 / Card content cannot be blank") String> cards;
}
