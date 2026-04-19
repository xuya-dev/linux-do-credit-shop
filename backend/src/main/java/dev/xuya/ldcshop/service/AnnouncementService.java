package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.params.AnnouncementParams;
import dev.xuya.ldcshop.results.AnnouncementResult;

import java.util.List;

/**
 * 公告服务接口 / Announcement Service Interface
 *
 * @author xuya
 */
public interface AnnouncementService {

    /**
     * 创建公告 / Create announcement
     */
    Long createAnnouncement(AnnouncementParams params);

    /**
     * 更新公告 / Update announcement
     */
    void updateAnnouncement(Long id, AnnouncementParams params);

    /**
     * 删除公告 / Delete announcement
     */
    void deleteAnnouncement(Long id);

    /**
     * 发布公告 / Publish announcement
     */
    void publishAnnouncement(Long id);

    /**
     * 下架公告 / Unpublish announcement
     */
    void unpublishAnnouncement(Long id);

    /**
     * 置顶/取消置顶 / Toggle pinned status
     */
    void toggleTop(Long id);

    /**
     * 获取公告详情 / Get announcement detail
     */
    AnnouncementResult getAnnouncementDetail(Long id);

    /**
     * 用户端分页查询已发布公告 / User side page query published announcements
     */
    IPage<AnnouncementResult> pageUserAnnouncements(int page, int size, Integer type);

    /**
     * 管理端分页查询所有公告 / Admin side page query all announcements
     */
    IPage<AnnouncementResult> pageAdminAnnouncements(int page, int size, Integer type, Integer status);

    /**
     * 获取最新公告列表（首页展示） / Get latest announcements (for homepage)
     */
    List<AnnouncementResult> getLatestAnnouncements(int limit);
}
