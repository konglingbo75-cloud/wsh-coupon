package com.wsh.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiModelConfigRequest {
    
    @NotBlank(message = "服务商编码不能为空")
    @Size(max = 32, message = "服务商编码不能超过32字符")
    private String providerCode;
    
    @NotBlank(message = "服务商名称不能为空")
    @Size(max = 64, message = "服务商名称不能超过64字符")
    private String providerName;
    
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 64, message = "模型名称不能超过64字符")
    private String modelName;
    
    @Size(max = 255, message = "API端点不能超过255字符")
    private String apiEndpoint;
    
    @Size(max = 512, message = "API Key不能超过512字符")
    private String apiKey;
    
    private Integer isDefault;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    private BigDecimal inputPrice;
    private BigDecimal outputPrice;
    private Integer maxTokens;
    private BigDecimal temperature;
}
