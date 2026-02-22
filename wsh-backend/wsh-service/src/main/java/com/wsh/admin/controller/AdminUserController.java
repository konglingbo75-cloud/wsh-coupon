package com.wsh.admin.controller;

import com.wsh.admin.dto.AdminUserListResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.admin.service.AdminUserService;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "平台-用户管理", description = "平台用户查询")
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "用户列表", description = "分页查询平台用户，支持关键词搜索和状态筛选")
    @GetMapping
    public R<PageResult<AdminUserListResponse>> listUsers(PageQueryRequest query) {
        return R.ok(adminUserService.listUsers(query));
    }
}
