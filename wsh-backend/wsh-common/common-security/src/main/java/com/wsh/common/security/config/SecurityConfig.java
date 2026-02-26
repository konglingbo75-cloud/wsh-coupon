package com.wsh.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsh.common.core.result.R;
import com.wsh.common.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isLocal = activeProfile != null
                && (activeProfile.contains("local") || activeProfile.contains("dev"));

        http
            // 关闭 CSRF（小程序无 Cookie）
            .csrf(AbstractHttpConfigurer::disable)
            // 无状态 Session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 路径权限配置
            .authorizeHttpRequests(auth -> {
                // 白名单：认证、公共接口、健康检查、静态资源
                auth.requestMatchers("/v1/auth/**").permitAll()
                    .requestMatchers("/v1/admin/auth/**").permitAll()
                    .requestMatchers("/v1/public/**").permitAll()
                    .requestMatchers("/v1/callback/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/static/**", "/*.html", "/*.css", "/*.js", "/*.ico").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                // 仅本地/开发环境开放 H2 控制台和 Swagger
                if (isLocal) {
                    auth.requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll();
                }

                // 其余接口需要认证
                auth.anyRequest().authenticated();
            })
            // 仅本地环境允许 H2 Console 的 iframe
            .headers(headers -> {
                if (isLocal) {
                    headers.frameOptions(frame -> frame.sameOrigin());
                } else {
                    headers.frameOptions(frame -> frame.deny());
                }
            })
            // 未认证处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(
                            objectMapper.writeValueAsString(R.fail(401, "未登录或登录已过期")));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(
                            objectMapper.writeValueAsString(R.fail(403, "无权限访问")));
                })
            )
            // JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
