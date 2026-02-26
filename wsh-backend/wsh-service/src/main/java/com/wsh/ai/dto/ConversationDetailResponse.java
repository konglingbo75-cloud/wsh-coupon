package com.wsh.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationDetailResponse {
    private Long conversationId;
    private String title;
    private Integer messageCount;
    private Integer totalTokens;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
    private List<MessageItem> messages;

    @Data
    public static class MessageItem {
        private Long messageId;
        private String role;
        private String content;
        private Integer inputTokens;
        private Integer outputTokens;
        private LocalDateTime createdAt;
    }
}
