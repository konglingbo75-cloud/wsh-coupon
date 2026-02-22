package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM tb_order WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户订单列表
     */
    @Select("SELECT * FROM tb_order WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询商户订单列表
     */
    @Select("SELECT * FROM tb_order WHERE merchant_id = #{merchantId} ORDER BY created_at DESC")
    List<Order> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询超时待支付订单（创建超过指定分钟数仍未支付）
     */
    @Select("SELECT * FROM tb_order WHERE status = 0 AND created_at < #{expireTime}")
    List<Order> selectExpiredOrders(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 更新订单状态
     */
    @Update("UPDATE tb_order SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    /**
     * 支付成功更新
     */
    @Update("UPDATE tb_order SET status = 1, pay_time = #{payTime}, transaction_id = #{transactionId}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE order_no = #{orderNo} AND status = 0")
    int updatePaySuccess(@Param("orderNo") String orderNo, 
                         @Param("payTime") LocalDateTime payTime, 
                         @Param("transactionId") String transactionId);

    /**
     * 统计用户已支付订单数
     */
    @Select("SELECT COUNT(*) FROM tb_order WHERE user_id = #{userId} AND status = 1")
    int countPaidOrdersByUserId(@Param("userId") Long userId);

    /**
     * 统计商户订单总金额
     */
    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM tb_order WHERE merchant_id = #{merchantId} AND status = 1")
    java.math.BigDecimal sumPaidAmountByMerchantId(@Param("merchantId") Long merchantId);
}
