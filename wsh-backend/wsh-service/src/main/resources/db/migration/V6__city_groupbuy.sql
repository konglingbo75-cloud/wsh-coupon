-- ============================================================
-- V6: 城市管理 + 完整拼团功能
-- ============================================================

-- 开通城市表
CREATE TABLE tb_open_city (
    city_id           BIGINT PRIMARY KEY,
    city_code         VARCHAR(32) NOT NULL UNIQUE COMMENT '城市编码(如440100)',
    city_name         VARCHAR(64) NOT NULL COMMENT '城市名称',
    province_name     VARCHAR(64) COMMENT '省份名称',
    pinyin            VARCHAR(8) COMMENT '拼音首字母(A-Z)',
    level             TINYINT DEFAULT 2 COMMENT '城市级别 1:一线 2:新一线 3:二线 4:三线',
    longitude         DECIMAL(10,6) COMMENT '城市中心经度',
    latitude          DECIMAL(10,6) COMMENT '城市中心纬度',
    merchant_count    INT DEFAULT 0 COMMENT '入驻商户数(统计)',
    activity_count    INT DEFAULT 0 COMMENT '活动数(统计)',
    status            TINYINT DEFAULT 1 COMMENT '0:未开放 1:已开放',
    sort_order        INT DEFAULT 0 COMMENT '排序权重(越大越靠前)',
    open_date         DATE COMMENT '开通日期',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_city_status ON tb_open_city(status);
CREATE INDEX idx_city_pinyin ON tb_open_city(pinyin);
CREATE INDEX idx_city_sort ON tb_open_city(sort_order DESC, merchant_count DESC);

-- 初始化主要城市数据
INSERT INTO tb_open_city (city_id, city_code, city_name, province_name, pinyin, level, longitude, latitude, status, sort_order, open_date) VALUES
(1, '440300', '深圳', '广东省', 'S', 1, 114.057868, 22.543099, 1, 100, CURRENT_DATE),
(2, '440100', '广州', '广东省', 'G', 1, 113.264385, 23.129112, 1, 99, CURRENT_DATE),
(3, '110000', '北京', '北京市', 'B', 1, 116.405285, 39.904989, 1, 98, CURRENT_DATE),
(4, '310000', '上海', '上海市', 'S', 1, 121.472644, 31.231706, 1, 97, CURRENT_DATE),
(5, '330100', '杭州', '浙江省', 'H', 2, 120.153576, 30.287459, 1, 90, CURRENT_DATE),
(6, '320100', '南京', '江苏省', 'N', 2, 118.767413, 32.041544, 1, 89, CURRENT_DATE),
(7, '510100', '成都', '四川省', 'C', 2, 104.065735, 30.659462, 1, 88, CURRENT_DATE),
(8, '500000', '重庆', '重庆市', 'C', 2, 106.504962, 29.533155, 1, 87, CURRENT_DATE),
(9, '420100', '武汉', '湖北省', 'W', 2, 114.298572, 30.584355, 1, 86, CURRENT_DATE),
(10, '610100', '西安', '陕西省', 'X', 2, 108.948024, 34.263161, 1, 85, CURRENT_DATE),
(11, '320500', '苏州', '江苏省', 'S', 2, 120.619585, 31.299379, 1, 84, CURRENT_DATE),
(12, '120000', '天津', '天津市', 'T', 2, 117.190182, 39.125596, 1, 83, CURRENT_DATE),
(13, '430100', '长沙', '湖南省', 'C', 2, 112.982279, 28.19409, 1, 82, CURRENT_DATE),
(14, '440600', '佛山', '广东省', 'F', 2, 113.122717, 23.028762, 1, 81, CURRENT_DATE),
(15, '441900', '东莞', '广东省', 'D', 2, 113.746262, 23.046237, 1, 80, CURRENT_DATE),
(16, '330200', '宁波', '浙江省', 'N', 2, 121.549792, 29.868388, 1, 79, CURRENT_DATE),
(17, '350100', '福州', '福建省', 'F', 2, 119.306239, 26.075302, 1, 78, CURRENT_DATE),
(18, '350200', '厦门', '福建省', 'X', 2, 118.11022, 24.490474, 1, 77, CURRENT_DATE),
(19, '210100', '沈阳', '辽宁省', 'S', 2, 123.429096, 41.796767, 1, 76, CURRENT_DATE),
(20, '370200', '青岛', '山东省', 'Q', 2, 120.369557, 36.094406, 1, 75, CURRENT_DATE);

-- ============================================================
-- 拼团功能表
-- ============================================================

-- 拼团配置表（扩展活动配置）
CREATE TABLE tb_group_buy_config (
    config_id          BIGINT PRIMARY KEY,
    activity_id        BIGINT NOT NULL UNIQUE COMMENT '关联活动ID',
    min_members        INT DEFAULT 2 COMMENT '最少成团人数',
    max_members        INT DEFAULT 10 COMMENT '最多成团人数',
    expire_hours       INT DEFAULT 24 COMMENT '拼团有效期(小时)',
    auto_refund        TINYINT DEFAULT 1 COMMENT '超时自动退款 0:否 1:是',
    allow_self_buy     TINYINT DEFAULT 0 COMMENT '允许凑单购买 0:否 1:是',
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_groupconfig_activity ON tb_group_buy_config(activity_id);

-- 拼团记录表
CREATE TABLE tb_group_order (
    group_order_id     BIGINT PRIMARY KEY,
    group_no           VARCHAR(32) NOT NULL UNIQUE COMMENT '拼团编号',
    activity_id        BIGINT NOT NULL,
    initiator_user_id  BIGINT NOT NULL COMMENT '发起人用户ID',
    required_members   INT NOT NULL COMMENT '成团所需人数',
    current_members    INT DEFAULT 1 COMMENT '当前参与人数',
    status             TINYINT DEFAULT 0 COMMENT '0:拼团中 1:已成团 2:已失败(超时) 3:已取消',
    expire_time        DATETIME NOT NULL COMMENT '拼团截止时间',
    complete_time      DATETIME COMMENT '成团时间',
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_grouporder_activity ON tb_group_order(activity_id);
CREATE INDEX idx_grouporder_status_expire ON tb_group_order(status, expire_time);
CREATE INDEX idx_grouporder_initiator ON tb_group_order(initiator_user_id);

-- 拼团参与者表
CREATE TABLE tb_group_participant (
    participant_id     BIGINT PRIMARY KEY,
    group_order_id     BIGINT NOT NULL,
    user_id            BIGINT NOT NULL,
    order_id           BIGINT COMMENT '关联订单ID(支付后填充)',
    is_initiator       TINYINT DEFAULT 0 COMMENT '是否发起人',
    join_time          DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_participant_group ON tb_group_participant(group_order_id);
CREATE INDEX idx_participant_user ON tb_group_participant(user_id);
CREATE UNIQUE INDEX idx_participant_group_user ON tb_group_participant(group_order_id, user_id);

-- 为订单表添加拼团关联字段
ALTER TABLE tb_order ADD COLUMN group_order_id BIGINT COMMENT '关联拼团ID(拼团订单专用)';
CREATE INDEX idx_order_group ON tb_order(group_order_id);
