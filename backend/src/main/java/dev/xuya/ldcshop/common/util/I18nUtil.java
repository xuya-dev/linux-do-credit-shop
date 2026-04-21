package dev.xuya.ldcshop.common.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 国际化工具类 / i18n Utility
 * 根据 Accept-Language 请求头返回对应语言的消息
 *
 * @author xuya
 */
@Component
public class I18nUtil implements ApplicationContextAware {

    private static MessageSource messageSource;

    /**
     * 设置应用上下文，获取 MessageSource Bean / Set application context, retrieve MessageSource bean
     *
     * @param ctx 应用上下文 / Application context
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        messageSource = ctx.getBean(MessageSource.class);
    }

    /**
     * 根据 i18n key 获取国际化消息 / Get i18n message by key
     *
     * @param key 消息键 / Message key
     * @param args 格式化参数 / Format arguments
     * @return 国际化消息 / Localized message
     */
    public static String get(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
