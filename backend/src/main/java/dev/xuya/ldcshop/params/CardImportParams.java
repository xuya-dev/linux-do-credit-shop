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
    @NotNull(message = "{validation.product_id_required}")
    private Long productId;

    /** 卡密内容列表 / Card Content List */
    @NotEmpty(message = "{validation.card_content_required}")
    private List<@NotBlank(message = "{validation.card_content_no_blank}") String> cards;
}
