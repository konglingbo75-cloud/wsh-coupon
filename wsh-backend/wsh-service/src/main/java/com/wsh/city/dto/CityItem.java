package com.wsh.city.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市列表项
 */
@Data
@Builder
public class CityItem {
    
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
}
