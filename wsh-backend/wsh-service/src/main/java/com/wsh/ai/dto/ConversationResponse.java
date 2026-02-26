package com.wsh.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConversationResponse {
    private Long conversationId;
    private String title;
    private Integer messageCount;
    private Integer totalTokens;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
