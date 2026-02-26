package com.wsh.city.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员城市响应
 */
@Data
@Builder
public class AdminCityResponse {
    
    private Long cityId;
    private String cityCode;
    private String cityName;
    private String provinceName;
    private String pinyin;
    private Integer level;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer merchantCount;
    private Integer activityCount;
    private Integer status;
    private Integer sortOrder;
    private LocalDate openDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
