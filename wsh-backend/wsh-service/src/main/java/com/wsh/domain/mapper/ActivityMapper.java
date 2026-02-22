package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 查询指定商户的进行中活动
     */
    @Select("SELECT * FROM tb_activity WHERE merchant_id = #{merchantId} AND status = 1 " +
            "ORDER BY sort_order ASC, created_at DESC")
    List<Activity> selectActiveByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询所有公开且进行中的活动（同城活动广场）
     */
    @Select("SELECT * FROM tb_activity WHERE is_public = 1 AND status = 1 " +
            "ORDER BY sort_order ASC, created_at DESC")
    List<Activity> selectPublicActivities();

    /**
     * 按城市查询公开活动（同城活动广场）
     */
    @Select("SELECT a.* FROM tb_activity a " +
            "INNER JOIN tb_merchant m ON a.merchant_id = m.merchant_id " +
            "WHERE a.is_public = 1 AND a.status = 1 AND m.city = #{city} AND m.status = 1 " +
            "ORDER BY a.sort_order ASC, a.created_at DESC")
    List<Activity> selectPublicActivitiesByCity(@Param("city") String city);

    /**
     * 按活动类型查询公开活动
     */
    @Select("SELECT a.* FROM tb_activity a " +
            "INNER JOIN tb_merchant m ON a.merchant_id = m.merchant_id " +
            "WHERE a.is_public = 1 AND a.status = 1 AND a.activity_type = #{activityType} " +
            "AND m.city = #{city} AND m.status = 1 " +
            "ORDER BY a.sort_order ASC, a.created_at DESC")
    List<Activity> selectPublicActivitiesByCityAndType(@Param("city") String city, @Param("activityType") Integer activityType);

    /**
     * 按商户ID列表查询进行中的活动
     */
    @Select("<script>" +
            "SELECT * FROM tb_activity WHERE status = 1 " +
            "AND merchant_id IN " +
            "<foreach collection='merchantIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY sort_order ASC, created_at DESC" +
            "</script>")
    List<Activity> selectActiveByMerchantIds(@Param("merchantIds") List<Long> merchantIds);

    /**
     * 查询指定商户的活动（按类型筛选）
     */
    @Select("SELECT * FROM tb_activity WHERE merchant_id = #{merchantId} AND status = 1 " +
            "AND activity_type = #{activityType} ORDER BY sort_order ASC, created_at DESC")
    List<Activity> selectActiveByMerchantIdAndType(@Param("merchantId") Long merchantId, 
                                                    @Param("activityType") Integer activityType);

    /**
     * 查询需要同步的商户活动（用于定时任务）
     */
    @Select("SELECT DISTINCT merchant_id FROM tb_activity WHERE status = 1")
    List<Long> selectDistinctMerchantIds();

    /**
     * 统计商户的进行中活动数量
     */
    @Select("SELECT COUNT(*) FROM tb_activity WHERE merchant_id = #{merchantId} AND status = 1")
    int countActiveByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 增加已售数量
     */
    @Update("UPDATE tb_activity SET sold_count = COALESCE(sold_count, 0) + 1, " +
            "updated_at = CURRENT_TIMESTAMP WHERE activity_id = #{activityId}")
    int incrementSoldCount(@Param("activityId") Long activityId);

    /**
     * 减少已售数量（退款时使用）
     */
    @Update("UPDATE tb_activity SET sold_count = GREATEST(COALESCE(sold_count, 0) - 1, 0), " +
            "updated_at = CURRENT_TIMESTAMP WHERE activity_id = #{activityId}")
    int decrementSoldCount(@Param("activityId") Long activityId);
}
