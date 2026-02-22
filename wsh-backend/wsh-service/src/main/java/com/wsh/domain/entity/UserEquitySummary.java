package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_user_equity_summary")
public class UserEquitySummary {

    @TableId(type = IdType.ASSIGN_ID)
    private Long summaryId;

    private Long userId;
    private BigDecimal totalPointsValue;
    private BigDecimal totalBalance;
    private BigDecimal totalVoucherValue;
    private BigDecimal expiringPointsValue;
    private Integer expiringVoucherCount;
    private Integer merchantCount;
    private LocalDateTime lastUpdated;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
