package com.wsh.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.AdminOperationLogResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import com.wsh.domain.entity.AdminOperationLog;
import com.wsh.domain.entity.AdminUser;
import com.wsh.domain.mapper.AdminOperationLogMapper;
import com.wsh.domain.mapper.AdminUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "平台-操作日志", description = "管理员操作日志查询")
@RestController
@RequestMapping("/v1/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminLogController {

    private final AdminOperationLogMapper operationLogMapper;
    private final AdminUserMapper adminUserMapper;

    @Operation(summary = "操作日志列表", description = "分页查询管理员操作日志")
    @GetMapping
    public R<PageResult<AdminOperationLogResponse>> listLogs(PageQueryRequest query) {
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(AdminOperationLog::getModule, query.getKeyword())
                    .or().like(AdminOperationLog::getAction, query.getKeyword())
                    .or().like(AdminOperationLog::getDetail, query.getKeyword()));
        }
        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);

        Page<AdminOperationLog> page = new Page<>(query.getPage(), query.getSize());
        Page<AdminOperationLog> result = operationLogMapper.selectPage(page, wrapper);

        List<AdminOperationLogResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return R.ok(PageResult.of(records, result.getTotal(), query.getPage(), query.getSize()));
    }

    private AdminOperationLogResponse toResponse(AdminOperationLog log) {
        String adminName = "";
        AdminUser admin = adminUserMapper.selectById(log.getAdminId());
        if (admin != null) {
            adminName = admin.getRealName();
        }

        return AdminOperationLogResponse.builder()
                .logId(log.getLogId())
                .adminId(log.getAdminId())
                .adminName(adminName)
                .module(log.getModule())
                .action(log.getAction())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .ip(log.getIp())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
