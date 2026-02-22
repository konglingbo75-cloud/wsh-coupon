package com.wsh.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsh.common.core.result.R;
import com.wsh.common.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF（小程序无 Cookie）
            .csrf(AbstractHttpConfigurer::disable)
            // 无状态 Session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 路径权限配置
            .authorizeHttpRequests(auth -> auth
                // 白名单：认证、公共接口、Swagger、健康检查、静态资源
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/v1/admin/auth/**").permitAll()
                .requestMatchers("/v1/public/**").permitAll()
                .requestMatchers("/v1/callback/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/static/**", "/*.html", "/*.css", "/*.js", "/*.ico").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 其余接口需要认证
                .anyRequest().authenticated()
            )
            // H2 Console需要允许iframe
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
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
