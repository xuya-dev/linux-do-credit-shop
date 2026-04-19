package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体 / Shop Setting Entity
 * 对应数据库 shop_setting 表，Key-Value 形式存储系统配置
 *
 * @author xuya
 */
@Data
@TableName("shop_setting")
public class ShopSetting {

    /** 配置ID / Setting ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键 / Setting Key */
    private String settingKey;

    /** 配置值 / Setting Value */
    private String settingValue;

    /** 配置描述 / Description */
    private String description;

    /** 更新时间 / Updated Time */
    private LocalDateTime updatedAt;
}
