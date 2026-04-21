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
     * 获取所有配置（敏感值脱敏）/ Get all settings (sensitive values masked)
     */
    Map<String, String> getAllSettingsMasked();

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

    /**
     * 获取配置值，优先从数据库读取，为空则使用默认值
     * Get setting value, DB first, fallback to default
     *
     * @param key          配置键 / Setting key
     * @param defaultValue 默认值 / Default value
     * @return 配置值 / Setting value
     */
    String getSettingOrDefault(String key, String defaultValue);
}
