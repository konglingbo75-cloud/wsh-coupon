package com.wsh.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.AiModelConfig;
import com.wsh.domain.entity.AiUsageDaily;
import com.wsh.domain.mapper.AiModelConfigMapper;
import com.wsh.domain.mapper.AiUsageDailyMapper;
import com.wsh.ai.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAdminService {

    private final AiModelConfigMapper modelConfigMapper;
    private final AiUsageDailyMapper usageDailyMapper;

    /**
     * 获取AI模型配置列表
     */
    public PageResult<AiModelConfigResponse> listModelConfigs(PageQueryRequest pageQuery) {
        Page<AiModelConfig> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        
        IPage<AiModelConfig> result = modelConfigMapper.selectPage(page,
                new LambdaQueryWrapper<AiModelConfig>()
                        .orderByDesc(AiModelConfig::getIsDefault)
                        .orderByDesc(AiModelConfig::getCreatedAt)
        );
        
        List<AiModelConfigResponse> list = result.getRecords().stream()
                .map(this::toModelConfigResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 新增AI模型配置
     */
    @Transactional
    public AiModelConfigResponse createModelConfig(AiModelConfigRequest request) {
        AiModelConfig config = new AiModelConfig();
        BeanUtils.copyProperties(request, config);
        
        // 如果设为默认，先取消其他默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefaultModel();
        }
        
        modelConfigMapper.insert(config);
        log.info("新增AI模型配置, configId={}, model={}", config.getConfigId(), config.getModelName());
        
        return toModelConfigResponse(config);
    }

    /**
     * 更新AI模型配置
     */
    @Transactional
    public AiModelConfigResponse updateModelConfig(Long configId, AiModelConfigRequest request) {
        AiModelConfig config = modelConfigMapper.selectById(configId);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }
        
        if (request.getProviderCode() != null) config.setProviderCode(request.getProviderCode());
        if (request.getProviderName() != null) config.setProviderName(request.getProviderName());
        if (request.getModelName() != null) config.setModelName(request.getModelName());
        if (request.getApiEndpoint() != null) config.setApiEndpoint(request.getApiEndpoint());
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            config.setApiKey(request.getApiKey());
        }
        if (request.getStatus() != null) config.setStatus(request.getStatus());
        if (request.getInputPrice() != null) config.setInputPrice(request.getInputPrice());
        if (request.getOutputPrice() != null) config.setOutputPrice(request.getOutputPrice());
        if (request.getMaxTokens() != null) config.setMaxTokens(request.getMaxTokens());
        if (request.getTemperature() != null) config.setTemperature(request.getTemperature());
        
        modelConfigMapper.updateById(config);
        log.info("更新AI模型配置, configId={}", configId);
        
        return toModelConfigResponse(config);
    }

    /**
     * 删除AI模型配置
     */
    @Transactional
    public void deleteModelConfig(Long configId) {
        AiModelConfig config = modelConfigMapper.selectById(configId);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }
        if (config.getIsDefault() == 1) {
            throw new RuntimeException("不能删除默认模型");
        }
        modelConfigMapper.deleteById(configId);
        log.info("删除AI模型配置, configId={}", configId);
    }

    /**
     * 设为默认模型
     */
    @Transactional
    public void setDefaultModel(Long configId) {
        AiModelConfig config = modelConfigMapper.selectById(configId);
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }
        if (config.getStatus() != 1) {
            throw new RuntimeException("模型未启用，无法设为默认");
        }
        
        clearDefaultModel();
        config.setIsDefault(1);
        modelConfigMapper.updateById(config);
        log.info("设置默认AI模型, configId={}", configId);
    }

    /**
     * 获取AI调用日统计
     */
    public PageResult<AiUsageDailyResponse> listDailyUsage(LocalDate startDate, LocalDate endDate, PageQueryRequest pageQuery) {
        Page<AiUsageDaily> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        
        LambdaQueryWrapper<AiUsageDaily> wrapper = new LambdaQueryWrapper<AiUsageDaily>()
                .ge(startDate != null, AiUsageDaily::getStatDate, startDate)
                .le(endDate != null, AiUsageDaily::getStatDate, endDate)
                .orderByDesc(AiUsageDaily::getStatDate);
        
        IPage<AiUsageDaily> result = usageDailyMapper.selectPage(page, wrapper);
        
        List<AiUsageDailyResponse> list = result.getRecords().stream()
                .map(this::toUsageDailyResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取AI调用汇总统计
     */
    public AiUsageSummaryResponse getUsageSummary() {
        AiUsageSummaryResponse resp = new AiUsageSummaryResponse();
        
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);
        
        // 总统计
        List<AiUsageDaily> allUsage = usageDailyMapper.selectList(null);
        int totalCallCount = 0;
        int totalInputTokens = 0;
        int totalOutputTokens = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (AiUsageDaily usage : allUsage) {
            totalCallCount += usage.getCallCount() != null ? usage.getCallCount() : 0;
            totalInputTokens += usage.getTotalInputTokens() != null ? usage.getTotalInputTokens() : 0;
            totalOutputTokens += usage.getTotalOutputTokens() != null ? usage.getTotalOutputTokens() : 0;
            totalCost = totalCost.add(usage.getTotalCost() != null ? usage.getTotalCost() : BigDecimal.ZERO);
        }
        
        resp.setTotalCallCount(totalCallCount);
        resp.setTotalInputTokens(totalInputTokens);
        resp.setTotalOutputTokens(totalOutputTokens);
        resp.setTotalCost(totalCost);
        
        // 今日统计
        Integer todayCallCount = usageDailyMapper.sumCallCount(today, today);
        BigDecimal todayCost = usageDailyMapper.sumTotalCost(today, today);
        resp.setTodayCallCount(todayCallCount != null ? todayCallCount : 0);
        resp.setTodayCost(todayCost != null ? todayCost : BigDecimal.ZERO);
        
        // 本月统计
        Integer monthCallCount = usageDailyMapper.sumCallCount(monthStart, today);
        BigDecimal monthCost = usageDailyMapper.sumTotalCost(monthStart, today);
        resp.setMonthCallCount(monthCallCount != null ? monthCallCount : 0);
        resp.setMonthCost(monthCost != null ? monthCost : BigDecimal.ZERO);
        
        return resp;
    }

    private void clearDefaultModel() {
        List<AiModelConfig> defaults = modelConfigMapper.selectList(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getIsDefault, 1)
        );
        for (AiModelConfig m : defaults) {
            m.setIsDefault(0);
            modelConfigMapper.updateById(m);
        }
    }

    private AiModelConfigResponse toModelConfigResponse(AiModelConfig config) {
        AiModelConfigResponse resp = new AiModelConfigResponse();
        BeanUtils.copyProperties(config, resp);
        // 不返回API Key
        return resp;
    }

    private AiUsageDailyResponse toUsageDailyResponse(AiUsageDaily usage) {
        AiUsageDailyResponse resp = new AiUsageDailyResponse();
        BeanUtils.copyProperties(usage, resp);
        
        // 获取模型名称
        if (usage.getModelConfigId() != null) {
            AiModelConfig config = modelConfigMapper.selectById(usage.getModelConfigId());
            if (config != null) {
                resp.setModelName(config.getModelName());
                resp.setProviderName(config.getProviderName());
            }
        }
        
        return resp;
    }
}
