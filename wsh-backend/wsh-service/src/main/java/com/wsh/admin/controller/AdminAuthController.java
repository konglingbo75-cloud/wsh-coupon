package com.wsh.admin.controller;

import com.wsh.admin.dto.AdminLoginRequest;
import com.wsh.admin.dto.AdminLoginResponse;
import com.wsh.admin.service.AdminAuthService;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "平台管理员认证", description = "管理员登录/登出")
@RestController
@RequestMapping("/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "管理员登录", description = "使用用户名密码登录平台运营后台")
    @PostMapping("/login")
    public R<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return R.ok(adminAuthService.login(request));
    }
}
