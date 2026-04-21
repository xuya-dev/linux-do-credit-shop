package dev.xuya.ldcshop.results;

import lombok.Data;

/**
 * 公开设置结果 / Public Settings Result
 * 前台用户可见的商店配置（不含敏感信息）
 *
 * @author xuya
 */
@Data
public class PublicSettingsResult {

    /** 商店名称 / Shop Name */
    private String shopName;

    /** 商店描述 / Shop Description */
    private String shopDescription;

    /** 商店Logo / Shop Logo URL */
    private String shopLogo;

    /** 商店公告 / Shop Notice */
    private String shopNotice;
}
