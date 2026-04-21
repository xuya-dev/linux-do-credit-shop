package dev.xuya.ldcshop.params;

import lombok.Data;

import java.util.Map;

/**
 * 管理端设置更新参数 / Admin Settings Update Params
 * 批量更新系统配置时的请求体
 *
 * @author xuya
 */
@Data
public class AdminSettingsUpdateParams {

    /** 配置项键值对 / Settings key-value pairs */
    private Map<String, String> settings;
}
