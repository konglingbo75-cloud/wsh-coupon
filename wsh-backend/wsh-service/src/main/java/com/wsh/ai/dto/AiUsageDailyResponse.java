package com.wsh.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AiUsageDailyResponse {
    private Long dailyId;
    private LocalDate statDate;
    private Long modelConfigId;
    private String modelName;
    private String providerName;
    private Integer callCount;
    private Integer totalInputTokens;
    private Integer totalOutputTokens;
    private BigDecimal totalCost;
}
