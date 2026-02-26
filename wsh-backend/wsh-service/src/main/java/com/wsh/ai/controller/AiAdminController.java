package com.wsh.ai.controller;

import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import com.wsh.ai.dto.*;
import com.wsh.ai.service.AiAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/admin/ai")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AiAdminController {

    private final AiAdminService aiAdminService;

    /**
     * 获取AI模型配置列表
     */
    @GetMapping("/models")
    public R<PageResult<AiModelConfigResponse>> listModelConfigs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageQueryRequest pageQuery = new PageQueryRequest();
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return R.ok(aiAdminService.listModelConfigs(pageQuery));
    }

    /**
     * 新增AI模型配置
     */
    @PostMapping("/models")
    public R<AiModelConfigResponse> createModelConfig(@Valid @RequestBody AiModelConfigRequest request) {
        return R.ok(aiAdminService.createModelConfig(request));
    }

    /**
     * 更新AI模型配置
     */
    @PutMapping("/models/{id}")
    public R<AiModelConfigResponse> updateModelConfig(
            @PathVariable("id") Long configId,
            @Valid @RequestBody AiModelConfigRequest request) {
        return R.ok(aiAdminService.updateModelConfig(configId, request));
    }

    /**
     * 删除AI模型配置
     */
    @DeleteMapping("/models/{id}")
    public R<Void> deleteModelConfig(@PathVariable("id") Long configId) {
        aiAdminService.deleteModelConfig(configId);
        return R.ok();
    }

    /**
     * 设为默认模型
     */
    @PutMapping("/models/{id}/default")
    public R<Void> setDefaultModel(@PathVariable("id") Long configId) {
        aiAdminService.setDefaultModel(configId);
        return R.ok();
    }

    /**
     * 获取AI调用日统计
     */
    @GetMapping("/usage/daily")
    public R<PageResult<AiUsageDailyResponse>> listDailyUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "30") Integer size) {
        PageQueryRequest pageQuery = new PageQueryRequest();
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return R.ok(aiAdminService.listDailyUsage(startDate, endDate, pageQuery));
    }

    /**
     * 获取AI调用汇总统计
     */
    @GetMapping("/usage/summary")
    public R<AiUsageSummaryResponse> getUsageSummary() {
        return R.ok(aiAdminService.getUsageSummary());
    }
}
