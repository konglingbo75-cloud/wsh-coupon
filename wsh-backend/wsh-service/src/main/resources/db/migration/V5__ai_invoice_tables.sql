-- ============================================================
-- AI服务与发票管理表（Flyway V5）
-- ============================================================

-- AI模型配置表
CREATE TABLE tb_ai_model_config (
    config_id        BIGINT PRIMARY KEY,
    provider_code    VARCHAR(32) NOT NULL COMMENT '供应商编码：openai/azure/claude/qwen',
    provider_name    VARCHAR(64) NOT NULL COMMENT '供应商名称',
    model_name       VARCHAR(64) NOT NULL COMMENT '模型名称',
    api_endpoint     VARCHAR(255) COMMENT 'API端点',
    api_key          VARCHAR(512) COMMENT 'API密钥（加密存储）',
    is_default       TINYINT DEFAULT 0 COMMENT '是否默认模型',
    status           TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    input_price      DECIMAL(10,6) COMMENT '输入token单价(元/千token)',
    output_price     DECIMAL(10,6) COMMENT '输出token单价(元/千token)',
    max_tokens       INT DEFAULT 4096 COMMENT '最大token数',
    temperature      DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_provider(provider_code),
    INDEX idx_default(is_default, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';

-- AI对话会话表
CREATE TABLE tb_ai_conversation (
    conversation_id  BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    title            VARCHAR(128) COMMENT '会话标题',
    model_config_id  BIGINT COMMENT '使用的模型配置ID',
    message_count    INT DEFAULT 0 COMMENT '消息数量',
    total_tokens     INT DEFAULT 0 COMMENT '总token消耗',
    total_cost       DECIMAL(10,4) DEFAULT 0 COMMENT '总费用',
    status           TINYINT DEFAULT 1 COMMENT '1正常 0已删除',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE tb_ai_message (
    message_id       BIGINT PRIMARY KEY,
    conversation_id  BIGINT NOT NULL COMMENT '会话ID',
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    role             VARCHAR(16) NOT NULL COMMENT '角色：user/assistant/system',
    content          TEXT NOT NULL COMMENT '消息内容',
    input_tokens     INT DEFAULT 0 COMMENT '输入token数',
    output_tokens    INT DEFAULT 0 COMMENT '输出token数',
    cost             DECIMAL(10,6) DEFAULT 0 COMMENT '本条消息费用',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation(conversation_id),
    INDEX idx_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';

-- AI调用日账单表
CREATE TABLE tb_ai_usage_daily (
    daily_id         BIGINT PRIMARY KEY,
    stat_date        DATE NOT NULL COMMENT '统计日期',
    model_config_id  BIGINT COMMENT '模型配置ID',
    call_count       INT DEFAULT 0 COMMENT '调用次数',
    total_input_tokens  INT DEFAULT 0 COMMENT '总输入token',
    total_output_tokens INT DEFAULT 0 COMMENT '总输出token',
    total_cost       DECIMAL(12,4) DEFAULT 0 COMMENT '总费用',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_date_model(stat_date, model_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI调用日账单表';

-- 发票信息表
CREATE TABLE tb_invoice (
    invoice_id       BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    merchant_id      BIGINT NOT NULL COMMENT '商户ID',
    consume_record_id BIGINT COMMENT '关联消费记录ID',
    invoice_no       VARCHAR(64) COMMENT '发票号码',
    invoice_code     VARCHAR(32) COMMENT '发票代码',
    invoice_type     TINYINT DEFAULT 1 COMMENT '1电子普票 2电子专票',
    invoice_status   TINYINT DEFAULT 0 COMMENT '0待开 1开票中 2已开 3失败',
    invoice_amount   DECIMAL(10,2) COMMENT '发票金额',
    invoice_title    VARCHAR(128) COMMENT '发票抬头',
    tax_number       VARCHAR(32) COMMENT '税号',
    invoice_url      VARCHAR(512) COMMENT '电子发票下载链接',
    invoice_date     DATE COMMENT '开票日期',
    request_time     DATETIME COMMENT '申请时间',
    complete_time    DATETIME COMMENT '完成时间',
    sync_time        DATETIME COMMENT '同步时间',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_merchant(merchant_id),
    INDEX idx_status(invoice_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票信息表';

-- 用户发票抬头设置表
CREATE TABLE tb_user_invoice_setting (
    setting_id       BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    title_type       TINYINT DEFAULT 1 COMMENT '1个人 2企业',
    invoice_title    VARCHAR(128) NOT NULL COMMENT '发票抬头',
    tax_number       VARCHAR(32) COMMENT '企业税号',
    bank_name        VARCHAR(64) COMMENT '开户银行',
    bank_account     VARCHAR(32) COMMENT '银行账号',
    company_address  VARCHAR(255) COMMENT '企业地址',
    company_phone    VARCHAR(16) COMMENT '企业电话',
    is_default       TINYINT DEFAULT 0 COMMENT '是否默认',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_default(user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户发票抬头设置表';
