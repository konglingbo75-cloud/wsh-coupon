package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_onboarding_fee_plan")
public class OnboardingFeePlan {

    @TableId(type = IdType.ASSIGN_ID)
    private Long planId;

    private String planName;
    private Integer planType;
    private BigDecimal feeAmount;
    private Integer durationMonths;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
