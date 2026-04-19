package dev.xuya.ldcshop.service;

import dev.xuya.ldcshop.results.DashboardResult;

/**
 * 统计服务接口 / Statistics Service Interface
 * 提供管理端数据看板所需的所有统计数据
 *
 * @author xuya
 */
public interface StatisticsService {

    /**
     * 获取数据看板统计 / Get dashboard statistics
     *
     * @return 看板数据 / Dashboard data
     */
    DashboardResult getDashboard();
}
