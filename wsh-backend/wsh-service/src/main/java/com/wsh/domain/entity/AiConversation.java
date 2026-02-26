package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI对话会话实体
 */
@Data
@TableName("tb_ai_conversation")
public class AiConversation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long conversationId;

    private Long userId;

    /** 会话标题（自动从首条消息提取） */
    private String title;

    /** 使用的模型配置ID */
    private Long modelConfigId;

    /** 消息数量 */
    private Integer messageCount;

    /** 累计token数 */
    private Integer totalTokens;

    /** 累计费用（元） */
    private BigDecimal totalCost;

    /** 状态：0已删除 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
