package com.wsh.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * 本地环境数据初始化器
 * 确保H2内存数据库在启动时加载schema和测试数据
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("========= 开始初始化本地测试数据 =========");
        try {
            // 先执行schema
            ResourceDatabasePopulator schemaPopulator = new ResourceDatabasePopulator();
            schemaPopulator.addScript(new ClassPathResource("db/schema-local.sql"));
            schemaPopulator.setSqlScriptEncoding("UTF-8");
            schemaPopulator.setContinueOnError(false);
            schemaPopulator.execute(dataSource);
            log.info("Schema初始化完成");
            
            // 再执行数据
            ResourceDatabasePopulator dataPopulator = new ResourceDatabasePopulator();
            dataPopulator.addScript(new ClassPathResource("db/data-local.sql"));
            dataPopulator.setSqlScriptEncoding("UTF-8");
            dataPopulator.setContinueOnError(false); // 不允许失败，以便发现问题
            dataPopulator.execute(dataSource);
            log.info("Data初始化完成");
            
            // 验证数据是否正确插入
            verifyData();
            
            // 创建默认管理员账号（密码需要BCrypt加密，不适合放在SQL中）
            initAdminUser();
            
            log.info("========= 本地测试数据初始化完成 =========");
        } catch (Exception e) {
            log.error("初始化测试数据失败: {}", e.getMessage(), e);
        }
    }
    
    private void verifyData() {
        Integer merchantCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_merchant", Integer.class);
        Integer activityCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_activity", Integer.class);
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_user", Integer.class);
        Integer snapshotCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_merchant_member_snapshot", Integer.class);
        Integer auditLogCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_merchant_audit_log", Integer.class);
        
        log.info("数据验证 - 商户: {}, 活动: {}, 用户: {}, 会员快照: {}, 审核日志: {}", 
                merchantCount, activityCount, userCount, snapshotCount, auditLogCount);
    }
    
    private void initAdminUser() {
        Integer adminCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_admin_user", Integer.class);
        if (adminCount != null && adminCount == 0) {
            String encodedPassword = passwordEncoder.encode("admin123");
            jdbcTemplate.update(
                    "INSERT INTO tb_admin_user (admin_id, username, password, real_name, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                    1L, "admin", encodedPassword, "平台管理员", 1, 1);
            log.info("默认管理员账号创建成功: admin / admin123");
        }
    }
}
