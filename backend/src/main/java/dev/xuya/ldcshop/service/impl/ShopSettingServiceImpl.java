package dev.xuya.ldcshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.entity.ShopSetting;
import dev.xuya.ldcshop.mapper.ShopSettingMapper;
import dev.xuya.ldcshop.service.ShopSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务实现 / System Config Service Implementation
 *
 * @author xuya
 */
@Service
@RequiredArgsConstructor
public class ShopSettingServiceImpl implements ShopSettingService {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "ldc_payment_client_secret", "ldc_payment_private_key"
    );

    private final ShopSettingMapper shopSettingMapper;
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();
    private volatile long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 30_000;

    /**
     * 获取所有配置 / Get all settings
     */
    @Override
    public Map<String, String> getAllSettings() {
        return loadFromDb();
    }

    /**
     * 获取所有配置（敏感字段脱敏）/ Get all settings with sensitive fields masked
     */
    @Override
    public Map<String, String> getAllSettingsMasked() {
        Map<String, String> settings = new HashMap<>(loadFromDb());
        for (String key : SENSITIVE_KEYS) {
            String val = settings.get(key);
            if (val != null && val.length() > 8) {
                settings.put(key, val.substring(0, 4) + "****" + val.substring(val.length() - 4));
            } else if (val != null) {
                settings.put(key, "****");
            }
        }
        return settings;
    }

    /**
     * 获取单个配置值 / Get a single setting value
     */
    @Override
    public String getSetting(String key) {
        Map<String, String> cached = loadFromDb();
        return cached.get(key);
    }

    /**
     * 获取配置值或默认值 / Get setting value or default
     */
    @Override
    public String getSettingOrDefault(String key, String defaultValue) {
        String value = getSetting(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    /**
     * 更新单个配置 / Update a single setting
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetting(String key, String value) {
        ShopSetting setting = shopSettingMapper.selectOne(
                new LambdaQueryWrapper<ShopSetting>().eq(ShopSetting::getConfigKey, key));
        if (setting != null) {
            setting.setConfigValue(value);
            shopSettingMapper.updateById(setting);
        } else {
            setting = new ShopSetting();
            setting.setConfigKey(key);
            setting.setConfigValue(value);
            shopSettingMapper.insert(setting);
        }
        invalidateCache();
    }

    /**
     * 批量更新配置 / Batch update settings
     * 自动过滤含脱敏占位符的值 / Automatically filters values containing mask placeholders
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSettings(Map<String, String> settings) {
        Map<String, String> filtered = settings.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().contains("****"))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        filtered.forEach(this::updateSetting);
        AuditLog.log("batchUpdateSettings", "updatedKeys=" + filtered.keySet());
    }

    /**
     * 从数据库加载配置（带本地缓存）/ Load settings from database (with local cache)
     */
    private Map<String, String> loadFromDb() {
        long now = System.currentTimeMillis();
        if (now - cacheTimestamp < CACHE_TTL_MS && !localCache.isEmpty()) {
            return new HashMap<>(localCache);
        }
        List<ShopSetting> settings = shopSettingMapper.selectList(null);
        Map<String, String> map = new HashMap<>();
        for (ShopSetting setting : settings) {
            map.put(setting.getConfigKey(), setting.getConfigValue());
        }
        localCache.clear();
        localCache.putAll(map);
        cacheTimestamp = now;
        return map;
    }

    /**
     * 使缓存失效 / Invalidate the cache
     */
    private void invalidateCache() {
        cacheTimestamp = 0;
        localCache.clear();
    }
}
