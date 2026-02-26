-- 服务套餐配置表
CREATE TABLE tb_service_package (
    package_id       BIGINT PRIMARY KEY,
    package_name     VARCHAR(64) NOT NULL COMMENT '套餐名称',
    package_type     TINYINT COMMENT '1基础 2标准 3高级',
    price            DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    duration_months  INT DEFAULT 12 COMMENT '时长(月)',
    service_fee_rate DECIMAL(5,4) COMMENT '套餐内含服务费率',
    features         JSON COMMENT '套餐功能清单',
    status           TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    sort_order       INT DEFAULT 0,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务套餐配置表';

-- 商户套餐购买记录
CREATE TABLE tb_merchant_package_purchase (
    purchase_id      BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    package_id       BIGINT NOT NULL,
    price_paid       DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_status       TINYINT DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭',
    pay_time         DATETIME,
    transaction_id   VARCHAR(64),
    valid_start_date DATE,
    valid_end_date   DATE,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户套餐购买记录';

-- 商户余额账户
CREATE TABLE tb_merchant_balance (
    balance_id       BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL UNIQUE,
    balance          DECIMAL(10,2) DEFAULT 0 COMMENT '当前余额',
    total_recharge   DECIMAL(10,2) DEFAULT 0 COMMENT '累计充值',
    total_consume    DECIMAL(10,2) DEFAULT 0 COMMENT '累计消费',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户余额账户';

-- 余额流水表
CREATE TABLE tb_merchant_balance_log (
    log_id           BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    change_type      TINYINT COMMENT '1充值 2扣费 3退款',
    amount           DECIMAL(10,2) NOT NULL COMMENT '变动金额',
    balance_before   DECIMAL(10,2) COMMENT '变动前余额',
    balance_after    DECIMAL(10,2) COMMENT '变动后余额',
    related_order_no VARCHAR(64) COMMENT '关联订单号',
    remark           VARCHAR(255),
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant_time(merchant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额流水表';

-- 商户保证金记录
CREATE TABLE tb_merchant_deposit (
    deposit_id       BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    deposit_amount   DECIMAL(10,2) NOT NULL COMMENT '保证金金额',
    pay_status       TINYINT DEFAULT 0 COMMENT '0待缴 1已缴 2退还中 3已退还',
    pay_time         DATETIME,
    transaction_id   VARCHAR(64),
    refund_time      DATETIME,
    refund_reason    VARCHAR(255),
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户保证金记录';

-- 月度服务费汇总表
CREATE TABLE tb_monthly_service_fee (
    summary_id       BIGINT PRIMARY KEY,
    merchant_id      BIGINT NOT NULL,
    year_month       VARCHAR(7) NOT NULL COMMENT '年月YYYY-MM',
    order_count      INT DEFAULT 0 COMMENT '订单数',
    total_amount     DECIMAL(10,2) DEFAULT 0 COMMENT '订单总额',
    service_fee      DECIMAL(10,2) DEFAULT 0 COMMENT '服务费总额',
    deduct_status    TINYINT DEFAULT 0 COMMENT '0待扣 1已扣 2余额不足',
    deduct_time      DATETIME,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_merchant_month(merchant_id, year_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月度服务费汇总';

-- 商户表新增服务费模式字段
ALTER TABLE tb_merchant ADD COLUMN service_fee_mode TINYINT DEFAULT 2 COMMENT '服务费模式:1预付费 2按单扣费';
