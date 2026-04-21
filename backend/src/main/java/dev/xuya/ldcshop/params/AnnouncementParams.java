package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公告创建/编辑参数 / Announcement Create/Update Params
 *
 * @author xuya
 */
@Data
public class AnnouncementParams {

    /** 公告标题 / Announcement Title */
    @NotBlank(message = "{validation.title_required}")
    @Size(max = 200, message = "{validation.title_too_long}")
    private String title;

    /** 公告正文(Markdown) / Content(Markdown) */
    private String content;

    /** 类型: 1=通知 2=活动 3=更新 / Type: 1=notice 2=activity 3=update */
    @Min(value = 1, message = "{validation.announcement_type_invalid}")
    @Max(value = 3, message = "{validation.announcement_type_invalid}")
    private Integer type;

    /** 是否置顶: 0=否 1=是 / Is Pinned */
    @Min(value = 0, message = "{validation.is_top_invalid}")
    @Max(value = 1, message = "{validation.is_top_invalid}")
    private Integer isTop;

    /** 状态: 0=草稿 1=已发布 / Status: 0=draft 1=published */
    @Min(value = 0, message = "{validation.status_invalid}")
    @Max(value = 1, message = "{validation.status_invalid}")
    private Integer status;

    /** 封面图片 / Cover Image */
    private String coverImage;
}
