package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体 / Announcement Entity
 * 对应数据库 announcement 表
 *
 * @author xuya
 */
@Data
@TableName("ldc_announcement")
public class Announcement {

    /** 公告ID / Announcement ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 / Announcement Title */
    private String title;

    /** 公告正文(Markdown) / Content(Markdown) */
    private String content;

    /** 类型: 1=通知 2=活动 3=更新 / Type: 1=notice 2=activity 3=update */
    private Integer type;

    /** 是否置顶: 0=否 1=是 / Is Pinned */
    private Integer isTop;

    /** 状态: 0=草稿 1=已发布 / Status: 0=draft 1=published */
    private Integer status;

    /** 封面图片 / Cover Image */
    private String coverImage;

    /** 发布时间 / Published Time */
    private LocalDateTime publishedAt;

    /** 创建人ID / Created By */
    private Long createdBy;

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
    // 公告类型常量 / Announcement Type Constants
    // ============================================================
    /** 通知 / Notice */
    public static final int TYPE_NOTICE = 1;
    /** 活动 / Activity */
    public static final int TYPE_ACTIVITY = 2;
    /** 更新 / Update */
    public static final int TYPE_UPDATE = 3;

    // ============================================================
    // 公告状态常量 / Announcement Status Constants
    // ============================================================
    /** 草稿 / Draft */
    public static final int STATUS_DRAFT = 0;
    /** 已发布 / Published */
    public static final int STATUS_PUBLISHED = 1;
}
