package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型配置实体
 */
@Data
@TableName("tb_ai_model_config")
public class AiModelConfig {

    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    /** 服务商编码：openai/claude/qwen/deepseek */
    private String providerCode;

    /** 服务商名称 */
    private String providerName;

    /** 模型名称：gpt-4/claude-3/qwen-turbo */
    private String modelName;

    /** API端点URL */
    private String apiEndpoint;

    /** 加密存储的API Key */
    private String apiKey;

    /** 是否默认模型：0否 1是 */
    private Integer isDefault;

    /** 状态：0停用 1启用 */
    private Integer status;

    /** 输入价格（元/千token） */
    private BigDecimal inputPrice;

    /** 输出价格（元/千token） */
    private BigDecimal outputPrice;

    /** 最大token数 */
    private Integer maxTokens;

    /** 温度参数 */
    private BigDecimal temperature;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
