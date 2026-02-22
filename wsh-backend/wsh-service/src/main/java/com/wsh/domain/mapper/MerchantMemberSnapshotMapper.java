package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MerchantMemberSnapshotMapper extends BaseMapper<MerchantMemberSnapshot> {

    /**
     * 查询指定用户在所有商户的会员快照
     */
    @Select("SELECT * FROM tb_merchant_member_snapshot WHERE user_id = #{userId} AND sync_status = 1")
    List<MerchantMemberSnapshot> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询7天内积分即将过期的快照
     */
    @Select("SELECT * FROM tb_merchant_member_snapshot WHERE sync_status = 1 " +
            "AND points_expire_date IS NOT NULL AND points_expire_date BETWEEN #{today} AND #{endDate} " +
            "AND points_value > 0")
    List<MerchantMemberSnapshot> selectExpiringPoints(@Param("today") LocalDate today,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 查询指定商户的所有会员快照（用于商户端分析）
     */
    @Select("SELECT * FROM tb_merchant_member_snapshot WHERE merchant_id = #{merchantId} AND sync_status = 1")
    List<MerchantMemberSnapshot> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询用户在指定商户的会员快照
     */
    @Select("SELECT * FROM tb_merchant_member_snapshot WHERE user_id = #{userId} AND merchant_id = #{merchantId}")
    MerchantMemberSnapshot selectByUserAndMerchant(@Param("userId") Long userId, @Param("merchantId") Long merchantId);
}
