package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.entity.Announcement;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.AnnouncementMapper;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.params.AnnouncementParams;
import dev.xuya.ldcshop.results.AnnouncementResult;
import dev.xuya.ldcshop.service.AnnouncementService;
import dev.xuya.ldcshop.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公告服务实现 / Announcement Service Implementation
 *
 * @author xuya
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    private final UserMapper userMapper;

    /** 公告类型名称映射 / Announcement type name mapping */
    private static final Map<Integer, String> TYPE_NAMES = Map.of(
            1, "通知 / Notice",
            2, "活动 / Activity",
            3, "更新 / Update"
    );

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
        return announcement.getId();
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementParams params) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        BeanUtil.copyProperties(params, announcement, "id", "createdBy");
        updateById(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        removeById(id);
    }

    @Override
    public void publishAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        announcement.setStatus(Announcement.STATUS_PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());
        updateById(announcement);
    }

    @Override
    public void unpublishAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        announcement.setStatus(Announcement.STATUS_DRAFT);
        updateById(announcement);
    }

    @Override
    public void toggleTop(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        announcement.setIsTop(announcement.getIsTop() == 1 ? 0 : 1);
        updateById(announcement);
    }

    @Override
    public AnnouncementResult getAnnouncementDetail(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        return convertToResult(announcement);
    }

    @Override
    public IPage<AnnouncementResult> pageUserAnnouncements(int page, int size, Integer type) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, Announcement.STATUS_PUBLISHED);
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }
        wrapper.orderByDesc(Announcement::getIsTop).orderByDesc(Announcement::getPublishedAt);

        IPage<Announcement> announcementPage = page(new Page<>(page, size), wrapper);
        return announcementPage.convert(this::convertToResult);
    }

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
        return announcementPage.convert(this::convertToResult);
    }

    @Override
    public List<AnnouncementResult> getLatestAnnouncements(int limit) {
        List<Announcement> announcements = list(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, Announcement.STATUS_PUBLISHED)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getPublishedAt)
                .last("LIMIT " + limit));
        return announcements.stream().map(this::convertToResult).collect(Collectors.toList());
    }

    /**
     * 转换为公告结果 / Convert to announcement result
     */
    private AnnouncementResult convertToResult(Announcement announcement) {
        AnnouncementResult result = BeanUtil.copyProperties(announcement, AnnouncementResult.class);
        result.setTypeName(TYPE_NAMES.getOrDefault(announcement.getType(), "未知 / Unknown"));
        if (announcement.getCreatedBy() != null) {
            User user = userMapper.selectById(announcement.getCreatedBy());
            if (user != null) {
                result.setCreatedByName(user.getUsername());
            }
        }
        return result;
    }
}
