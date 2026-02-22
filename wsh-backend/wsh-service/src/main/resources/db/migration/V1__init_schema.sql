-- ============================================================
-- 微生活券吧 - 数据库初始化脚本（Flyway V1）
-- 基于 Spec Section 6 全部表定义
-- 数据库：wsh_dev / wsh_prod（MySQL 8.0，utf8mb4）
-- ============================================================

-- -----------------------------------------------------------
-- 6.1 用户域
-- -----------------------------------------------------------

-- 用户表
CREATE TABLE tb_user (
    user_id       BIGINT PRIMARY KEY COMMENT '雪花ID',
    openid        VARCHAR(128) NOT NULL UNIQUE COMMENT '微信openid',
    unionid       VARCHAR(128) COMMENT '微信unionid',
    nickname      VARCHAR(64),
    avatar_url    VARCHAR(255),
    phone         VARCHAR(16) UNIQUE,
    status        TINYINT DEFAULT 0 COMMENT '0正常 1禁用',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 平台会员表（独立于商户会员）
CREATE TABLE tb_platform_member (
    member_id             BIGINT PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    member_level          TINYINT DEFAULT 0 COMMENT '0普通 1银卡 2金卡（预留）',
    total_consume_amount  DECIMAL(12,2) DEFAULT 0 COMMENT '平台累计消费金额',
    total_consume_count   INT DEFAULT 0 COMMENT '平台累计消费次数',
    merchant_count        INT DEFAULT 0 COMMENT '消费过的商户数',
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台会员表';

-- 用户权益提醒设置
CREATE TABLE tb_user_notification_setting (
    setting_id           BIGINT PRIMARY KEY,
    user_id              BIGINT NOT NULL UNIQUE,
    points_expire_notify TINYINT DEFAULT 1 COMMENT '积分过期提醒 0关 1开',
    balance_notify       TINYINT DEFAULT 1 COMMENT '储值余额提醒',
    voucher_expire_notify TINYINT DEFAULT 1 COMMENT '券过期提醒',
    activity_notify      TINYINT DEFAULT 1 COMMENT '新活动提醒',
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户提醒设置';

-- -----------------------------------------------------------
-- 6.2 商户域
-- -----------------------------------------------------------

-- 商户表
CREATE TABLE tb_merchant (
    merchant_id          BIGINT PRIMARY KEY,
    merchant_code        VARCHAR(32) NOT NULL UNIQUE,
    merchant_name        VARCHAR(128) NOT NULL,
    logo_url             VARCHAR(255),
    contact_name         VARCHAR(32),
    contact_phone        VARCHAR(16),
    address              VARCHAR(255) COMMENT '商户地址',
    city                 VARCHAR(32) COMMENT '所在城市（GPS定位匹配用）',
    longitude            DECIMAL(10,6) COMMENT '经度',
    latitude             DECIMAL(10,6) COMMENT '纬度',
    business_category    VARCHAR(64) COMMENT '经营类目：中餐/西餐/火锅/奶茶等',
    status               TINYINT DEFAULT 0 COMMENT '0待审核 1正常 2停用',
    integration_type     TINYINT COMMENT '1-API 2-RPA（二期） 3-手动',
    profit_sharing_rate  DECIMAL(5,4) DEFAULT 0.0200 COMMENT '交易服务费率（如0.02=2%，可配置1%-3%）',
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_location(latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- 商户门店表
CREATE TABLE tb_merchant_branch (
    branch_id    BIGINT PRIMARY KEY,
    merchant_id  BIGINT NOT NULL,
    branch_name  VARCHAR(128),
    address      VARCHAR(255),
    longitude    DECIMAL(10,6),
    latitude     DECIMAL(10,6),
    status       TINYINT DEFAULT 1,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id),
    INDEX idx_location(latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户门店表';

-- 商户员工表
CREATE TABLE tb_merchant_employee (
    employee_id  BIGINT PRIMARY KEY,
    merchant_id  BIGINT NOT NULL,
    branch_id    BIGINT,
    name         VARCHAR(32),
    phone        VARCHAR(16),
    openid       VARCHAR(128),
    role         TINYINT COMMENT '1店长/管理员 2收银员 3服务员',
    status       TINYINT DEFAULT 1,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id),
    INDEX idx_openid(openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户员工表';

-- -----------------------------------------------------------
-- 6.3 商户会员聚合数据
-- -----------------------------------------------------------

-- 商户会员快照（从商户系统同步的数据）
CREATE TABLE tb_merchant_member_snapshot (
    snapshot_id           BIGINT PRIMARY KEY,
    user_id               BIGINT NOT NULL,
    merchant_id           BIGINT NOT NULL,
    source_member_id      VARCHAR(64) COMMENT '商户系统内会员ID',
    member_level_name     VARCHAR(64),
    points                INT DEFAULT 0,
    points_value          DECIMAL(10,2) DEFAULT 0 COMMENT '积分可兑换价值（元）',
    points_expire_date    DATE COMMENT '积分过期日期',
    balance               DECIMAL(10,2) DEFAULT 0,
    consume_count         INT DEFAULT 0,
    total_consume_amount  DECIMAL(12,2) DEFAULT 0,
    last_consume_time     DATETIME,
    dormancy_level        TINYINT DEFAULT 0 COMMENT '沉睡等级：0活跃 1轻度(30天) 2中度(60天) 3深度(90天+)',
    sync_time             DATETIME COMMENT '最近同步时间',
    sync_status           TINYINT COMMENT '1成功 2失败',
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_merchant(user_id, merchant_id),
    INDEX idx_dormancy(merchant_id, dormancy_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户会员快照';

-- 商户消费记录
CREATE TABLE tb_merchant_consume_record (
    record_id        BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    merchant_id      BIGINT NOT NULL,
    branch_id        BIGINT,
    consume_time     DATETIME,
    consume_amount   DECIMAL(10,2),
    invoice_status   TINYINT DEFAULT 0 COMMENT '0未开 1已开 2开票中',
    invoice_no       VARCHAR(64),
    invoice_url      VARCHAR(255) COMMENT '电子发票链接',
    source_order_no  VARCHAR(64),
    sync_time        DATETIME,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_merchant(user_id, merchant_id),
    INDEX idx_consume_time(consume_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户消费记录';

-- 用户权益资产汇总（缓存表，定时更新）
CREATE TABLE tb_user_equity_summary (
    summary_id            BIGINT PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    total_points_value    DECIMAL(12,2) DEFAULT 0 COMMENT '全部积分可兑换总价值',
    total_balance         DECIMAL(12,2) DEFAULT 0 COMMENT '全部储值余额',
    total_voucher_value   DECIMAL(12,2) DEFAULT 0 COMMENT '全部未用券价值',
    expiring_points_value DECIMAL(12,2) DEFAULT 0 COMMENT '7天内过期积分价值',
    expiring_voucher_count INT DEFAULT 0 COMMENT '7天内过期券数量',
    merchant_count        INT DEFAULT 0 COMMENT '有权益的商户数',
    last_updated          DATETIME,
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权益资产汇总';

-- -----------------------------------------------------------
-- 6.4 活动域
-- -----------------------------------------------------------

-- 活动表（从商户系统同步或手工创建的活动）
CREATE TABLE tb_activity (
    activity_id       BIGINT PRIMARY KEY,
    merchant_id       BIGINT NOT NULL,
    source_activity_id VARCHAR(64) COMMENT '商户系统内活动ID',
    activity_type     TINYINT NOT NULL COMMENT '1积分兑换 2储值 3代金券 4团购',
    activity_name     VARCHAR(128) NOT NULL,
    activity_desc     TEXT,
    cover_image       VARCHAR(255),
    start_time        DATETIME,
    end_time          DATETIME,
    status            TINYINT DEFAULT 0 COMMENT '0同步中 1进行中 2已结束 3已下架',
    config            JSON COMMENT '活动类型特定配置',
    stock             INT DEFAULT -1 COMMENT '库存，-1无限',
    sold_count        INT DEFAULT 0,
    target_member_type TINYINT DEFAULT 0 COMMENT '目标会员类型：0全部 1仅活跃会员 2仅沉睡会员',
    is_public          TINYINT DEFAULT 0 COMMENT '是否公开可见（0仅会员可见 1非会员也可见，商户控制）',
    sync_source       VARCHAR(32) COMMENT '同步来源：api/manual（一期）；rpa/meituan（二期）',
    sync_time         DATETIME COMMENT '最近同步时间',
    sort_order        INT DEFAULT 0,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_merchant_source(merchant_id, source_activity_id),
    INDEX idx_merchant_status(merchant_id, status, start_time),
    INDEX idx_target_member(target_member_type, status),
    INDEX idx_public_city(is_public, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- -----------------------------------------------------------
-- 6.5 权益提醒表
-- -----------------------------------------------------------

-- 权益提醒记录
CREATE TABLE tb_equity_reminder (
    reminder_id     BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    merchant_id     BIGINT,
    reminder_type   TINYINT COMMENT '1积分过期 2储值提醒 3券过期 4等级降级 5沉睡唤醒',
    equity_type     VARCHAR(32) COMMENT 'points/balance/voucher',
    equity_value    DECIMAL(10,2),
    expire_date     DATE,
    remind_status   TINYINT DEFAULT 0 COMMENT '0待发送 1已发送 2发送失败',
    remind_time     DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date(user_id, created_at),
    INDEX idx_status(remind_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权益提醒记录';

-- -----------------------------------------------------------
-- 6.6 订单与支付域
-- -----------------------------------------------------------

-- 订单表
CREATE TABLE tb_order (
    order_id        BIGINT PRIMARY KEY,
    order_no        VARCHAR(32) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    merchant_id     BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    order_type      TINYINT COMMENT '1代金券 2储值 3积分兑换 4团购 5沉睡唤醒券',
    order_amount    DECIMAL(10,2),
    pay_amount      DECIMAL(10,2),
    status          TINYINT DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭 3已退款',
    pay_time        DATETIME,
    transaction_id  VARCHAR(64) COMMENT '微信支付单号',
    is_dormancy_awake TINYINT DEFAULT 0 COMMENT '是否沉睡唤醒订单',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_merchant(merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 券码表
CREATE TABLE tb_voucher (
    voucher_id       BIGINT PRIMARY KEY,
    voucher_code     VARCHAR(32) NOT NULL UNIQUE,
    order_id         BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    merchant_id      BIGINT NOT NULL,
    activity_id      BIGINT NOT NULL,
    voucher_type     TINYINT COMMENT '1代金券 2兑换券 3储值码 4沉睡唤醒券',
    voucher_value    DECIMAL(10,2),
    status           TINYINT DEFAULT 0 COMMENT '0未使用 1已使用 2已过期 3已退款',
    valid_start_time DATETIME,
    valid_end_time   DATETIME,
    used_time        DATETIME,
    used_branch_id   BIGINT,
    used_employee_id BIGINT,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status(user_id, status),
    INDEX idx_merchant(merchant_id),
    INDEX idx_expire(valid_end_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='券码表';

-- 分账记录表（核销时触发分账）
CREATE TABLE tb_profit_sharing (
    sharing_id      BIGINT PRIMARY KEY,
    order_no        VARCHAR(32) NOT NULL,
    voucher_id      BIGINT COMMENT '关联券码ID',
    merchant_id     BIGINT NOT NULL,
    transaction_id  VARCHAR(64),
    total_amount    DECIMAL(10,2) COMMENT '交易总金额',
    service_fee_rate DECIMAL(5,4) COMMENT '服务费率（快照）',
    service_fee     DECIMAL(10,2) COMMENT '平台服务费 = total_amount × service_fee_rate',
    merchant_amount DECIMAL(10,2) COMMENT '商户到账金额 = total_amount - service_fee',
    status          TINYINT DEFAULT 0 COMMENT '0待分账(待核销) 1核销触发分账中 2已分账 3失败',
    verify_record_id BIGINT COMMENT '关联核销记录ID',
    retry_count     INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order(order_no),
    INDEX idx_merchant(merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分账记录表';

-- 核销记录表
CREATE TABLE tb_verification_record (
    record_id     BIGINT PRIMARY KEY,
    voucher_id    BIGINT NOT NULL,
    voucher_code  VARCHAR(32),
    user_id       BIGINT NOT NULL,
    merchant_id   BIGINT NOT NULL,
    branch_id     BIGINT,
    employee_id   BIGINT,
    verify_time   DATETIME,
    is_dormancy_awake TINYINT DEFAULT 0 COMMENT '是否沉睡唤醒核销',
    sync_status   TINYINT DEFAULT 0 COMMENT '0未同步 1已同步 2同步失败',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id),
    INDEX idx_voucher(voucher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='核销记录表';

-- -----------------------------------------------------------
-- 6.7 集成与 RPA
-- -----------------------------------------------------------

-- 商户集成配置
CREATE TABLE tb_merchant_integration (
    integration_id   BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    integration_type TINYINT COMMENT '1-API 2-RPA（二期） 3-手动',
    system_type      VARCHAR(32) COMMENT '对接系统标识',
    config           JSON COMMENT '加密存储的连接配置',
    status           TINYINT DEFAULT 1,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户集成配置';

-- RPA 任务表（二期）
CREATE TABLE tb_rpa_task (
    task_id        BIGINT PRIMARY KEY,
    merchant_id    BIGINT NOT NULL,
    task_type      VARCHAR(32) COMMENT 'sync_member / sync_consume',
    status         TINYINT DEFAULT 0 COMMENT '0待执行 1执行中 2成功 3失败',
    retry_count    INT DEFAULT 0,
    request_params JSON,
    response_data  JSON,
    error_message  TEXT,
    started_at     DATETIME,
    completed_at   DATETIME,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant_status(merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RPA任务表（二期）';

-- -----------------------------------------------------------
-- 6.8 会员分析表
-- -----------------------------------------------------------

-- 商户会员分析快照（每日更新）
CREATE TABLE tb_merchant_member_analysis (
    analysis_id        BIGINT PRIMARY KEY,
    merchant_id        BIGINT NOT NULL,
    analysis_date      DATE NOT NULL,
    total_members      INT DEFAULT 0 COMMENT '总会员数',
    active_members     INT DEFAULT 0 COMMENT '活跃会员(30天内消费)',
    dormant_light      INT DEFAULT 0 COMMENT '轻度沉睡(30-60天)',
    dormant_medium     INT DEFAULT 0 COMMENT '中度沉睡(60-90天)',
    dormant_deep       INT DEFAULT 0 COMMENT '深度沉睡(90天+)',
    new_members_7d     INT DEFAULT 0 COMMENT '近7天新增',
    awakened_7d        INT DEFAULT 0 COMMENT '近7天唤醒成功数',
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_merchant_date(merchant_id, analysis_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户会员分析快照';

-- -----------------------------------------------------------
-- 6.9 费用与计费域
-- -----------------------------------------------------------

-- 入驻费配置表
CREATE TABLE tb_onboarding_fee_plan (
    plan_id          BIGINT PRIMARY KEY,
    plan_name        VARCHAR(64) NOT NULL COMMENT '套餐名称',
    plan_type        TINYINT COMMENT '1-按门店 2-按品牌',
    fee_amount       DECIMAL(10,2) NOT NULL COMMENT '费用金额（元）',
    duration_months  INT DEFAULT 12 COMMENT '有效期（月）',
    description      TEXT,
    status           TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入驻费配置表';

-- 商户入驻费缴纳记录
CREATE TABLE tb_merchant_onboarding_fee (
    fee_id           BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    plan_id          BIGINT NOT NULL,
    fee_amount       DECIMAL(10,2) NOT NULL COMMENT '缴纳金额',
    pay_status       TINYINT DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭',
    pay_time         DATETIME,
    transaction_id   VARCHAR(64) COMMENT '微信支付单号',
    valid_start_date DATE COMMENT '有效期开始',
    valid_end_date   DATE COMMENT '有效期结束',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id),
    INDEX idx_expire(valid_end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户入驻费缴纳记录';

-- 服务费扣除记录（核销时生成）
CREATE TABLE tb_service_fee_record (
    record_id        BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    order_no         VARCHAR(32) NOT NULL,
    voucher_id       BIGINT NOT NULL,
    verify_record_id BIGINT NOT NULL COMMENT '关联核销记录',
    order_amount     DECIMAL(10,2) COMMENT '订单金额',
    service_fee_rate DECIMAL(5,4) COMMENT '服务费率（快照）',
    service_fee      DECIMAL(10,2) COMMENT '服务费金额',
    merchant_amount  DECIMAL(10,2) COMMENT '商户到账金额',
    sharing_status   TINYINT DEFAULT 0 COMMENT '0待分账 1已分账 2分账失败',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id),
    INDEX idx_merchant_date(merchant_id, created_at),
    INDEX idx_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务费扣除记录';
