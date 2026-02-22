package com.wsh.integration.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 从商户系统同步的会员数据
 */
@Data
@Builder
public class MemberDataDTO {

    private String sourceMemberId;
    private String memberLevelName;
    private Integer points;
    private BigDecimal pointsValue;
    private LocalDate pointsExpireDate;
    private BigDecimal balance;
    private Integer consumeCount;
    private BigDecimal totalConsumeAmount;
    private LocalDateTime lastConsumeTime;
    /** 沉睡等级：0活跃 1轻度 2中度 3深度 */
    private Integer dormancyLevel;
}
