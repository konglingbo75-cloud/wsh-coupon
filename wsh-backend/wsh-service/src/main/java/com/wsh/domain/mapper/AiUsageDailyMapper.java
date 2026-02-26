package com.wsh.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsh.domain.entity.AiUsageDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface AiUsageDailyMapper extends BaseMapper<AiUsageDaily> {

    @Select("SELECT COALESCE(SUM(call_count), 0) FROM tb_ai_usage_daily WHERE stat_date >= #{startDate} AND stat_date <= #{endDate}")
    Integer sumCallCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM tb_ai_usage_daily WHERE stat_date >= #{startDate} AND stat_date <= #{endDate}")
    BigDecimal sumTotalCost(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
