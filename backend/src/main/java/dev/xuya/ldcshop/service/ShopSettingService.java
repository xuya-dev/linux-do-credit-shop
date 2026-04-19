package dev.xuya.ldcshop.service;

import java.util.Map;

/**
 * 系统配置服务接口 / Shop Setting Service Interface
 *
 * @author xuya
 */
public interface ShopSettingService {

    /**
     * 获取所有配置 / Get all settings
     */
    Map<String, String> getAllSettings();

    /**
     * 获取单个配置 / Get single setting
     */
    String getSetting(String key);

    /**
     * 更新配置 / Update setting
     */
    void updateSetting(String key, String value);

    /**
     * 批量更新配置 / Batch update settings
     */
    void batchUpdateSettings(Map<String, String> settings);
}
