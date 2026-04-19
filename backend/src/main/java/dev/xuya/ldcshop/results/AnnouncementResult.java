package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告结果 / Announcement Result
 * 返回给前端的公告信息
 *
 * @author xuya
 */
@Data
public class AnnouncementResult {

    /** 公告ID / Announcement ID */
    private Long id;

    /** 公告标题 / Title */
    private String title;

    /** 公告正文(Markdown) / Content(Markdown) */
    private String content;

    /** 类型: 1=通知 2=活动 3=更新 / Type */
    private Integer type;

    /** 类型名称 / Type Name */
    private String typeName;

    /** 是否置顶 / Is Pinned */
    private Integer isTop;

    /** 状态 / Status */
    private Integer status;

    /** 封面图片 / Cover Image */
    private String coverImage;

    /** 发布时间 / Published Time */
    private LocalDateTime publishedAt;

    /** 创建人名称 / Creator Name */
    private String createdByName;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    private LocalDateTime updatedAt;
}
