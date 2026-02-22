package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.ProfitSharing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProfitSharingMapper extends BaseMapper<ProfitSharing> {

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM tb_profit_sharing WHERE order_no = #{orderNo}")
    ProfitSharing selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据券码ID查询
     */
    @Select("SELECT * FROM tb_profit_sharing WHERE voucher_id = #{voucherId}")
    ProfitSharing selectByVoucherId(@Param("voucherId") Long voucherId);

    /**
     * 查询待分账记录
     */
    @Select("SELECT * FROM tb_profit_sharing WHERE status = 0")
    List<ProfitSharing> selectPendingSharing();

    /**
     * 查询分账失败且可重试的记录
     */
    @Select("SELECT * FROM tb_profit_sharing WHERE status = 3 AND retry_count < #{maxRetry}")
    List<ProfitSharing> selectFailedRetryable(@Param("maxRetry") int maxRetry);

    /**
     * 更新分账状态
     */
    @Update("UPDATE tb_profit_sharing SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE sharing_id = #{sharingId}")
    int updateStatus(@Param("sharingId") Long sharingId, @Param("status") Integer status);

    /**
     * 更新核销触发分账
     */
    @Update("UPDATE tb_profit_sharing SET status = 1, verify_record_id = #{verifyRecordId}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE voucher_id = #{voucherId} AND status = 0")
    int updateVerifyTriggered(@Param("voucherId") Long voucherId, @Param("verifyRecordId") Long verifyRecordId);

    /**
     * 增加重试次数
     */
    @Update("UPDATE tb_profit_sharing SET retry_count = retry_count + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE sharing_id = #{sharingId}")
    int incrementRetryCount(@Param("sharingId") Long sharingId);
}
