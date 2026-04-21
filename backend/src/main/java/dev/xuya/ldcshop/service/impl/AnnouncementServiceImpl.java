package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.util.I18nUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.common.util.EntityUtil;
import dev.xuya.ldcshop.entity.Announcement;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.AnnouncementMapper;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.params.AnnouncementParams;
import dev.xuya.ldcshop.results.AnnouncementResult;
import dev.xuya.ldcshop.service.AnnouncementService;
import dev.xuya.ldcshop.common.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 公告服务实现 / Announcement Service Implementation
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    private final UserMapper userMapper;

    /** 公告类型名称 / Announcement type name keys */
    private static final Map<Integer, String> TYPE_KEYS = Map.of(
            1, "announcement.type_notice",
            2, "announcement.type_activity",
            3, "announcement.type_update"
    );

    /**
     * 创建公告 / Create an announcement
     */
    @Override
    public Long createAnnouncement(AnnouncementParams params) {
        Announcement announcement = BeanUtil.copyProperties(params, Announcement.class);
        announcement.setCreatedBy(UserContextUtil.getCurrentUserId());
        if (announcement.getStatus() == null) {
            announcement.setStatus(Announcement.STATUS_DRAFT);
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        if (announcement.getType() == null) {
            announcement.setType(Announcement.TYPE_NOTICE);
        }
        save(announcement);
        AuditLog.log("createAnnouncement", "announcementId=" + announcement.getId() + ", title=" + announcement.getTitle());
        return announcement.getId();
    }

    /**
     * 更新公告 / Update an announcement
     */
    @Override
    public void updateAnnouncement(Long id, AnnouncementParams params) {
        Announcement announcement = EntityUtil.requireNonNull(getById(id), ResultCode.ANNOUNCEMENT_NOT_FOUND);
        BeanUtil.copyProperties(params, announcement, "id", "createdBy");
        updateById(announcement);
        AuditLog.log("updateAnnouncement", "announcementId=" + id + ", title=" + announcement.getTitle());
    }

    /**
     * 删除公告 / Delete an announcement
     */
    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = EntityUtil.requireNonNull(getById(id), ResultCode.ANNOUNCEMENT_NOT_FOUND);
        removeById(id);
        AuditLog.log("deleteAnnouncement", "announcementId=" + id + ", title=" + announcement.getTitle());
    }

    /**
     * 发布公告 / Publish an announcement
     */
    @Override
    public void publishAnnouncement(Long id) {
        Announcement announcement = EntityUtil.requireNonNull(getById(id), ResultCode.ANNOUNCEMENT_NOT_FOUND);
        announcement.setStatus(Announcement.STATUS_PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());
        updateById(announcement);
    }

    /**
     * 取消发布公告 / Unpublish an announcement
     */
    @Override
    public void unpublishAnnouncement(Long id) {
        Announcement announcement = EntityUtil.requireNonNull(getById(id), ResultCode.ANNOUNCEMENT_NOT_FOUND);
        announcement.setStatus(Announcement.STATUS_DRAFT);
        updateById(announcement);
    }

    /**
     * 切换公告置顶状态 / Toggle announcement top status
     */
    @Override
    public void toggleTop(Long id) {
        Announcement announcement = EntityUtil.requireNonNull(getById(id), ResultCode.ANNOUNCEMENT_NOT_FOUND);
        announcement.setIsTop(announcement.getIsTop() == 1 ? 0 : 1);
        updateById(announcement);
    }

    /**
     * 获取公告详情 / Get announcement detail
     */
    @Override
    public AnnouncementResult getAnnouncementDetail(Long id) {
        Announcement announcement = EntityUtil.requireNonNull(getById(id), ResultCode.ANNOUNCEMENT_NOT_FOUND);
        AnnouncementResult result = BeanUtil.copyProperties(announcement, AnnouncementResult.class);
        result.setTypeName(I18nUtil.get(TYPE_KEYS.getOrDefault(announcement.getType(), "announcement.type_unknown")));
        if (announcement.getCreatedBy() != null) {
            User user = userMapper.selectById(announcement.getCreatedBy());
            if (user != null) {
                result.setCreatedByName(user.getUsername());
            }
        }
        return result;
    }

    /**
     * 用户端公告分页查询 / User-side announcement pagination
     */
    @Override
    public IPage<AnnouncementResult> pageUserAnnouncements(int page, int size, Integer type) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, Announcement.STATUS_PUBLISHED);
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }
        wrapper.orderByDesc(Announcement::getIsTop).orderByDesc(Announcement::getPublishedAt);

        IPage<Announcement> announcementPage = page(new Page<>(page, size), wrapper);
        return convertPage(announcementPage);
    }

    /**
     * 管理端公告分页查询 / Admin-side announcement pagination
     */
    @Override
    public IPage<AnnouncementResult> pageAdminAnnouncements(int page, int size, Integer type, Integer status) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        wrapper.orderByDesc(Announcement::getIsTop).orderByDesc(Announcement::getCreatedAt);

        IPage<Announcement> announcementPage = page(new Page<>(page, size), wrapper);
        return convertPage(announcementPage);
    }

    /**
     * 获取最新公告列表 / Get latest announcements
     */
    @Override
    public List<AnnouncementResult> getLatestAnnouncements(int limit) {
        List<Announcement> announcements = baseMapper.selectLatest(Math.min(limit, 50));
        return batchConvertToResults(announcements);
    }

    /**
     * 转换公告分页 / Convert announcement page
     */
    private IPage<AnnouncementResult> convertPage(IPage<Announcement> page) {
        Map<Long, User> userMap = resolveUserMap(page.getRecords());
        return page.convert(ann -> toResult(ann, userMap));
    }

    /**
     * 批量转换公告结果 / Batch convert announcement results
     */
    private List<AnnouncementResult> batchConvertToResults(List<Announcement> announcements) {
        Map<Long, User> userMap = resolveUserMap(announcements);
        return announcements.stream().map(ann -> toResult(ann, userMap)).collect(Collectors.toList());
    }

    /**
     * 解析公告关联的用户映射 / Resolve user map from announcements
     */
    private Map<Long, User> resolveUserMap(List<Announcement> announcements) {
        Set<Long> userIds = announcements.stream()
                .map(Announcement::getCreatedBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    /**
     * 转换公告为结果对象 / Convert announcement to result object
     */
    private AnnouncementResult toResult(Announcement announcement, Map<Long, User> userMap) {
        AnnouncementResult result = BeanUtil.copyProperties(announcement, AnnouncementResult.class);
        result.setTypeName(I18nUtil.get(TYPE_KEYS.getOrDefault(announcement.getType(), "announcement.type_unknown")));
        if (announcement.getCreatedBy() != null) {
            User user = userMap.get(announcement.getCreatedBy());
            if (user != null) {
                result.setCreatedByName(user.getUsername());
            }
        }
        return result;
    }
}
