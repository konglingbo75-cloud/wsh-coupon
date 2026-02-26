package com.wsh.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiUsageSummaryResponse {
    private Integer totalCallCount;
    private Integer totalInputTokens;
    private Integer totalOutputTokens;
    private BigDecimal totalCost;
    private Integer todayCallCount;
    private BigDecimal todayCost;
    private Integer monthCallCount;
    private BigDecimal monthCost;
}
