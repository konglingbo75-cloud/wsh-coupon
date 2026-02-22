package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 查询即将过期且未使用的券（用于提醒扫描）
     */
    @Select("SELECT * FROM tb_voucher WHERE status = 0 " +
            "AND valid_end_time IS NOT NULL AND valid_end_time BETWEEN #{start} AND #{end}")
    List<Voucher> selectExpiringVouchers(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * 查询用户的未使用券列表
     */
    @Select("SELECT * FROM tb_voucher WHERE user_id = #{userId} AND status = 0 ORDER BY valid_end_time ASC")
    List<Voucher> selectUnusedByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询券码
     */
    @Select("SELECT * FROM tb_voucher WHERE order_id = #{orderId}")
    Voucher selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据券码查询
     */
    @Select("SELECT * FROM tb_voucher WHERE voucher_code = #{voucherCode}")
    Voucher selectByVoucherCode(@Param("voucherCode") String voucherCode);

    /**
     * 查询商户的券码列表
     */
    @Select("SELECT * FROM tb_voucher WHERE merchant_id = #{merchantId} ORDER BY created_at DESC")
    List<Voucher> selectByMerchantId(@Param("merchantId") Long merchantId);
}
