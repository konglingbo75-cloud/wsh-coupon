package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.security.util.JwtUtil;
import com.wsh.admin.dto.AdminLoginRequest;
import com.wsh.admin.dto.AdminLoginResponse;
import com.wsh.domain.entity.AdminUser;
import com.wsh.domain.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser admin = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, request.getUsername()));

        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (admin.getStatus() != 1) {
            throw new BusinessException(403, "该账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 更新最后登录时间
        admin.setLastLogin(LocalDateTime.now());
        adminUserMapper.updateById(admin);

        String token = jwtUtil.generateAdminToken(admin.getAdminId(), admin.getUsername());

        log.info("管理员登录成功: adminId={}, username={}", admin.getAdminId(), admin.getUsername());

        return AdminLoginResponse.builder()
                .token(token)
                .adminId(admin.getAdminId())
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .role(admin.getRole())
                .build();
    }
}
