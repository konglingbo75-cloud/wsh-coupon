package com.wsh.city.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.city.dto.CityItem;
import com.wsh.city.dto.CityListResponse;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.OpenCity;
import com.wsh.domain.mapper.OpenCityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 城市服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

    private final OpenCityMapper openCityMapper;
    private final RedisUtil redisUtil;

    private static final String CACHE_KEY_CITIES = "wsh:cities:list";
    private static final long CACHE_TTL = 7 * 24 * 60 * 60; // 7天

    /**
     * 获取城市列表
     */
    public CityListResponse getCityList() {
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        CityListResponse cached = (CityListResponse) redisUtil.get(CACHE_KEY_CITIES);
        if (cached != null) {
            return cached;
        }

        // 查询已开放的城市
        List<OpenCity> cities = openCityMapper.selectList(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getStatus, 1)
                        .orderByDesc(OpenCity::getSortOrder)
                        .orderByDesc(OpenCity::getMerchantCount)
        );

        // 转换为DTO
        List<CityItem> cityItems = cities.stream()
                .map(this::toCityItem)
                .collect(Collectors.toList());

        // 热门城市（前8个）
        List<CityItem> hotCities = cityItems.stream()
                .limit(8)
                .collect(Collectors.toList());

        // 按拼音分组
        Map<String, List<CityItem>> allCities = cityItems.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getPinyin() != null ? c.getPinyin().toUpperCase() : "#",
                        TreeMap::new,
                        Collectors.toList()
                ));

        CityListResponse response = CityListResponse.builder()
                .hotCities(hotCities)
                .allCities(allCities)
                .total(cityItems.size())
                .build();

        // 缓存结果
        redisUtil.set(CACHE_KEY_CITIES, response, CACHE_TTL, java.util.concurrent.TimeUnit.SECONDS);

        return response;
    }

    /**
     * GPS定位识别城市
     * 使用Haversine公式计算距离
     */
    public CityItem locateCity(BigDecimal longitude, BigDecimal latitude) {
        List<OpenCity> cities = openCityMapper.selectList(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getStatus, 1)
                        .isNotNull(OpenCity::getLongitude)
                        .isNotNull(OpenCity::getLatitude)
        );

        if (cities.isEmpty()) {
            return getDefaultCity();
        }

        OpenCity nearestCity = null;
        double minDistance = Double.MAX_VALUE;

        for (OpenCity city : cities) {
            double distance = calculateDistance(
                    latitude.doubleValue(), longitude.doubleValue(),
                    city.getLatitude().doubleValue(), city.getLongitude().doubleValue()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearestCity = city;
            }
        }

        // 如果最近的城市距离超过200公里，返回默认城市
        if (minDistance > 200) {
            log.info("最近城市距离{}公里，超过200公里，返回默认城市", minDistance);
            return getDefaultCity();
        }

        return nearestCity != null ? toCityItem(nearestCity) : getDefaultCity();
    }

    /**
     * 根据城市名称获取城市
     */
    public CityItem getCityByName(String cityName) {
        OpenCity city = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getCityName, cityName)
                        .eq(OpenCity::getStatus, 1)
        );
        return city != null ? toCityItem(city) : null;
    }

    /**
     * 获取默认城市（深圳）
     */
    public CityItem getDefaultCity() {
        OpenCity city = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getCityName, "深圳")
                        .eq(OpenCity::getStatus, 1)
        );
        if (city != null) {
            return toCityItem(city);
        }
        // 如果深圳不存在，返回第一个城市
        OpenCity firstCity = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getStatus, 1)
                        .orderByDesc(OpenCity::getSortOrder)
                        .last("LIMIT 1")
        );
        return firstCity != null ? toCityItem(firstCity) : null;
    }

    /**
     * 清除城市缓存
     */
    public void clearCache() {
        redisUtil.delete(CACHE_KEY_CITIES);
    }

    /**
     * Haversine公式计算两点间距离（公里）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private CityItem toCityItem(OpenCity city) {
        return CityItem.builder()
                .cityId(city.getCityId())
                .cityCode(city.getCityCode())
                .cityName(city.getCityName())
                .provinceName(city.getProvinceName())
                .pinyin(city.getPinyin())
                .level(city.getLevel())
                .longitude(city.getLongitude())
                .latitude(city.getLatitude())
                .merchantCount(city.getMerchantCount())
                .activityCount(city.getActivityCount())
                .status(city.getStatus())
                .build();
    }
}
