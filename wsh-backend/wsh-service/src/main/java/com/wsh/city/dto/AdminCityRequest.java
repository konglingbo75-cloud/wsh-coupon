package com.wsh.city.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 管理员城市请求
 */
@Data
public class AdminCityRequest {
    
    /** 城市ID（更新时必填） */
    private Long cityId;
    
    /** 城市编码 */
    @NotBlank(message = "城市编码不能为空")
    private String cityCode;
    
    /** 城市名称 */
    @NotBlank(message = "城市名称不能为空")
    private String cityName;
    
    /** 省份名称 */
    private String provinceName;
    
    /** 拼音首字母 */
    private String pinyin;
    
    /** 城市级别 1:一线 2:新一线 3:二线 4:三线 */
    @NotNull(message = "城市级别不能为空")
    private Integer level;
    
    /** 城市中心经度 */
    private BigDecimal longitude;
    
    /** 城市中心纬度 */
    private BigDecimal latitude;
    
    /** 排序权重 */
    private Integer sortOrder;
    
    /** 状态 0:未开放 1:已开放 */
    private Integer status;
    
    /** 开通日期 */
    private LocalDate openDate;
}
