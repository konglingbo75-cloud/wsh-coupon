package com.wsh.city.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GPS定位请求
 */
@Data
public class LocateCityRequest {
    
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;
    
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;
}
