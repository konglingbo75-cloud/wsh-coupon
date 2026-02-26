-- ============================================================
-- 添加管理后台相关表
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_admin_user (
    admin_id    BIGINT PRIMARY KEY,
    username    VARCHAR(32) NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    real_name   VARCHAR(32),
    role        TINYINT DEFAULT 1 COMMENT '1超级管理员 2运营 3客服',
    status      TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    last_login  DATETIME,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

CREATE TABLE IF NOT EXISTS tb_merchant_audit_log (
    log_id      BIGINT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    admin_id    BIGINT NOT NULL,
    action      VARCHAR(32) NOT NULL,
    prev_status TINYINT,
    new_status  TINYINT,
    reason      VARCHAR(255),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户审核日志';
