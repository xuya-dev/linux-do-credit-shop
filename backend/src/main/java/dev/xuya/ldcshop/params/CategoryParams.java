package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类创建/编辑参数 / Category Create/Update Params
 *
 * @author xuya
 */
@Data
public class CategoryParams {

    /** 分类名称 / Category Name */
    @NotBlank(message = "{validation.category_name_required}")
    private String name;

    /** 分类图标URL / Category Icon URL */
    private String icon;

    /** 排序值 / Sort Order */
    private Integer sortOrder;

    /** 状态: 0=禁用 1=启用 / Status: 0=disabled 1=enabled */
    private Integer status;
}
