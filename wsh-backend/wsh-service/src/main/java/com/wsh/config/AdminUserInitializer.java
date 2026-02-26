package com.wsh.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 管理员账号初始化（所有环境生效）
 * 启动时检查是否存在管理员，不存在则创建默认账号
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            Integer adminCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tb_admin_user", Integer.class);
            if (adminCount != null && adminCount == 0) {
                String encodedPassword = passwordEncoder.encode("admin123");
                jdbcTemplate.update(
                        "INSERT INTO tb_admin_user (admin_id, username, password, real_name, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                        1L, "admin", encodedPassword, "平台管理员", 1, 1);
                log.info("默认管理员账号创建成功: admin / admin123");
            }
        } catch (Exception e) {
            log.warn("管理员初始化跳过: {}", e.getMessage());
        }
    }
}
