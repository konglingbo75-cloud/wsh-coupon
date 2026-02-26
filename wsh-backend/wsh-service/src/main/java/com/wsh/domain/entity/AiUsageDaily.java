package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI调用日账单实体
 */
@Data
@TableName("tb_ai_usage_daily")
public class AiUsageDaily {

    @TableId(type = IdType.ASSIGN_ID)
    private Long dailyId;

    /** 统计日期 */
    private LocalDate statDate;

    /** 模型配置ID */
    private Long modelConfigId;

    /** 调用次数 */
    private Integer callCount;

    /** 总输入token */
    private Integer totalInputTokens;

    /** 总输出token */
    private Integer totalOutputTokens;

    /** 总费用 */
    private BigDecimal totalCost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
