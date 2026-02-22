package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.MerchantOnboardingFee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MerchantOnboardingFeeMapper extends BaseMapper<MerchantOnboardingFee> {

    /**
     * 查询指定日期到期的入驻费记录
     */
    @Select("SELECT * FROM tb_merchant_onboarding_fee WHERE pay_status = 1 AND valid_end_date = #{date}")
    List<MerchantOnboardingFee> selectByExpireDate(@Param("date") LocalDate date);

    /**
     * 查询已过期但商户未冻结的记录
     */
    @Select("SELECT f.* FROM tb_merchant_onboarding_fee f " +
            "JOIN tb_merchant m ON f.merchant_id = m.merchant_id " +
            "WHERE f.pay_status = 1 AND f.valid_end_date < #{today} AND m.status = 1")
    List<MerchantOnboardingFee> selectExpiredWithActiveMerchant(@Param("today") LocalDate today);
}
