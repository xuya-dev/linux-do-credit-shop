package dev.xuya.ldcshop.common.util;

import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;

/**
 * 实体工具类 / Entity Utility
 * 提供通用的实体校验方法
 *
 * @author xuya
 */
public final class EntityUtil {

    private EntityUtil() {}

    /**
     * 校验实体非空，为空则抛出业务异常
     * Require entity non-null, throw BusinessException if null
     *
     * @param entity 待校验实体 / Entity to check
     * @param code   错误码 / Error code
     * @return 非空实体 / Non-null entity
     */
    public static <T> T requireNonNull(T entity, ResultCode code) {
        if (entity == null) {
            throw new BusinessException(code);
        }
        return entity;
    }
}
