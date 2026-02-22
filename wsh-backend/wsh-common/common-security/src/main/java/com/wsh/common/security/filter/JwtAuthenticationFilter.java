package com.wsh.common.security.filter;

import com.wsh.common.security.domain.LoginUser;
import com.wsh.common.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器：从请求头提取 Token，解析后设置 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Value("${wsh.security.mock-auth:false}")
    private boolean mockAuthEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            // 本地测试模式：支持 mock token
            if (mockAuthEnabled && token.startsWith("mock_")) {
                handleMockToken(token);
            } else if (jwtUtil.validateToken(token)) {
                handleRealToken(token);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleRealToken(String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            LoginUser loginUser = new LoginUser();

            String tokenType = claims.get("tokenType", String.class);
            if ("admin".equals(tokenType)) {
                Long adminId = claims.get("adminId", Long.class);
                loginUser.setUserId(adminId);
                loginUser.setAdminId(adminId);
                loginUser.setRole(claims.get("role", Integer.class));
            } else {
                loginUser.setUserId(claims.get("userId", Long.class));
                loginUser.setOpenid(claims.get("openid", String.class));
                loginUser.setRole(claims.get("role", Integer.class));
                loginUser.setMerchantId(claims.get("merchantId", Long.class));
                loginUser.setEmployeeId(claims.get("employeeId", Long.class));
            }

            setAuthentication(loginUser);
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
        }
    }

    /**
     * 处理本地测试用的 mock token
     * 格式: mock_user_token_{userId} 或 mock_merchant_token_{merchantId}
     */
    private void handleMockToken(String token) {
        LoginUser loginUser = new LoginUser();

        if (token.startsWith("mock_user_token_")) {
            // 普通用户 mock token
            String userIdStr = token.substring("mock_user_token_".length());
            loginUser.setUserId(Long.parseLong(userIdStr));
            loginUser.setOpenid("mock_openid_" + userIdStr);
            loginUser.setRole(0); // consumer
            log.debug("Mock user login: userId={}", userIdStr);
        } else if (token.startsWith("mock_merchant_token_")) {
            // 商户员工 mock token
            String merchantIdStr = token.substring("mock_merchant_token_".length());
            loginUser.setUserId(100001L); // 固定测试用户
            loginUser.setOpenid("mock_openid_merchant");
            loginUser.setRole(1); // merchant_admin
            loginUser.setMerchantId(Long.parseLong(merchantIdStr));
            loginUser.setEmployeeId(400001L); // 固定测试员工
            log.debug("Mock merchant login: merchantId={}", merchantIdStr);
        } else if (token.startsWith("mock_admin_token_")) {
            // 平台管理员 mock token
            String adminIdStr = token.substring("mock_admin_token_".length());
            loginUser.setUserId(Long.parseLong(adminIdStr));
            loginUser.setAdminId(Long.parseLong(adminIdStr));
            loginUser.setRole(10); // platform_admin
            log.debug("Mock admin login: adminId={}", adminIdStr);
        } else {
            log.warn("Unknown mock token format: {}", token);
            return;
        }

        setAuthentication(loginUser);
    }

    private void setAuthentication(LoginUser loginUser) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
