package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.DisputeHandleParams;
import dev.xuya.ldcshop.results.DisputeResult;
import dev.xuya.ldcshop.service.DisputeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-争议管理控制器 / Admin Dispute Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-争议 / Admin Disputes")
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public R<IPage<DisputeResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return R.ok(disputeService.pageAdminDisputes(page, size, status));
    }

    @GetMapping("/{id}")
    public R<DisputeResult> detail(@PathVariable Long id) {
        return R.ok(disputeService.getDisputeDetail(id));
    }

    /**
     * 处理争议 / Handle dispute
     */
    @PutMapping("/{id}/handle")
    public R<Void> handle(@PathVariable Long id, @RequestBody DisputeHandleParams params) {
        disputeService.handleDispute(id, params);
        return R.ok();
    }
}
