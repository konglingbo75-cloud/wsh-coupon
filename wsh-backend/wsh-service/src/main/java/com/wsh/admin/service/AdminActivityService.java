package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.AdminActivityListResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.Activity;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.mapper.ActivityMapper;
import com.wsh.domain.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityService {

    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;

    public PageResult<AdminActivityListResponse> listActivities(PageQueryRequest query) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(Activity::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Activity::getActivityName, query.getKeyword());
        }
        wrapper.orderByDesc(Activity::getCreatedAt);

        Page<Activity> page = new Page<>(query.getPage(), query.getSize());
        Page<Activity> result = activityMapper.selectPage(page, wrapper);

        List<AdminActivityListResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    private AdminActivityListResponse toResponse(Activity a) {
        Merchant merchant = merchantMapper.selectById(a.getMerchantId());

        return AdminActivityListResponse.builder()
                .activityId(a.getActivityId())
                .merchantId(a.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : "未知商户")
                .activityType(a.getActivityType())
                .activityName(a.getActivityName())
                .status(a.getStatus())
                .stock(a.getStock())
                .soldCount(a.getSoldCount())
                .isPublic(a.getIsPublic())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
