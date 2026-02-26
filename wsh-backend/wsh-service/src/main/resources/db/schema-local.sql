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
    group_order_id  BIGINT,
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

-- ============================================================
-- 费用与计费相关表 (V4 迁移补充)
-- ============================================================

-- 服务套餐配置
CREATE TABLE IF NOT EXISTS tb_service_package (
    package_id       BIGINT PRIMARY KEY,
    package_name     VARCHAR(64) NOT NULL,
    package_type     TINYINT,
    price            DECIMAL(10,2) NOT NULL,
    duration_months  INT DEFAULT 12,
    service_fee_rate DECIMAL(5,4),
    features         CLOB,
    status           TINYINT DEFAULT 1,
    sort_order       INT DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商户套餐购买记录
CREATE TABLE IF NOT EXISTS tb_merchant_package_purchase (
    purchase_id      BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    package_id       BIGINT NOT NULL,
    price_paid       DECIMAL(10,2) NOT NULL,
    pay_status       TINYINT DEFAULT 0,
    pay_time         TIMESTAMP,
    transaction_id   VARCHAR(64),
    valid_start_date DATE,
    valid_end_date   DATE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_purchase_merchant ON tb_merchant_package_purchase(merchant_id);

-- 商户保证金记录
CREATE TABLE IF NOT EXISTS tb_merchant_deposit (
    deposit_id       BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    deposit_amount   DECIMAL(10,2) NOT NULL,
    pay_status       TINYINT DEFAULT 0,
    pay_time         TIMESTAMP,
    transaction_id   VARCHAR(64),
    refund_time      TIMESTAMP,
    refund_reason    VARCHAR(255),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_deposit_merchant ON tb_merchant_deposit(merchant_id);

-- 月度服务费汇总
CREATE TABLE IF NOT EXISTS tb_monthly_service_fee (
    summary_id       BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    year_month       VARCHAR(7) NOT NULL,
    order_count      INT DEFAULT 0,
    total_amount     DECIMAL(10,2) DEFAULT 0,
    service_fee      DECIMAL(10,2) DEFAULT 0,
    deduct_status    TINYINT DEFAULT 0,
    deduct_time      TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_monthly_fee_merchant ON tb_monthly_service_fee(merchant_id);
CREATE INDEX IF NOT EXISTS idx_monthly_fee_month ON tb_monthly_service_fee(year_month);

-- 商户余额账户
CREATE TABLE IF NOT EXISTS tb_merchant_balance (
    balance_id       BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL UNIQUE,
    balance          DECIMAL(12,2) DEFAULT 0,
    total_recharge   DECIMAL(12,2) DEFAULT 0,
    total_consume    DECIMAL(12,2) DEFAULT 0,
    frozen_amount    DECIMAL(12,2) DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商户余额流水
CREATE TABLE IF NOT EXISTS tb_merchant_balance_log (
    log_id           BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    change_type      TINYINT,
    amount           DECIMAL(10,2),
    balance_before   DECIMAL(12,2),
    balance_after    DECIMAL(12,2),
    related_order_no VARCHAR(64),
    remark           VARCHAR(255),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_balance_log_merchant ON tb_merchant_balance_log(merchant_id);

-- 补充 tb_merchant 表的 service_fee_mode 列
ALTER TABLE tb_merchant ADD COLUMN IF NOT EXISTS service_fee_mode TINYINT DEFAULT 2;

-- ============================================================
-- AI服务与发票管理表
-- ============================================================

-- AI模型配置表
CREATE TABLE IF NOT EXISTS tb_ai_model_config (
    config_id        BIGINT PRIMARY KEY,
    provider_code    VARCHAR(32) NOT NULL,
    provider_name    VARCHAR(64) NOT NULL,
    model_name       VARCHAR(64) NOT NULL,
    api_endpoint     VARCHAR(255),
    api_key          VARCHAR(512),
    is_default       TINYINT DEFAULT 0,
    status           TINYINT DEFAULT 1,
    input_price      DECIMAL(10,6),
    output_price     DECIMAL(10,6),
    max_tokens       INT DEFAULT 4096,
    temperature      DECIMAL(3,2) DEFAULT 0.70,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_ai_model_provider ON tb_ai_model_config(provider_code);
CREATE INDEX IF NOT EXISTS idx_ai_model_default ON tb_ai_model_config(is_default, status);

-- AI对话会话表
CREATE TABLE IF NOT EXISTS tb_ai_conversation (
    conversation_id  BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    title            VARCHAR(128),
    model_config_id  BIGINT,
    message_count    INT DEFAULT 0,
    total_tokens     INT DEFAULT 0,
    total_cost       DECIMAL(10,4) DEFAULT 0,
    status           TINYINT DEFAULT 1,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_ai_conv_user ON tb_ai_conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_conv_created ON tb_ai_conversation(created_at);

-- AI对话消息表
CREATE TABLE IF NOT EXISTS tb_ai_message (
    message_id       BIGINT PRIMARY KEY,
    conversation_id  BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    role             VARCHAR(16) NOT NULL,
    content          CLOB NOT NULL,
    input_tokens     INT DEFAULT 0,
    output_tokens    INT DEFAULT 0,
    cost             DECIMAL(10,6) DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_ai_msg_conv ON tb_ai_message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_msg_user ON tb_ai_message(user_id);

-- AI调用日账单表
CREATE TABLE IF NOT EXISTS tb_ai_usage_daily (
    daily_id         BIGINT PRIMARY KEY,
    stat_date        DATE NOT NULL,
    model_config_id  BIGINT,
    call_count       INT DEFAULT 0,
    total_input_tokens  INT DEFAULT 0,
    total_output_tokens INT DEFAULT 0,
    total_cost       DECIMAL(12,4) DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_ai_usage_date_model ON tb_ai_usage_daily(stat_date, model_config_id);

-- 发票信息表
CREATE TABLE IF NOT EXISTS tb_invoice (
    invoice_id       BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    merchant_id      BIGINT NOT NULL,
    consume_record_id BIGINT,
    invoice_no       VARCHAR(64),
    invoice_code     VARCHAR(32),
    invoice_type     TINYINT DEFAULT 1,
    invoice_status   TINYINT DEFAULT 0,
    invoice_amount   DECIMAL(10,2),
    invoice_title    VARCHAR(128),
    tax_number       VARCHAR(32),
    invoice_url      VARCHAR(512),
    invoice_date     DATE,
    request_time     TIMESTAMP,
    complete_time    TIMESTAMP,
    sync_time        TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_invoice_user ON tb_invoice(user_id);
CREATE INDEX IF NOT EXISTS idx_invoice_merchant ON tb_invoice(merchant_id);
CREATE INDEX IF NOT EXISTS idx_invoice_status ON tb_invoice(invoice_status);

-- 用户发票抬头设置表
CREATE TABLE IF NOT EXISTS tb_user_invoice_setting (
    setting_id       BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    title_type       TINYINT DEFAULT 1,
    invoice_title    VARCHAR(128) NOT NULL,
    tax_number       VARCHAR(32),
    bank_name        VARCHAR(64),
    bank_account     VARCHAR(32),
    company_address  VARCHAR(255),
    company_phone    VARCHAR(16),
    is_default       TINYINT DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_invoice_setting_user ON tb_user_invoice_setting(user_id);
CREATE INDEX IF NOT EXISTS idx_invoice_setting_default ON tb_user_invoice_setting(user_id, is_default);

-- ============================================================
-- 城市管理表
-- ============================================================

-- 开通城市表
CREATE TABLE IF NOT EXISTS tb_open_city (
    city_id           BIGINT PRIMARY KEY,
    city_code         VARCHAR(32) NOT NULL UNIQUE,
    city_name         VARCHAR(64) NOT NULL,
    province_name     VARCHAR(64),
    pinyin            VARCHAR(8),
    level             TINYINT DEFAULT 2,
    longitude         DECIMAL(10,6),
    latitude          DECIMAL(10,6),
    merchant_count    INT DEFAULT 0,
    activity_count    INT DEFAULT 0,
    status            TINYINT DEFAULT 1,
    sort_order        INT DEFAULT 0,
    open_date         DATE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_city_status ON tb_open_city(status);
CREATE INDEX IF NOT EXISTS idx_city_pinyin ON tb_open_city(pinyin);
CREATE INDEX IF NOT EXISTS idx_city_sort ON tb_open_city(sort_order DESC, merchant_count DESC);

-- ============================================================
-- 拼团功能表
-- ============================================================

-- 拼团配置表
CREATE TABLE IF NOT EXISTS tb_group_buy_config (
    config_id          BIGINT PRIMARY KEY,
    activity_id        BIGINT NOT NULL UNIQUE,
    min_members        INT DEFAULT 2,
    max_members        INT DEFAULT 10,
    expire_hours       INT DEFAULT 24,
    auto_refund        TINYINT DEFAULT 1,
    allow_self_buy     TINYINT DEFAULT 0,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_groupconfig_activity ON tb_group_buy_config(activity_id);

-- 拼团记录表
CREATE TABLE IF NOT EXISTS tb_group_order (
    group_order_id     BIGINT PRIMARY KEY,
    group_no           VARCHAR(32) NOT NULL UNIQUE,
    activity_id        BIGINT NOT NULL,
    initiator_user_id  BIGINT NOT NULL,
    required_members   INT NOT NULL,
    current_members    INT DEFAULT 1,
    status             TINYINT DEFAULT 0,
    expire_time        TIMESTAMP NOT NULL,
    complete_time      TIMESTAMP,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_grouporder_activity ON tb_group_order(activity_id);
CREATE INDEX IF NOT EXISTS idx_grouporder_status_expire ON tb_group_order(status, expire_time);
CREATE INDEX IF NOT EXISTS idx_grouporder_initiator ON tb_group_order(initiator_user_id);

-- 拼团参与者表
CREATE TABLE IF NOT EXISTS tb_group_participant (
    participant_id     BIGINT PRIMARY KEY,
    group_order_id     BIGINT NOT NULL,
    user_id            BIGINT NOT NULL,
    order_id           BIGINT,
    is_initiator       TINYINT DEFAULT 0,
    join_time          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_participant_group ON tb_group_participant(group_order_id);
CREATE INDEX IF NOT EXISTS idx_participant_user ON tb_group_participant(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_participant_group_user ON tb_group_participant(group_order_id, user_id);
