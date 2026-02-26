package com.wsh.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private Long messageId;
    private Long conversationId;
    private String role;
    private String content;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal cost;
    private LocalDateTime createdAt;
}
