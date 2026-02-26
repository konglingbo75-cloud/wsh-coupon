package com.wsh.city.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.city.dto.AdminCityRequest;
import com.wsh.city.dto.AdminCityResponse;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.OpenCity;
import com.wsh.domain.mapper.OpenCityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员城市服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCityService {

    private final OpenCityMapper openCityMapper;
    private final CityService cityService;

    /**
     * 获取城市列表（分页）
     */
    public PageResult<AdminCityResponse> getCityList(int page, int size, String keyword, Integer status) {
        Page<OpenCity> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<OpenCity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OpenCity::getCityName, keyword)
                    .or().like(OpenCity::getCityCode, keyword)
                    .or().like(OpenCity::getProvinceName, keyword));
        }
        if (status != null) {
            wrapper.eq(OpenCity::getStatus, status);
        }
        wrapper.orderByDesc(OpenCity::getSortOrder)
               .orderByDesc(OpenCity::getMerchantCount);

        Page<OpenCity> result = openCityMapper.selectPage(pageParam, wrapper);

        List<AdminCityResponse> items = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResult.of(items, result.getTotal(), page, size);
    }

    /**
     * 获取城市详情
     */
    public AdminCityResponse getCityDetail(Long cityId) {
        OpenCity city = openCityMapper.selectById(cityId);
        if (city == null) {
            throw new BusinessException("城市不存在");
        }
        return toResponse(city);
    }

    /**
     * 创建城市
     */
    @Transactional
    public AdminCityResponse createCity(AdminCityRequest request) {
        // 检查城市编码是否已存在
        OpenCity existing = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getCityCode, request.getCityCode())
        );
        if (existing != null) {
            throw new BusinessException("城市编码已存在");
        }

        // 检查城市名称是否已存在
        existing = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getCityName, request.getCityName())
        );
        if (existing != null) {
            throw new BusinessException("城市名称已存在");
        }

        OpenCity city = new OpenCity();
        city.setCityCode(request.getCityCode());
        city.setCityName(request.getCityName());
        city.setProvinceName(request.getProvinceName());
        city.setPinyin(request.getPinyin());
        city.setLevel(request.getLevel());
        city.setLongitude(request.getLongitude());
        city.setLatitude(request.getLatitude());
        city.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        city.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        city.setOpenDate(request.getOpenDate() != null ? request.getOpenDate() : LocalDate.now());
        city.setMerchantCount(0);
        city.setActivityCount(0);

        openCityMapper.insert(city);

        // 清除缓存
        cityService.clearCache();

        log.info("创建城市成功: {}", city.getCityName());
        return toResponse(city);
    }

    /**
     * 更新城市
     */
    @Transactional
    public AdminCityResponse updateCity(Long cityId, AdminCityRequest request) {
        OpenCity city = openCityMapper.selectById(cityId);
        if (city == null) {
            throw new BusinessException("城市不存在");
        }

        // 检查城市编码是否重复（排除自身）
        OpenCity existing = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getCityCode, request.getCityCode())
                        .ne(OpenCity::getCityId, cityId)
        );
        if (existing != null) {
            throw new BusinessException("城市编码已被其他城市使用");
        }

        // 检查城市名称是否重复（排除自身）
        existing = openCityMapper.selectOne(
                new LambdaQueryWrapper<OpenCity>()
                        .eq(OpenCity::getCityName, request.getCityName())
                        .ne(OpenCity::getCityId, cityId)
        );
        if (existing != null) {
            throw new BusinessException("城市名称已被其他城市使用");
        }

        city.setCityCode(request.getCityCode());
        city.setCityName(request.getCityName());
        city.setProvinceName(request.getProvinceName());
        city.setPinyin(request.getPinyin());
        city.setLevel(request.getLevel());
        city.setLongitude(request.getLongitude());
        city.setLatitude(request.getLatitude());
        if (request.getSortOrder() != null) {
            city.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            city.setStatus(request.getStatus());
        }
        if (request.getOpenDate() != null) {
            city.setOpenDate(request.getOpenDate());
        }

        openCityMapper.updateById(city);

        // 清除缓存
        cityService.clearCache();

        log.info("更新城市成功: {}", city.getCityName());
        return toResponse(city);
    }

    /**
     * 更新城市状态
     */
    @Transactional
    public void updateCityStatus(Long cityId, Integer status) {
        OpenCity city = openCityMapper.selectById(cityId);
        if (city == null) {
            throw new BusinessException("城市不存在");
        }

        city.setStatus(status);
        if (status == 1 && city.getOpenDate() == null) {
            city.setOpenDate(LocalDate.now());
        }
        openCityMapper.updateById(city);

        // 清除缓存
        cityService.clearCache();

        log.info("更新城市状态成功: {} -> {}", city.getCityName(), status == 1 ? "已开放" : "未开放");
    }

    private AdminCityResponse toResponse(OpenCity city) {
        return AdminCityResponse.builder()
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
                .sortOrder(city.getSortOrder())
                .openDate(city.getOpenDate())
                .createdAt(city.getCreatedAt())
                .updatedAt(city.getUpdatedAt())
                .build();
    }
}
