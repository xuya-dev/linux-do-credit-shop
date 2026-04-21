package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体 / System Config Entity
 * 对应数据库 ldc_setting 表，Key-Value 形式存储系统配置
 *
 * @author xuya
 */
@Data
@TableName("ldc_setting")
public class ShopSetting {

    /** 配置ID / Config ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键 / Config Key */
    private String configKey;

    /** 配置值 / Config Value */
    private String configValue;

    /** 配置描述 / Description */
    private String description;

    /** 更新时间 / Updated Time */
    private LocalDateTime updatedAt;
}
