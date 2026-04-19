package dev.xuya.ldcshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.ldcshop.entity.ShopSetting;
import dev.xuya.ldcshop.mapper.ShopSettingMapper;
import dev.xuya.ldcshop.service.ShopSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现 / Shop Setting Service Implementation
 *
 * @author xuya
 */
@Service
@RequiredArgsConstructor
public class ShopSettingServiceImpl implements ShopSettingService {

    private final ShopSettingMapper shopSettingMapper;

    @Override
    public Map<String, String> getAllSettings() {
        List<ShopSetting> settings = shopSettingMapper.selectList(null);
        Map<String, String> map = new HashMap<>();
        for (ShopSetting setting : settings) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }

    @Override
    public String getSetting(String key) {
        ShopSetting setting = shopSettingMapper.selectOne(
                new LambdaQueryWrapper<ShopSetting>().eq(ShopSetting::getSettingKey, key));
        return setting != null ? setting.getSettingValue() : null;
    }

    @Override
    public void updateSetting(String key, String value) {
        ShopSetting setting = shopSettingMapper.selectOne(
                new LambdaQueryWrapper<ShopSetting>().eq(ShopSetting::getSettingKey, key));
        if (setting != null) {
            setting.setSettingValue(value);
            shopSettingMapper.updateById(setting);
        }
    }

    @Override
    public void batchUpdateSettings(Map<String, String> settings) {
        settings.forEach(this::updateSetting);
    }
}
