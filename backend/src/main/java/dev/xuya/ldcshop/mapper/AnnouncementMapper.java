package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公告 Mapper / Announcement Mapper
 *
 * @author xuya
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 查询最新公告列表 / Query latest announcements
     *
     * @param limit 数量限制 / Limit count
     * @return 公告列表 / Announcement list
     */
    @Select("SELECT * FROM ldc_announcement WHERE status = 1 AND deleted = 0 ORDER BY is_top DESC, published_at DESC LIMIT #{limit}")
    List<Announcement> selectLatest(@Param("limit") int limit);
}
