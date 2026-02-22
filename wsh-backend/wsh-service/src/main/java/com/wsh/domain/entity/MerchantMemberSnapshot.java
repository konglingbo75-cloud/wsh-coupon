package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_merchant_member_snapshot")
public class MerchantMemberSnapshot {

    @TableId(type = IdType.ASSIGN_ID)
    private Long snapshotId;

    private Long userId;
    private Long merchantId;
    private String sourceMemberId;
    private String memberLevelName;
    private Integer points;
    private BigDecimal pointsValue;
    private LocalDate pointsExpireDate;
    private BigDecimal balance;
    private Integer consumeCount;
    private BigDecimal totalConsumeAmount;
    private LocalDateTime lastConsumeTime;
    private Integer dormancyLevel;
    private LocalDateTime syncTime;
    private Integer syncStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
