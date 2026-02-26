package com.wsh.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AiModelConfigResponse {
    private Long configId;
    private String providerCode;
    private String providerName;
    private String modelName;
    private String apiEndpoint;
    private Integer isDefault;
    private Integer status;
    private BigDecimal inputPrice;
    private BigDecimal outputPrice;
    private Integer maxTokens;
    private BigDecimal temperature;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
