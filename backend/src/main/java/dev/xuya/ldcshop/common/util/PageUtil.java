package dev.xuya.ldcshop.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 分页工具类 / Page Utility
 * 提供通用的分页转换方法
 *
 * @author xuya
 */
public final class PageUtil {

    private PageUtil() {}

    /**
     * 将源分页转换为新的类型分页 / Convert source page to new typed page
     *
     * @param source  源分页 / Source page
     * @param records 转换后的记录列表 / Converted records
     * @return 新的分页结果 / New page result
     */
    public static <T> IPage<T> convertPage(IPage<?> source, List<T> records) {
        Page<T> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return result;
    }
}
