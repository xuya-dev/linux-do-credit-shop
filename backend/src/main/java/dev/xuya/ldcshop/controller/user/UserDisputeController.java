package dev.xuya.ldcshop.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.DisputeCreateParams;
import dev.xuya.ldcshop.results.DisputeResult;
import dev.xuya.ldcshop.service.DisputeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端-争议控制器 / User Dispute Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/user/disputes")
@RequiredArgsConstructor
@Tag(name = "用户端-争议 / User Disputes")
public class UserDisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody DisputeCreateParams params) {
        return R.ok(disputeService.createDispute(params));
    }

    @GetMapping
    public R<IPage<DisputeResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return R.ok(disputeService.pageUserDisputes(page, size, status));
    }

    @GetMapping("/{id}")
    public R<DisputeResult> detail(@PathVariable Long id) {
        return R.ok(disputeService.getDisputeDetail(id));
    }
}
