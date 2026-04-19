package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.AnnouncementParams;
import dev.xuya.ldcshop.results.AnnouncementResult;
import dev.xuya.ldcshop.service.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-公告管理控制器 / Admin Announcement Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-公告 / Admin Announcements")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public R<IPage<AnnouncementResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        return R.ok(announcementService.pageAdminAnnouncements(page, size, type, status));
    }

    @GetMapping("/{id}")
    public R<AnnouncementResult> detail(@PathVariable Long id) {
        return R.ok(announcementService.getAnnouncementDetail(id));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody AnnouncementParams params) {
        return R.ok(announcementService.createAnnouncement(params));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementParams params) {
        announcementService.updateAnnouncement(id, params);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return R.ok();
    }

    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return R.ok();
    }

    @PutMapping("/{id}/unpublish")
    public R<Void> unpublish(@PathVariable Long id) {
        announcementService.unpublishAnnouncement(id);
        return R.ok();
    }

    @PutMapping("/{id}/top")
    public R<Void> toggleTop(@PathVariable Long id) {
        announcementService.toggleTop(id);
        return R.ok();
    }
}
