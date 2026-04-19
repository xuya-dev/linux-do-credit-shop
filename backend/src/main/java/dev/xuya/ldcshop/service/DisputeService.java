package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.params.DisputeCreateParams;
import dev.xuya.ldcshop.params.DisputeHandleParams;
import dev.xuya.ldcshop.results.DisputeResult;

/**
 * 争议服务接口 / Dispute Service Interface
 *
 * @author xuya
 */
public interface DisputeService {

    /**
     * 创建争议 / Create dispute
     */
    Long createDispute(DisputeCreateParams params);

    /**
     * 获取争议详情 / Get dispute detail
     */
    DisputeResult getDisputeDetail(Long id);

    /**
     * 用户端分页查询争议 / User side page query disputes
     */
    IPage<DisputeResult> pageUserDisputes(int page, int size, Integer status);

    /**
     * 管理端分页查询争议 / Admin side page query disputes
     */
    IPage<DisputeResult> pageAdminDisputes(int page, int size, Integer status);

    /**
     * 管理员处理争议 / Admin handle dispute
     */
    void handleDispute(Long id, DisputeHandleParams params);
}
