package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体 / Category Entity
 * 对应数据库 category 表
 *
 * @author xuya
 */
@Data
@TableName("category")
public class Category {

    /** 分类ID / Category ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 / Category Name */
    private String name;

    /** 分类图标 / Category Icon */
    private String icon;

    /** 排序值（升序） / Sort Order (ascending) */
    private Integer sortOrder;

    /** 状态: 0=禁用 1=启用 / Status: 0=disabled 1=enabled */
    private Integer status;

    /** 创建时间 / Created Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 / Logical Delete */
    @TableLogic
    private Integer deleted;
}
