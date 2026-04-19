package dev.xuya.ldcshop.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.results.AnnouncementResult;
import dev.xuya.ldcshop.service.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-公告控制器 / User Announcement Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/user/announcements")
@RequiredArgsConstructor
@Tag(name = "用户端-公告 / User Announcements")
public class UserAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public R<IPage<AnnouncementResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer type) {
        return R.ok(announcementService.pageUserAnnouncements(page, size, type));
    }

    @GetMapping("/{id}")
    public R<AnnouncementResult> detail(@PathVariable Long id) {
        return R.ok(announcementService.getAnnouncementDetail(id));
    }

    @GetMapping("/latest")
    public R<List<AnnouncementResult>> latest(
            @RequestParam(defaultValue = "5") int limit) {
        return R.ok(announcementService.getLatestAnnouncements(limit));
    }
}
