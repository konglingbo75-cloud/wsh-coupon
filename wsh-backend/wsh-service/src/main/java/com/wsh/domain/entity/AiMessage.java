package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI对话消息实体
 */
@Data
@TableName("tb_ai_message")
public class AiMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long messageId;

    private Long conversationId;
    private Long userId;

    /** 角色：user/assistant/system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 输入token数 */
    private Integer inputTokens;

    /** 输出token数 */
    private Integer outputTokens;

    /** 本条消息费用 */
    private BigDecimal cost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
