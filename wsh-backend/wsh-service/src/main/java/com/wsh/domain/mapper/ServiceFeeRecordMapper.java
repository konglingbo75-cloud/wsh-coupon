package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.ServiceFeeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ServiceFeeRecordMapper extends BaseMapper<ServiceFeeRecord> {

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM tb_service_fee_record WHERE order_no = #{orderNo}")
    ServiceFeeRecord selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询商户服务费记录
     */
    @Select("SELECT * FROM tb_service_fee_record WHERE merchant_id = #{merchantId} ORDER BY created_at DESC")
    List<ServiceFeeRecord> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询商户指定时间范围的服务费记录
     */
    @Select("SELECT * FROM tb_service_fee_record WHERE merchant_id = #{merchantId} " +
            "AND created_at >= #{startTime} AND created_at < #{endTime} ORDER BY created_at DESC")
    List<ServiceFeeRecord> selectByMerchantIdAndTimeRange(@Param("merchantId") Long merchantId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户服务费总额
     */
    @Select("SELECT COALESCE(SUM(service_fee), 0) FROM tb_service_fee_record WHERE merchant_id = #{merchantId}")
    BigDecimal sumServiceFeeByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 统计商户指定月份服务费
     */
    @Select("SELECT COALESCE(SUM(service_fee), 0) FROM tb_service_fee_record WHERE merchant_id = #{merchantId} " +
            "AND created_at >= #{startTime} AND created_at < #{endTime}")
    BigDecimal sumServiceFeeByMerchantIdAndMonth(@Param("merchantId") Long merchantId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户到账总额
     */
    @Select("SELECT COALESCE(SUM(merchant_amount), 0) FROM tb_service_fee_record WHERE merchant_id = #{merchantId}")
    BigDecimal sumMerchantAmountByMerchantId(@Param("merchantId") Long merchantId);
}
