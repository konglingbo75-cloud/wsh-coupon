package com.wsh.city.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 城市列表响应
 */
@Data
@Builder
public class CityListResponse {
    
    /** 热门城市(前8个) */
    private List<CityItem> hotCities;
    
    /** 全部城市(按拼音分组) */
    private Map<String, List<CityItem>> allCities;
    
    /** 城市总数 */
    private Integer total;
}
