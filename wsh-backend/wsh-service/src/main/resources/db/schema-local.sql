-- ============================================================
-- H2 compatible schema for local testing (MODE=MySQL)
-- Stripped ENGINE/CHARSET/COLLATE/COMMENT clauses
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_user (
    user_id       BIGINT PRIMARY KEY,
    openid        VARCHAR(128) NOT NULL UNIQUE,
    unionid       VARCHAR(128),
    nickname      VARCHAR(64),
    avatar_url    VARCHAR(255),
    phone         VARCHAR(16),
    status        TINYINT DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_platform_member (
    member_id             BIGINT PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    member_level          TINYINT DEFAULT 0,
    total_consume_amount  DECIMAL(12,2) DEFAULT 0,
    total_consume_count   INT DEFAULT 0,
    merchant_count        INT DEFAULT 0,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_user_notification_setting (
    setting_id           BIGINT PRIMARY KEY,
    user_id              BIGINT NOT NULL UNIQUE,
    points_expire_notify TINYINT DEFAULT 1,
    balance_notify       TINYINT DEFAULT 1,
    voucher_expire_notify TINYINT DEFAULT 1,
    activity_notify      TINYINT DEFAULT 1,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant (
    merchant_id          BIGINT PRIMARY KEY,
    merchant_code        VARCHAR(32) NOT NULL UNIQUE,
    merchant_name        VARCHAR(128) NOT NULL,
    logo_url             VARCHAR(255),
    contact_name         VARCHAR(32),
    contact_phone        VARCHAR(16),
    address              VARCHAR(255),
    city                 VARCHAR(32),
    longitude            DECIMAL(10,6),
    latitude             DECIMAL(10,6),
    business_category    VARCHAR(64),
    status               TINYINT DEFAULT 0,
    integration_type     TINYINT,
    profit_sharing_rate  DECIMAL(5,4) DEFAULT 0.0200,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_branch (
    branch_id    BIGINT PRIMARY KEY,
    merchant_id  BIGINT NOT NULL,
    branch_name  VARCHAR(128),
    address      VARCHAR(255),
    longitude    DECIMAL(10,6),
    latitude     DECIMAL(10,6),
    status       TINYINT DEFAULT 1,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_employee (
    employee_id  BIGINT PRIMARY KEY,
    merchant_id  BIGINT NOT NULL,
    branch_id    BIGINT,
    name         VARCHAR(32),
    phone        VARCHAR(16),
    openid       VARCHAR(128),
    role         TINYINT,
    status       TINYINT DEFAULT 1,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_member_snapshot (
    snapshot_id           BIGINT PRIMARY KEY,
    user_id               BIGINT NOT NULL,
    merchant_id           BIGINT NOT NULL,
    source_member_id      VARCHAR(64),
    member_level_name     VARCHAR(64),
    points                INT DEFAULT 0,
    points_value          DECIMAL(10,2) DEFAULT 0,
    points_expire_date    DATE,
    balance               DECIMAL(10,2) DEFAULT 0,
    consume_count         INT DEFAULT 0,
    total_consume_amount  DECIMAL(12,2) DEFAULT 0,
    last_consume_time     TIMESTAMP,
    dormancy_level        TINYINT DEFAULT 0,
    sync_time             TIMESTAMP,
    sync_status           TINYINT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_snapshot_user_merchant ON tb_merchant_member_snapshot(user_id, merchant_id);

CREATE TABLE IF NOT EXISTS tb_merchant_consume_record (
    record_id        BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    merchant_id      BIGINT NOT NULL,
    branch_id        BIGINT,
    consume_time     TIMESTAMP,
    consume_amount   DECIMAL(10,2),
    invoice_status   TINYINT DEFAULT 0,
    invoice_no       VARCHAR(64),
    invoice_url      VARCHAR(255),
    source_order_no  VARCHAR(64),
    sync_time        TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_user_equity_summary (
    summary_id            BIGINT PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    total_points_value    DECIMAL(12,2) DEFAULT 0,
    total_balance         DECIMAL(12,2) DEFAULT 0,
    total_voucher_value   DECIMAL(12,2) DEFAULT 0,
    expiring_points_value DECIMAL(12,2) DEFAULT 0,
    expiring_voucher_count INT DEFAULT 0,
    merchant_count        INT DEFAULT 0,
    last_updated          TIMESTAMP,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_activity (
    activity_id       BIGINT PRIMARY KEY,
    merchant_id       BIGINT NOT NULL,
    source_activity_id VARCHAR(64),
    activity_type     TINYINT NOT NULL,
    activity_name     VARCHAR(128) NOT NULL,
    activity_desc     CLOB,
    cover_image       VARCHAR(255),
    start_time        TIMESTAMP,
    end_time          TIMESTAMP,
    status            TINYINT DEFAULT 0,
    config            CLOB,
    stock             INT DEFAULT -1,
    sold_count        INT DEFAULT 0,
    target_member_type TINYINT DEFAULT 0,
    is_public          TINYINT DEFAULT 0,
    sync_source       VARCHAR(32),
    sync_time         TIMESTAMP,
    sort_order        INT DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_equity_reminder (
    reminder_id     BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    merchant_id     BIGINT,
    reminder_type   TINYINT,
    equity_type     VARCHAR(32),
    equity_value    DECIMAL(10,2),
    expire_date     DATE,
    remind_status   TINYINT DEFAULT 0,
    remind_time     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_order (
    order_id        BIGINT PRIMARY KEY,
    order_no        VARCHAR(32) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    merchant_id     BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    order_type      TINYINT,
    order_amount    DECIMAL(10,2),
    pay_amount      DECIMAL(10,2),
    status          TINYINT DEFAULT 0,
    pay_time        TIMESTAMP,
    transaction_id  VARCHAR(64),
    is_dormancy_awake TINYINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_voucher (
    voucher_id       BIGINT PRIMARY KEY,
    voucher_code     VARCHAR(32) NOT NULL UNIQUE,
    order_id         BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    merchant_id      BIGINT NOT NULL,
    activity_id      BIGINT NOT NULL,
    voucher_type     TINYINT,
    voucher_value    DECIMAL(10,2),
    status           TINYINT DEFAULT 0,
    valid_start_time TIMESTAMP,
    valid_end_time   TIMESTAMP,
    used_time        TIMESTAMP,
    used_branch_id   BIGINT,
    used_employee_id BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_profit_sharing (
    sharing_id      BIGINT PRIMARY KEY,
    order_no        VARCHAR(32) NOT NULL,
    voucher_id      BIGINT,
    merchant_id     BIGINT NOT NULL,
    transaction_id  VARCHAR(64),
    total_amount    DECIMAL(10,2),
    service_fee_rate DECIMAL(5,4),
    service_fee     DECIMAL(10,2),
    merchant_amount DECIMAL(10,2),
    status          TINYINT DEFAULT 0,
    verify_record_id BIGINT,
    retry_count     INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_verification_record (
    record_id     BIGINT PRIMARY KEY,
    voucher_id    BIGINT NOT NULL,
    voucher_code  VARCHAR(32),
    user_id       BIGINT NOT NULL,
    merchant_id   BIGINT NOT NULL,
    branch_id     BIGINT,
    employee_id   BIGINT,
    verify_time   TIMESTAMP,
    is_dormancy_awake TINYINT DEFAULT 0,
    sync_status   TINYINT DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_integration (
    integration_id   BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    integration_type TINYINT,
    system_type      VARCHAR(32),
    config           CLOB,
    status           TINYINT DEFAULT 1,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_rpa_task (
    task_id        BIGINT PRIMARY KEY,
    merchant_id    BIGINT NOT NULL,
    task_type      VARCHAR(32),
    status         TINYINT DEFAULT 0,
    retry_count    INT DEFAULT 0,
    request_params CLOB,
    response_data  CLOB,
    error_message  CLOB,
    started_at     TIMESTAMP,
    completed_at   TIMESTAMP,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_member_analysis (
    analysis_id        BIGINT PRIMARY KEY,
    merchant_id        BIGINT NOT NULL,
    analysis_date      DATE NOT NULL,
    total_members      INT DEFAULT 0,
    active_members     INT DEFAULT 0,
    dormant_light      INT DEFAULT 0,
    dormant_medium     INT DEFAULT 0,
    dormant_deep       INT DEFAULT 0,
    new_members_7d     INT DEFAULT 0,
    awakened_7d        INT DEFAULT 0,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_analysis_merchant_date ON tb_merchant_member_analysis(merchant_id, analysis_date);

CREATE TABLE IF NOT EXISTS tb_onboarding_fee_plan (
    plan_id          BIGINT PRIMARY KEY,
    plan_name        VARCHAR(64) NOT NULL,
    plan_type        TINYINT,
    fee_amount       DECIMAL(10,2) NOT NULL,
    duration_months  INT DEFAULT 12,
    description      CLOB,
    status           TINYINT DEFAULT 1,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_onboarding_fee (
    fee_id           BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    plan_id          BIGINT NOT NULL,
    fee_amount       DECIMAL(10,2) NOT NULL,
    pay_status       TINYINT DEFAULT 0,
    pay_time         TIMESTAMP,
    transaction_id   VARCHAR(64),
    valid_start_date DATE,
    valid_end_date   DATE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_service_fee_record (
    record_id        BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    order_no         VARCHAR(32) NOT NULL,
    voucher_id       BIGINT NOT NULL,
    verify_record_id BIGINT NOT NULL,
    order_amount     DECIMAL(10,2),
    service_fee_rate DECIMAL(5,4),
    service_fee      DECIMAL(10,2),
    merchant_amount  DECIMAL(10,2),
    sharing_status   TINYINT DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 平台运营后台管理表
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_admin_user (
    admin_id    BIGINT PRIMARY KEY,
    username    VARCHAR(32) NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    real_name   VARCHAR(32),
    role        TINYINT DEFAULT 1,
    status      TINYINT DEFAULT 1,
    last_login  TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_merchant_audit_log (
    log_id      BIGINT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    admin_id    BIGINT NOT NULL,
    action      VARCHAR(32) NOT NULL,
    prev_status TINYINT,
    new_status  TINYINT,
    reason      VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_merchant ON tb_merchant_audit_log(merchant_id);
CREATE INDEX IF NOT EXISTS idx_audit_admin ON tb_merchant_audit_log(admin_id);

CREATE TABLE IF NOT EXISTS tb_admin_operation_log (
    log_id      BIGINT PRIMARY KEY,
    admin_id    BIGINT NOT NULL,
    module      VARCHAR(32),
    action      VARCHAR(64),
    target_id   VARCHAR(64),
    detail      CLOB,
    ip          VARCHAR(64),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_oplog_admin ON tb_admin_operation_log(admin_id);
CREATE INDEX IF NOT EXISTS idx_oplog_time ON tb_admin_operation_log(created_at);
