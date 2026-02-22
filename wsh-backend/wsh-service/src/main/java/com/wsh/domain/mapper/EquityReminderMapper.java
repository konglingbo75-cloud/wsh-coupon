package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.EquityReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface EquityReminderMapper extends BaseMapper<EquityReminder> {

    /**
     * 查询用户的提醒消息列表（按创建时间倒序）
     */
    @Select("SELECT * FROM tb_equity_reminder WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<EquityReminder> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待发送的提醒
     */
    @Select("SELECT * FROM tb_equity_reminder WHERE remind_status = 0 ORDER BY created_at ASC")
    List<EquityReminder> selectPending();

    /**
     * 检查是否已存在相同类型的提醒（避免重复）
     */
    @Select("SELECT COUNT(*) FROM tb_equity_reminder WHERE user_id = #{userId} " +
            "AND merchant_id = #{merchantId} AND reminder_type = #{reminderType} " +
            "AND expire_date = #{expireDate}")
    int countDuplicate(@Param("userId") Long userId,
                       @Param("merchantId") Long merchantId,
                       @Param("reminderType") Integer reminderType,
                       @Param("expireDate") LocalDate expireDate);
}
