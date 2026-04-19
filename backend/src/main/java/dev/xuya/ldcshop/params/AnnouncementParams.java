package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公告创建/编辑参数 / Announcement Create/Update Params
 *
 * @author xuya
 */
@Data
public class AnnouncementParams {

    /** 公告标题 / Announcement Title */
    @NotBlank(message = "公告标题不能为空 / Title is required")
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
}
