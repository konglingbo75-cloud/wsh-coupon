package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.VerificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VerificationRecordMapper extends BaseMapper<VerificationRecord> {

    /**
     * 根据券码ID查询
     */
    @Select("SELECT * FROM tb_verification_record WHERE voucher_id = #{voucherId}")
    VerificationRecord selectByVoucherId(@Param("voucherId") Long voucherId);

    /**
     * 根据券码查询
     */
    @Select("SELECT * FROM tb_verification_record WHERE voucher_code = #{voucherCode}")
    VerificationRecord selectByVoucherCode(@Param("voucherCode") String voucherCode);

    /**
     * 查询商户核销记录
     */
    @Select("SELECT * FROM tb_verification_record WHERE merchant_id = #{merchantId} ORDER BY verify_time DESC")
    List<VerificationRecord> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询员工核销记录
     */
    @Select("SELECT * FROM tb_verification_record WHERE employee_id = #{employeeId} ORDER BY verify_time DESC")
    List<VerificationRecord> selectByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 查询待同步记录
     */
    @Select("SELECT * FROM tb_verification_record WHERE sync_status = 0")
    List<VerificationRecord> selectPendingSync();

    /**
     * 统计商户核销数量
     */
    @Select("SELECT COUNT(*) FROM tb_verification_record WHERE merchant_id = #{merchantId}")
    int countByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 统计沉睡唤醒核销数量
     */
    @Select("SELECT COUNT(*) FROM tb_verification_record WHERE merchant_id = #{merchantId} AND is_dormancy_awake = 1")
    int countDormancyAwakeByMerchantId(@Param("merchantId") Long merchantId);
}
