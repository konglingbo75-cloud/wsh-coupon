# 微生活券吧 - 项目架构与实施规划

## 1. 产品定位与核心价值

### 1.1 产品愿景

**微生活券吧**是一个服务于餐饮邻里经济的会员权益聚合平台，定位于帮助消费者"用好已有的资产权益"，帮助商家"激活沉睡会员、提升复购"，构建区别于抖音、美团、大众点评、淘宝等大平台的差异化生态。

### 1.2 消费者价值

| 痛点 | 解决方案 |
|------|---------|
| 权益资产散落在各品牌，难以统一管理 | **一站式聚合展示**：积分、储值余额、消费记录、发票状态等，一个入口全掌握 |
| 积分/储值/优惠券因遗忘而过期浪费 | **智能提醒系统**：即将过期的权益主动推送，避免白白浪费 |
| 经济下行期追求高性价比消费 | **权益变现助手**：展示可兑换、可使用的权益，让每一分钱都花得值 |
| 不知道附近哪些店有优惠 | **邻里优惠发现**：基于位置推荐附近商户的优惠活动 |

**核心理念**：让消费者的每一份权益都不被遗忘，每一分储值都能花出去，每一个积分都能换到实惠。

### 1.3 商家价值

| 痛点 | 解决方案 |
|------|---------|
| 大量会员沉睡，无法有效触达 | **沉睡会员唤醒**：精准识别长期未消费会员，定向发送专属优惠 |
| 新店开业、新品上市缺乏曝光渠道 | **场景化营销**：开新店、发新品、做节日活动时，向目标会员推送 |
| 在大平台上获客成本越来越高 | **私域会员运营**：基于已有会员做深度运营，提升复购而非拉新 |
| 难以了解会员的真实活跃度 | **会员活跃度分析**：清晰看到活跃/沉睡会员分布，辅助运营决策 |

**核心理念**：激活沉睡会员为主，发展新会员为辅，用存量带增量。

### 1.4 差异化定位

```
┌─────────────────────────────────────────────────────────────────┐
│                      平台竞争格局                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   抖音/美团/大众点评/淘宝        vs        微生活券吧              │
│   ┌─────────────────────┐         ┌─────────────────────┐      │
│   │ 流量型平台           │         │ 权益型平台           │      │
│   │ - 拉新获客           │         │ - 激活存量           │      │
│   │ - 低价引流           │         │ - 用好已有权益       │      │
│   │ - 平台抽佣高         │         │ - 邻里经济          │      │
│   │ - 适合大品牌         │         │ - 适合社区餐饮       │      │
│   └─────────────────────┘         └─────────────────────┘      │
│                                                                 │
│   目标用户：追求理性、高性价比消费的消费者                          │
│   目标商户：追求提升复购率的社区餐饮商家                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.5 商业模式

微生活券吧采用**服务费 + 入驻费**双重盈利模式：

```
┌─────────────────────────────────────────────────────────────────┐
│                        平台收入来源                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────────────────────┐                              │
│  │  1. 交易服务费（按笔抽佣）      │                              │
│  ├───────────────────────────────┤                              │
│  │ 收取方式：消费者线上交易金额     │                              │
│  │          × 服务费率（可配置）    │                              │
│  │ 费率范围：1% ~ 3%（每商户独立） │                              │
│  │ 扣除时机：消费者到店核销时       │                              │
│  │          从交易金额中扣除留存    │                              │
│  │          剩余金额分账给商户      │                              │
│  │ 结算方式：微信支付分账          │                              │
│  └───────────────────────────────┘                              │
│                                                                 │
│  ┌───────────────────────────────┐                              │
│  │  2. 商户入驻费（年费制）        │                              │
│  ├───────────────────────────────┤                              │
│  │ 收费标准：                      │                              │
│  │   按门店：约 2,000 元/店/年     │                              │
│  │   按品牌：约 1万 ~ 5万 元/年    │                              │
│  │          （可按商户独立配置）    │                              │
│  │ 支付方式：商户在小程序内        │                              │
│  │          直接在线支付开通        │                              │
│  │ 到期处理：到期前提醒续费        │                              │
│  │          逾期后商户功能冻结      │                              │
│  └───────────────────────────────┘                              │
│                                                                 │
│  资金流向示例（消费者购买 100 元代金券，服务费率 2%）：             │
│  ┌──────────┐  支付100元  ┌──────────┐  核销时分账 ┌──────────┐ │
│  │  消费者   │ ─────────→ │  平台代收  │ ────────→ │  商户    │ │
│  └──────────┘             └──────────┘            └──────────┘ │
│                           平台留存 2元               商户到账 98元│
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**关键设计原则**：
- 服务费在**核销时**才扣除并分账，未核销的订单不产生服务费
- 入驻费独立于交易服务费，是商户使用平台的基础门槛
- 商户可在商户管理页面查看每笔服务费扣除记录和入驻费缴纳记录

## 2. 确认的技术选型

| 项目 | 选型 |
|------|------|
| 后端框架 | Java 17 + Spring Boot 3.x + MyBatis-Plus |
| 小程序框架 | uni-app 3.x (Vue 3 + Composition API) |
| 数据库 | MySQL 8.0 + Redis 6.x |
| RPA | Python 3.10+ + Playwright（二期） |
| 支付 | 微信支付分账 |
| 小程序形态 | 单小程序多角色（消费者 + 商家员工 + 商户管理） |
| 管理后台（商户端） | 小程序内嵌商户管理页面（非独立 Web） |
| 平台运营后台 | 独立 Web 应用：Vue 3 + Vite + Element Plus + Pinia + TypeScript |
| 团购提取 | 手工创建（一期）/ 美团API（二期） |
| 架构模式 | 模块化单体（MVP），预留微服务拆分能力 |
| 商户规模 | 架构从一开始支持大规模商户入驻 |

## 3. 会员数据模式（混合模式）

```
┌─────────────────────────────────────────────────────────────────┐
│                       会员数据双轨制                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────┐    ┌─────────────────────────┐   │
│  │    商户会员数据（聚合）    │    │   平台会员数据（独立）    │   │
│  ├─────────────────────────┤    ├─────────────────────────┤   │
│  │ 来源：商户现有系统         │    │ 来源：平台自身记录        │   │
│  │ 方式：API对接/手动导入(一期) │    │ 内容：平台购买记录        │   │
│  │       RPA抓取(二期)         │
│  │ 内容：                    │    │      - 代金券购买        │   │
│  │   - 会员等级              │    │      - 储值充值          │   │
│  │   - 积分余额              │    │      - 积分兑换          │   │
│  │   - 储值余额              │    │ 目的：                   │   │
│  │   - 消费记录              │    │   - 平台会员等级（预留）  │   │
│  │   - 发票状态              │    │   - 消费频次分析         │   │
│  │   - 过期时间              │    │   - 未来权益设计         │   │
│  └─────────────────────────┘    └─────────────────────────┘   │
│                                                                 │
│                    两套体系完全独立，互不干扰                       │
└─────────────────────────────────────────────────────────────────┘
```

## 4. 系统架构

```
┌───────────────────────────────────────────────────────────────────┐
│                    uni-app 微信小程序（单程序三角色）                  │
│  ┌───────────────┐ ┌────────────┐ ┌────────────┐ ┌───────────┐  │
│  │   消费者页面    │ │  商家核销   │ │ 商户管理    │ │ 权益提醒   │  │
│  │ - 权益资产总览  │ │ - 扫码核销  │ │ - 沉睡唤醒  │ │ - 过期预警 │  │
│  │ - 各店会员详情  │ │ - 核销记录  │ │ - 活动发布  │ │ - 消息推送 │  │
│  │ - 邻里活动发现  │ │            │ │ - 会员分析  │ │           │  │
│  └───────────────┘ └────────────┘ └────────────┘ └───────────┘  │
└──────────────────────────┬────────────────────────────────────────┘
                           │ HTTPS REST API
┌──────────────────────────▼────────────────────────────────────────┐
│                  Spring Boot 模块化单体服务                          │
│  ┌──────────────┐ ┌──────────────┐ ┌────────────────────────┐    │
│  │   用户服务    │ │   商户服务    │ │     平台会员服务        │    │
│  ├──────────────┤ ├──────────────┤ ├────────────────────────┤    │
│  │   活动服务    │ │   订单服务    │ │     支付/分账服务       │    │
│  ├──────────────┤ ├──────────────┤ ├────────────────────────┤    │
│  │   核销服务    │ │   聚合服务    │ │     RPA调度服务(二期)   │    │
│  ├──────────────┤ ├──────────────┤ ├────────────────────────┤    │
│  │ 权益提醒服务  │ │ 沉睡唤醒服务  │ │     会员分析服务       │    │
│  └──────────────┘ └──────────────┘ └────────────────────────┘    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              集成适配层 (Adapter Pattern)                    │  │
│  │   API适配器  │  RPA适配器(二期)  │  美团团购适配器(二期)  │  消息推送  │  │
│  └────────────────────────────────────────────────────────────┘  │
└────────────┬──────────────────────────────────┬──────────────────┘
             │                                  │ HTTP
┌────────────▼────────────┐       ┌────────────▼──────────────────┐
│    MySQL + Redis        │       │       Python RPA 服务（二期）   │
│    (主从 + 缓存)         │       │    Flask + Playwright          │
└─────────────────────────┘       │    + Celery 任务队列           │
                                  └─────────────────────────────────┘
```

## 5. 核心功能模块

### 5.1 消费者端功能

| 模块 | 功能 | 产品价值 |
|------|------|---------|
| **权益资产总览** | 汇总展示用户在所有商户的权益价值（积分可兑换价值 + 储值余额 + 优惠券价值） | 一眼看清"我还有多少钱可以花" |
| **过期预警** | 即将过期的积分、储值、优惠券提醒（7天/3天/1天） | 避免权益白白浪费 |
| **商户会员详情** | 单个商户的会员等级、积分、余额、消费记录、发票状态 | 随时查看，心中有数 |
| **专属活动发现** | 根据会员状态（活跃/沉睡）展示商户针对该状态的专属活动 | 发现被遗忘的专属优惠 |
| **消费足迹** | 历史消费过的商户列表、消费次数、总金额 | 回顾消费历史，发现常去的店 |
| **邻里活动** | 基于位置推荐附近商户的优惠活动 | 发现身边的实惠 |
| **同城活动广场** | 未授权手机号或非会员用户，按活动类型（代金券/储值/积分兑换/团购）分类Tab展示同城所有入驻商户的公开活动；基于GPS定位自动识别城市 | 吸引新用户参与，降低使用门槛，无需注册即可发现优惠 |
| **活动参与** | 代金券购买、储值充值、积分兑换、团购 | 一站式参与各类优惠 |
| **我的券包** | 已购买的券码管理、使用状态、二维码展示 | 方便使用，不怕找不到 |
| **发票管理** | 查看各商户消费的发票状态（未开/已开/开票中）、下载电子发票、申请开票 | 消费凭证一站管理 |
| **AI智能助手** | 基于大语言模型的智能客服，支持权益咨询、活动推荐、订单查询等自然语言交互 | 更便捷的消费体验 |

### 5.2 商家端功能

| 模块 | 功能 | 产品价值 |
|------|------|---------|
| **会员触达统计** | 查看通过微生活券吧触达的沉睡会员数量、唤醒成功率 | 了解平台带来的会员激活效果 |
| **会员活跃度分析** | 活跃/普通/沉睡会员在平台上的分布（从商户系统同步） | 数据驱动运营决策 |
| **活动同步管理** | 查看从商户系统同步到平台的活动列表、同步状态 | 确保活动信息准确展示 |
| **订单与核销** | 订单查看、扫码核销、核销记录 | 完成交易闭环 |
| **数据概览** | 活动销量、核销率、会员增长等关键指标 | 一目了然经营状况 |
| **入驻费管理** | 在线支付入驻费开通/续费，查看缴费记录、到期时间 | 自助开通，方便续费 |
| **服务费账单** | 查看每笔交易的服务费扣除明细、月度汇总 | 费用透明，心中有数 |

**说明**：商家在微生活券吧平台主要是**查看数据**和**完成核销**，活动的创建和管理仍在商户自己的会员系统中进行，微生活券吧通过 API 提取活动信息（一期）；RPA 抓取（二期）。商户也可在平台手工创建活动（如团购）。

### 5.3 平台运营后台功能（已实现）

平台运营后台是独立于小程序的 Web 管理系统，供平台运营人员使用，用于管理入驻商户、监控平台数据、审核商户资质等。

| 模块 | 功能 | 产品价值 |
|------|------|---------|
| **仪表盘** | 商户数/用户数/活动数/订单数/入驻费收入/服务费收入/核销率等 12 项核心指标 | 一目了然平台经营全貌 |
| **商户管理** | 商户列表（搜索/状态筛选）、商户详情（基本信息/门店/费用/审核日志）、入驻审核（通过/驳回）、状态管理（冻结/解冻） | 全生命周期管理入驻商户 |
| **用户管理** | 用户列表（分页）、消费指标（消费商户数/消费次数/消费金额） | 了解平台用户消费画像 |
| **活动管理** | 活动列表、类型/状态展示、库存/销量监控 | 监控平台全部活动运营情况 |
| **费用管理** | 入驻费记录列表（商户/套餐/金额/支付状态/有效期） | 追踪平台入驻费收入 |
| **操作日志** | 管理员操作审计日志（模块/动作/目标/IP/时间） | 运营行为可追溯 |
| **AI模型管理** | AI服务商配置（OpenAI/Claude/通义千问等）、模型选择、API Key管理、调用计费统计、费率设置 | 智能化服务可控可计费 |

**技术架构**：
- 前端：Vue 3 + TypeScript + Vite（端口 3001）+ Element Plus UI + Pinia 状态管理
- 后端：复用 wsh-backend 服务，新增 `/v1/admin/**` 系列 API
- 认证：独立管理员认证体系，JWT Token（tokenType=admin），Spring Security 角色 `ROLE_PLATFORM_ADMIN`
- 安全：所有管理接口 `@PreAuthorize("hasRole('PLATFORM_ADMIN')")`，操作日志自动记录

### 5.4 权益提醒系统

```
┌─────────────────────────────────────────────────────────────────┐
│                      权益提醒触发规则                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐                                               │
│  │ 定时扫描任务 │  每日凌晨扫描所有用户的权益数据                   │
│  └──────┬──────┘                                               │
│         │                                                       │
│         ▼                                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    检查规则                               │   │
│  │  1. 积分即将过期（7天/3天/1天内）                          │   │
│  │  2. 储值余额长期未使用（30天未消费且余额>0）                 │   │
│  │  3. 优惠券即将过期（7天/3天/1天内）                        │   │
│  │  4. 会员等级即将降级（消费不足预警）                        │   │
│  └──────┬──────────────────────────────────────────────────┘   │
│         │                                                       │
│         ▼                                                       │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │ 微信订阅消息 │    │ 小程序消息   │    │ 站内消息     │        │
│  │ (需用户授权) │    │ (服务通知)   │    │ (进入时提示) │        │
│  └─────────────┘    └─────────────┘    └─────────────┘        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.5 会员匹配与沉睡唤醒系统

**核心机制**：消费者登录微生活券吧后，平台通过**手机号**去已对接的商户系统中匹配会员身份，自动发现消费者散落在各商户的会员权益，并提取商户系统中针对该会员状态（活跃/沉睡）的专属活动。

```
┌─────────────────────────────────────────────────────────────────┐
│                    会员匹配与沉睡唤醒流程                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  消费者登录微生活券吧                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  1. 用户授权手机号                                         │   │
│  │     └→ 获取用户手机号（微信授权或手动输入）                   │   │
│  │                                                           │   │
│  │  2. 遍历已对接的商户系统，用手机号匹配会员                    │   │
│  │     └→ 调用商户API / 手动导入（一期）                        │   │
│  │     └→ 查询该手机号是否是商户的会员                          │   │
│  │                                                           │   │
│  │  3. 匹配成功后，提取会员数据                                 │   │
│  │     ├→ 会员等级、积分、储值余额、消费记录、发票状态           │   │
│  │     └→ 会员状态：活跃会员 or 沉睡会员（商户系统判定）          │   │
│  │                                                           │   │
│  │  4. 提取商户系统中针对该会员的专属活动                        │   │
│  │     ├→ 如果是活跃会员 → 提取"活跃会员专属活动"               │   │
│  │     └→ 如果是沉睡会员 → 提取"沉睡会员唤醒活动"               │   │
│  │                                                           │   │
│  │  5. 聚合展示给消费者                                        │   │
│  │     └→ "您是XX餐厅的沉睡会员，有专属优惠等您领取"             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     核心价值                               │   │
│  │  消费者可能自己都不知道自己是某商户的沉睡会员                  │   │
│  │  微生活券吧帮消费者"发现"这些被遗忘的会员身份和专属权益        │   │
│  │  让消费者不错过任何一个可以享受优惠的机会                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**活动来源说明**：沉睡会员专属活动是商户在其自有会员系统中已配置的，微生活券吧通过 API 提取后展示（一期），RPA 抓取（二期）。平台也支持商户手工创建活动（如团购），平台不额外自动创建活动，只做聚合展示。

## 6. 数据库核心表设计

### 6.1 用户域

```sql
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
);

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
);

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
);
```

### 6.2 商户域

```sql
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
    profit_sharing_rate  DECIMAL(5,4) DEFAULT 0.02 COMMENT '交易服务费率（如0.02=2%，可配置1%-3%）',
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_location(latitude, longitude)
);

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
);

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
);
```

### 6.3 商户会员聚合数据

```sql
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
);

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
);

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
);
```

### 6.4 活动域

```sql
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
);
```

**说明**：活动从商户系统同步或手工创建而来，`target_member_type` 标记该活动面向的会员类型（活跃会员专属/沉睡会员专属/全部会员），`is_public` 标记活动是否对非会员公开可见（商户控制），平台根据消费者的会员状态筛选展示对应的活动。

活动 `config` JSON 结构示例：
```json
// 代金券
{"voucher_value": 100, "selling_price": 80, "min_consume": 200, "valid_days": 30}
// 储值
{"recharge_amount": 500, "gift_amount": 50}
// 积分兑换
{"points_required": 1000, "product_name": "咖啡券"}
// 团购（一期手工创建，二期对接美团）
{"original_price": 188, "group_price": 88, "source": "manual", "valid_days": 30}
// 沉睡唤醒
{"voucher_value": 50, "selling_price": 30, "target_dormancy": [2,3], "valid_days": 14}
```

### 6.5 权益提醒表

```sql
-- 权益提醒记录（避免重复提醒）
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
);
```

### 6.6 订单与支付域

```sql
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
);

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
);

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
);

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
);
```

### 6.7 集成与 RPA

```sql
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
);

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
);
```

### 6.8 会员分析表（商户端使用）

```sql
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
);
```

### 6.9 费用与计费域

```sql
-- 入驻费配置表（支持按门店/按品牌等多种计费方式）
CREATE TABLE tb_onboarding_fee_plan (
    plan_id          BIGINT PRIMARY KEY,
    plan_name        VARCHAR(64) NOT NULL COMMENT '套餐名称，如"单店年费""品牌年费"',
    plan_type        TINYINT COMMENT '1-按门店 2-按品牌',
    fee_amount       DECIMAL(10,2) NOT NULL COMMENT '费用金额（元）',
    duration_months  INT DEFAULT 12 COMMENT '有效期（月）',
    description      TEXT,
    status           TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP
);

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
);

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
);
```

### 6.10 平台运营后台域（已实现）

```sql
-- 平台管理员表
CREATE TABLE tb_admin_user (
    admin_id     BIGINT PRIMARY KEY,
    username     VARCHAR(32) NOT NULL UNIQUE COMMENT '登录账号',
    password     VARCHAR(128) NOT NULL COMMENT 'BCrypt加密密码',
    real_name    VARCHAR(32) COMMENT '真实姓名',
    role         TINYINT DEFAULT 1 COMMENT '管理员角色（预留分级）',
    status       TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    last_login   TIMESTAMP COMMENT '最近登录时间',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商户审核日志表
CREATE TABLE tb_merchant_audit_log (
    log_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id   BIGINT NOT NULL,
    admin_id      BIGINT NOT NULL COMMENT '操作管理员ID',
    action        VARCHAR(32) NOT NULL COMMENT 'APPROVE/REJECT/FREEZE/UNFREEZE',
    prev_status   TINYINT COMMENT '操作前状态',
    new_status    TINYINT COMMENT '操作后状态',
    reason        VARCHAR(512) COMMENT '审核/操作原因',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant(merchant_id),
    INDEX idx_admin(admin_id)
);

-- 管理员操作日志表
CREATE TABLE tb_admin_operation_log (
    log_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id    BIGINT NOT NULL,
    module      VARCHAR(32) NOT NULL COMMENT '操作模块：merchant/user/activity/billing',
    action      VARCHAR(64) NOT NULL COMMENT '操作动作',
    target_id   BIGINT COMMENT '操作目标ID',
    detail      VARCHAR(512) COMMENT '操作详情',
    ip          VARCHAR(64) COMMENT '操作IP地址',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin(admin_id),
    INDEX idx_module(module)
);
```

### 6.11 AI服务与发票域

```sql
-- AI模型配置表
CREATE TABLE tb_ai_model_config (
    config_id        BIGINT PRIMARY KEY,
    provider_code    VARCHAR(32) NOT NULL COMMENT '服务商编码：openai/claude/qwen/deepseek',
    provider_name    VARCHAR(64) NOT NULL COMMENT '服务商名称',
    model_name       VARCHAR(64) NOT NULL COMMENT '模型名称：gpt-4/claude-3/qwen-turbo',
    api_endpoint     VARCHAR(255) COMMENT 'API端点URL',
    api_key          VARCHAR(512) COMMENT '加密存储的API Key',
    is_default       TINYINT DEFAULT 0 COMMENT '是否默认模型 0否 1是',
    status           TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    input_price      DECIMAL(10,6) COMMENT '输入价格（元/千token）',
    output_price     DECIMAL(10,6) COMMENT '输出价格（元/千token）',
    max_tokens       INT DEFAULT 4096 COMMENT '最大token数',
    temperature      DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_provider(provider_code),
    INDEX idx_default(is_default, status)
);

-- AI对话会话表
CREATE TABLE tb_ai_conversation (
    conversation_id  BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    title            VARCHAR(128) COMMENT '会话标题（自动从首条消息提取）',
    model_config_id  BIGINT COMMENT '使用的模型配置',
    message_count    INT DEFAULT 0 COMMENT '消息数量',
    total_tokens     INT DEFAULT 0 COMMENT '累计token数',
    total_cost       DECIMAL(10,4) DEFAULT 0 COMMENT '累计费用（元）',
    status           TINYINT DEFAULT 1 COMMENT '0已删除 1正常',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_created(created_at)
);

-- AI对话消息表
CREATE TABLE tb_ai_message (
    message_id       BIGINT PRIMARY KEY,
    conversation_id  BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    role             VARCHAR(16) NOT NULL COMMENT 'user/assistant/system',
    content          TEXT NOT NULL COMMENT '消息内容',
    input_tokens     INT DEFAULT 0 COMMENT '输入token数',
    output_tokens    INT DEFAULT 0 COMMENT '输出token数',
    cost             DECIMAL(10,6) DEFAULT 0 COMMENT '本条消息费用',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation(conversation_id),
    INDEX idx_user(user_id)
);

-- AI调用日账单表（按日汇总）
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
);

-- 发票信息表（从商户系统同步）
CREATE TABLE tb_invoice (
    invoice_id       BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    merchant_id      BIGINT NOT NULL,
    consume_record_id BIGINT COMMENT '关联消费记录ID',
    invoice_no       VARCHAR(64) COMMENT '发票号码',
    invoice_code     VARCHAR(32) COMMENT '发票代码',
    invoice_type     TINYINT DEFAULT 1 COMMENT '1电子普票 2电子专票 3纸质普票',
    invoice_status   TINYINT DEFAULT 0 COMMENT '0未开 1已开 2开票中 3开票失败',
    invoice_amount   DECIMAL(10,2) COMMENT '发票金额',
    invoice_title    VARCHAR(128) COMMENT '发票抬头',
    tax_number       VARCHAR(32) COMMENT '税号',
    invoice_url      VARCHAR(512) COMMENT '电子发票下载链接',
    invoice_date     DATE COMMENT '开票日期',
    request_time     DATETIME COMMENT '申请开票时间',
    complete_time    DATETIME COMMENT '开票完成时间',
    sync_time        DATETIME COMMENT '最近同步时间',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_merchant(merchant_id),
    INDEX idx_status(invoice_status)
);

-- 用户发票抬头设置表
CREATE TABLE tb_user_invoice_setting (
    setting_id       BIGINT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    title_type       TINYINT DEFAULT 1 COMMENT '1个人 2企业',
    invoice_title    VARCHAR(128) NOT NULL COMMENT '发票抬头',
    tax_number       VARCHAR(32) COMMENT '税号（企业必填）',
    bank_name        VARCHAR(64) COMMENT '开户银行',
    bank_account     VARCHAR(32) COMMENT '银行账号',
    company_address  VARCHAR(255) COMMENT '公司地址',
    company_phone    VARCHAR(16) COMMENT '公司电话',
    is_default       TINYINT DEFAULT 0 COMMENT '是否默认 0否 1是',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user(user_id),
    INDEX idx_default(user_id, is_default)
);
```

### 6.12 多租户隔离方案

采用**租户ID字段隔离**：所有业务表通过 `merchant_id` 区分数据，MyBatis 拦截器自动注入过滤条件。

## 7. 后端项目结构

采用**模块化单体架构**，Maven 多模块组织，为后续拆分微服务预留边界。

```
wsh-backend/
├── pom.xml                          (父 POM，统一依赖版本管理)
│
├── wsh-common/                      (公共模块)
│   ├── common-core/                 (工具类、统一响应R、全局异常、雪花ID)
│   ├── common-redis/                (Redis 封装)
│   ├── common-security/             (JWT + Spring Security)
│   └── common-mybatis/              (MyBatis 配置、租户拦截器、代码生成)
│
├── wsh-service/                     (业务服务，编译为单体 JAR 部署)
│   ├── src/main/java/com/wsh/
│   │   ├── WshApplication.java      (启动类)
│   │   │
│   │   ├── user/                    (用户服务：微信登录、用户管理)
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── mapper/
│   │   │   └── domain/
│   │   │
│   │   ├── merchant/                (商户服务：入驻、门店、员工)
│   │   │
│   │   ├── member/                  (平台会员服务：购买记录统计)
│   │   │
│   │   ├── equity/                  (权益服务)
│   │   │   ├── controller/
│   │   │   │   └── EquityController.java      (权益资产总览接口)
│   │   │   ├── service/
│   │   │   │   ├── EquitySummaryService.java  (权益汇总计算)
│   │   │   │   └── EquityReminderService.java (过期提醒)
│   │   │   └── job/
│   │   │       ├── EquityScanJob.java         (每日权益扫描)
│   │   │       └── ReminderSendJob.java       (提醒发送任务)
│   │   │
│   │   ├── matching/               (会员匹配服务 - 核心)
│   │   │   ├── controller/
│   │   │   │   └── MatchingController.java    (触发会员匹配)
│   │   │   ├── service/
│   │   │   │   ├── MemberMatchingService.java (手机号匹配会员)
│   │   │   │   └── ActivityMatchingService.java (匹配专属活动)
│   │   │   └── job/
│   │   │       └── MemberSyncJob.java         (定时同步会员数据)
│   │   │
│   │   ├── activity/                (活动服务：同步 + 展示)
│   │   │   ├── service/
│   │   │   │   ├── ActivitySyncService.java   (从商户系统同步活动)
│   │   │   │   ├── ActivityDisplayService.java (根据会员状态筛选展示)
│   │   │   │   └── PublicActivityService.java  (公开活动查询，支持同城筛选)
│   │   │   └── strategy/
│   │   │       ├── PointsExchangeStrategy.java
│   │   │       ├── RechargeStrategy.java
│   │   │       ├── VoucherStrategy.java
│   │   │       └── GroupBuyStrategy.java
│   │   │
│   │   ├── order/                   (订单服务：创建、状态流转)
│   │   │
│   │   ├── payment/                 (支付服务：微信支付 + 分账)
│   │   │
│   │   ├── billing/                 (计费服务 - 新增)
│   │   │   ├── controller/
│   │   │   │   └── BillingController.java        (入驻费/服务费接口)
│   │   │   ├── service/
│   │   │   │   ├── OnboardingFeeService.java     (入驻费管理：支付、续费、到期检查)
│   │   │   │   ├── ServiceFeeService.java        (服务费计算与扣除)
│   │   │   │   └── ServiceFeeRecordService.java  (服务费账单查询)
│   │   │   └── job/
│   │   │       └── OnboardingExpireJob.java      (入驻费到期提醒/冻结任务)
│   │   │
│   │   ├── verification/            (核销服务：扫码核销 + 同步)
│   │   │
│   │   ├── aggregate/               (聚合服务：商户会员数据聚合展示)
│   │   │
│   │   ├── integration/             (集成服务：适配器工厂 + 调度)
│   │   │   ├── adapter/
│   │   │   └── rpa/                 (二期)
│   │   │
│   │   ├── notification/            (消息通知服务 - 新增)
│   │   │   ├── service/
│   │   │   │   ├── WechatSubscribeService.java  (微信订阅消息)
│   │   │   │   └── InAppMessageService.java     (站内消息)
│   │   │   └── template/
│   │   │       └── MessageTemplate.java         (消息模板)
│   │   │
│   │   └── admin/                   (平台运营后台：管理员认证、商户审核、数据统计)
│   │       ├── controller/
│   │       │   ├── AdminAuthController.java       (/v1/admin/auth/login)
│   │       │   ├── AdminMerchantController.java   (/v1/admin/merchants)
│   │       │   ├── AdminDashboardController.java  (/v1/admin/dashboard)
│   │       │   ├── AdminUserController.java       (/v1/admin/users)
│   │       │   ├── AdminActivityController.java   (/v1/admin/activities)
│   │       │   ├── AdminBillingController.java    (/v1/admin/billing)
│   │       │   └── AdminLogController.java        (/v1/admin/logs)
│   │       ├── service/
│   │       │   ├── AdminAuthService.java          (管理员认证+JWT)
│   │       │   ├── AdminMerchantService.java      (商户管理+审核)
│   │       │   ├── AdminDashboardService.java     (仪表盘统计聚合)
│   │       │   ├── AdminUserService.java          (用户列表+消费指标)
│   │       │   ├── AdminActivityService.java      (活动列表)
│   │       │   ├── AdminBillingService.java       (入驻费记录)
│   │       │   └── AdminOperationLogService.java  (操作日志记录)
│   │       ├── dto/
│   │       │   ├── AdminLoginRequest.java / AdminLoginResponse.java
│   │       │   ├── AdminMerchantListResponse.java / AdminMerchantDetailResponse.java
│   │       │   ├── MerchantAuditRequest.java / MerchantStatusUpdateRequest.java
│   │       │   ├── DashboardStatsResponse.java
│   │       │   ├── AdminUserListResponse.java
│   │       │   ├── AdminActivityListResponse.java
│   │       │   ├── AdminBillingListResponse.java
│   │       │   └── AdminOperationLogResponse.java
│   │       └── mapper/
│   │           ├── AdminUserMapper.java
│   │           ├── MerchantAuditLogMapper.java
│   │           └── AdminOperationLogMapper.java
│   │
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── mapper/
│
└── sql/
    ├── schema.sql
    └── data-init.sql
```

## 8. 小程序项目结构

```
wsh-miniapp/
├── package.json
├── manifest.json
├── pages.json
│
├── src/
│   ├── main.js
│   ├── App.vue
│   │
│   ├── pages/                       (主包 - 公共页面)
│   │   ├── index/index.vue          (首页：权益概览 + 邻里活动)
│   │   ├── login/index.vue          (微信登录)
│   │   └── tabbar/
│   │       ├── home/index.vue       (首页 Tab - 权益资产总览)
│   │       ├── activity/index.vue   (活动列表 Tab - 同城活动广场/邻里优惠)
│   │       └── mine/index.vue       (我的 Tab)
│   │
│   ├── subPackages/
│   │   ├── consumer/                (消费者分包)
│   │   │   ├── equity/              (权益中心 - 新增)
│   │   │   │   ├── summary.vue      (权益资产总览：总积分价值+总储值+总券价值)
│   │   │   │   └── expiring.vue     (即将过期权益列表)
│   │   │   ├── member/
│   │   │   │   ├── list.vue         (我在各商户的会员列表)
│   │   │   │   └── detail.vue       (某商户会员详情)
│   │   │   ├── activity/
│   │   │   │   ├── voucher.vue
│   │   │   │   ├── recharge.vue
│   │   │   │   ├── points.vue
│   │   │   │   ├── groupbuy.vue
│   │   │   │   └── awake.vue        (沉睡唤醒专属优惠 - 新增)
│   │   │   ├── nearby/              (邻里发现 - 新增)
│   │   │   │   └── list.vue         (附近商户优惠列表)
│   │   │   ├── public/              (同城活动广场 - 新增)
│   │   │   │   └── plaza.vue        (公开活动广场页，按类型分Tab)
│   │   │   ├── order/
│   │   │   │   ├── confirm.vue
│   │   │   │   ├── list.vue
│   │   │   │   └── detail.vue
│   │   │   ├── voucher/
│   │   │   │   ├── list.vue
│   │   │   │   └── detail.vue
│   │   │   └── message/             (消息中心 - 新增)
│   │   │       └── list.vue         (权益提醒消息列表)
│   │   │
│   │   ├── merchant/                (商家核销分包)
│   │   │   ├── verification/
│   │   │   │   ├── scan.vue
│   │   │   │   └── record.vue
│   │   │   └── employee/
│   │   │       └── bind.vue
│   │   │
│   │   └── admin/                   (商户管理分包)
│   │       ├── dashboard.vue        (数据概览 - 含会员触达统计)
│   │       ├── activity/
│   │       │   └── list.vue         (已同步活动列表，查看同步状态)
│   │       ├── member/
│   │       │   └── stats.vue        (会员活跃度统计：从平台触达的会员数据)
│   │       ├── billing/             (费用管理 - 新增)
│   │       │   ├── onboarding.vue   (入驻费缴纳/续费)
│   │       │   └── service-fee.vue  (服务费扣除明细/月度账单)
│   │       ├── order/list.vue
│   │       └── settings.vue
│   │
│   ├── components/
│   │   ├── equity-summary-card.vue  (权益汇总卡片 - 新增)
│   │   ├── expiring-alert.vue       (过期预警横幅 - 新增)
│   │   ├── dormancy-chart.vue       (沉睡分布图表 - 新增)
│   │   ├── activity-card.vue
│   │   ├── member-card.vue
│   │   ├── voucher-card.vue
│   │   └── empty-state.vue
│   │
│   ├── api/
│   │   ├── request.js
│   │   ├── auth.js
│   │   ├── equity.js                (权益接口 - 新增)
│   │   ├── dormancy.js              (沉睡唤醒接口 - 新增)
│   │   ├── public.js                (公共活动接口 - 新增，无需登录)
│   │   ├── merchant.js
│   │   ├── activity.js
│   │   ├── order.js
│   │   ├── billing.js               (入驻费/服务费接口 - 新增)
│   │   └── verification.js
│   │
│   ├── store/
│   │   ├── user.js
│   │   ├── equity.js                (权益状态 - 新增)
│   │   └── app.js
│   │
│   ├── utils/
│   │   ├── auth.js
│   │   ├── payment.js
│   │   └── location.js              (位置服务 - 新增)
│   │
│   └── static/
│       └── images/
```

## 9. 平台运营后台前端结构（已实现）

```
wsh-admin/
├── package.json                      (Vue 3 + Vite + Element Plus + Pinia + TypeScript)
├── vite.config.ts                    (端口3001，代理/api→localhost:8080)
├── tsconfig.json / tsconfig.node.json
├── index.html
│
├── src/
│   ├── main.ts                       (Bootstrap：ElementPlus中文locale + Icon注册)
│   ├── App.vue                       (根组件 router-view)
│   │
│   ├── api/
│   │   ├── request.ts                (Axios封装：JWT拦截器、401处理、统一响应解析)
│   │   └── admin.ts                  (Admin API模块：login/dashboard/merchants/users等)
│   │
│   ├── stores/
│   │   └── auth.ts                   (Pinia认证状态：token/adminId/username + localStorage持久化)
│   │
│   ├── router/
│   │   └── index.ts                  (Vue Router：路由守卫 + 7个路由页面)
│   │
│   ├── layout/
│   │   └── MainLayout.vue            (侧边栏导航 + 顶部面包屑 + 用户下拉菜单)
│   │
│   └── views/
│       ├── Login.vue                 (管理员登录：用户名/密码表单)
│       ├── Dashboard.vue             (仪表盘：12项统计指标卡片)
│       ├── Merchants.vue             (商户列表：搜索/筛选/审核/冻结操作)
│       ├── MerchantDetail.vue        (商户详情：基本信息/门店/费用/审核日志)
│       ├── Users.vue                 (用户列表：消费指标)
│       ├── Activities.vue            (活动列表：类型/状态/库存)
│       ├── Billing.vue               (费用记录：入驻费/支付状态)
│       └── Logs.vue                  (操作日志：模块/动作/IP/时间)
```

## 10. RPA 服务结构（二期）

> 以下为二期实现内容，一期不涉及 RPA 服务，仅使用 API 对接和手动模式。

```
wsh-rpa/
├── app.py
├── requirements.txt
├── config/settings.py
├── core/
│   ├── browser.py
│   ├── task_queue.py
│   └── scheduler.py
├── adapters/
│   ├── base_adapter.py
│   └── meituan_adapter.py
├── extractors/
│   ├── member_extractor.py
│   └── consume_extractor.py
├── api/
│   └── routes.py
└── utils/
    └── anti_detect.py
```

## 11. 核心业务流程

### 10.1 权益资产总览流程

```
用户打开小程序首页
    → 调用 GET /v1/equity/summary
    → 后端查询 tb_user_equity_summary 缓存表
        → 若无数据或已过期，触发异步汇总计算：
            遍历用户的所有 tb_merchant_member_snapshot
            + 遍历 tb_voucher(未使用)
            → 计算总积分价值 + 总储值余额 + 总券价值
            → 写入 tb_user_equity_summary
    → 返回权益汇总数据
    → 前端展示"你的权益资产总价值: ¥XXX"
        ├── 积分可兑换: ¥XX (来自N家店)
        ├── 储值余额: ¥XX (来自N家店)
        └── 优惠券: ¥XX (N张可用)
```

### 10.2 权益过期提醒流程

```
每日凌晨定时任务 (EquityScanJob)
    → 扫描所有 tb_merchant_member_snapshot
        → 筛选 points_expire_date 在7/3/1天内的记录
    → 扫描所有 tb_voucher
        → 筛选 valid_end_time 在7/3/1天内且 status=0 的记录
    → 生成 tb_equity_reminder 记录
    → 异步发送提醒
        → 微信订阅消息（用户已授权）
        → 小程序站内消息
    → 标记 remind_status = 1
```

### 10.3 会员匹配与专属活动发现流程（核心）

```
消费者登录微生活券吧
    → 授权手机号（微信获取或手动输入）
    → 触发会员匹配任务 MemberMatchingService
        → 遍历所有已对接的商户
        → 用手机号调用商户API查询会员（一期）
        → 匹配结果：
            ├── 未匹配到 → 跳过该商户
            └── 匹配到会员 → 提取会员数据
                ├── 会员等级、积分、储值余额、消费记录
                ├── 会员状态（活跃/沉睡，商户系统判定）
                └── 存入 tb_merchant_member_snapshot
    
    → 触发活动匹配任务 ActivityMatchingService
        → 遍历匹配到的商户
        → 从商户系统提取活动列表
        → 筛选该会员可参与的活动：
            ├── 活跃会员 → 提取 target_member_type=0或1 的活动
            └── 沉睡会员 → 提取 target_member_type=0或2 的活动（沉睡专属）
        → 存入 tb_activity（标记 sync_source）
    
    → 前端展示
        → 权益资产总览（聚合所有商户）
        → 商户会员列表
        → 各商户针对该用户的专属活动
        → 沉睡会员专属提示："您是XX餐厅的老会员，有专属优惠"
```

**核心价值**：消费者可能自己都不知道自己是某商户的沉睡会员，微生活券吧帮他"发现"这些被遗忘的会员身份和专属权益。

### 10.4 微信支付与延迟分账流程

```
用户选择活动 → 创建订单(待支付)
    → 调用微信统一下单API(标记需分账)
    → 返回支付参数给小程序
    → 用户完成支付
    → 接收微信支付回调 → 更新订单(已支付) + 生成券码
    → 创建 tb_profit_sharing 记录(status=0待分账，等待核销)
    → 注意：此时不立即分账，资金由微信代管
    → 分账在消费者核销时触发（见 10.5）
```

### 10.5 核销 + 服务费扣除 + 分账流程

```
消费者出示券码(二维码) → 商家员工扫码
    → 后端验证券码有效性(未使用/未过期/对应商户)
    → 更新券状态(已使用) + 记录核销信息
    → 如果是沉睡唤醒券，更新会员 dormancy_level = 0
    → 如果商户有API对接 → 异步同步核销到商户系统
    
    → 触发分账（核销时扣除服务费）：
        → 读取商户 profit_sharing_rate（服务费率）
        → 服务费 = 订单金额 × 服务费率
        → 商户到账 = 订单金额 - 服务费
        → 写入 tb_service_fee_record（服务费扣除记录）
        → 更新 tb_profit_sharing（状态→分账中）
        → 调用微信分账API：
            商户到账金额 → 分给商户
            服务费金额 → 留存平台
        → 分账失败则进入重试队列(最多3次)
    
    → 返回核销成功
```

### 10.6 商户入驻费支付流程

```
商户管理员进入"入驻费管理"页面
    → 查看入驻费套餐列表 (tb_onboarding_fee_plan)
        ├── 单店年费：2,000 元/店/年
        └── 品牌年费：10,000 ~ 50,000 元/年
    → 选择套餐 → 创建入驻费订单 (tb_merchant_onboarding_fee)
    → 调用微信支付统一下单（普通商户收款，不走分账）
    → 商户完成支付
    → 接收支付回调：
        → 更新 pay_status = 1（已支付）
        → 设置 valid_start_date / valid_end_date
        → 更新商户 status = 1（正常）
    → 支付成功页展示有效期信息

入驻费到期处理 (OnboardingExpireJob 定时任务)：
    → 每日扫描 tb_merchant_onboarding_fee
    → 到期前30/7/1天 → 发送续费提醒
    → 到期后 → 商户 status 改为冻结
    → 冻结后商户活动不展示、无法接收新订单
```

### 10.7 商户数据同步流程

```
消费者查看某商户会员信息
    → 先查 Redis 缓存 → 有则直接返回
    → 无则查 tb_merchant_member_snapshot
    → 如果数据超过缓存时效 → 触发后台同步任务
        → 根据 integration_type 选择适配器
            → API适配器: 直接调用商户API
            → 手动模式: 返回已手动录入的快照数据
            → RPA适配器: （二期）提交Python RPA任务
        → 同步结果写入 snapshot 表 + 更新 Redis
        → 更新 dormancy_level（基于 last_consume_time 计算）
    → 返回当前快照数据 + 显示"数据更新时间"
```

### 10.8 公共活动展示流程（同城活动广场）

```
用户打开小程序（未授权手机号 / 非会员场景）
    → 获取用户GPS定位 → 解析所在城市
    → 调用 GET /v1/public/activities?city=xxx
    → 后端查询 tb_activity WHERE is_public=1 AND status=1
        AND merchant_id IN (该城市的入驻商户)
    → 按活动类型分组返回
    → 前端按类型Tab展示：
        ├── 代金券 Tab
        ├── 储值充值 Tab
        ├── 积分兑换 Tab
        └── 团购 Tab
    → 用户点击活动 → 引导授权登录后参与购买
```

**说明**：此流程面向所有用户（包括未登录用户），目的是通过展示同城商户的公开活动吸引新用户参与。活动的公开可见性由商户通过 `is_public` 字段控制。

### 10.9 AI智能助手交互流程

```
用户点击首页/我的页面的"AI助手"入口
    → 进入AI对话页面
    → 首次使用时显示功能引导：
        "我可以帮您：
         - 查询权益余额和过期时间
         - 推荐附近优惠活动
         - 查询订单状态
         - 解答平台使用问题"
    
用户输入问题（如"我有多少积分快过期了？"）
    → 前端发送 POST /v1/ai/conversations/{id}/messages
    → 后端处理：
        1. 意图识别（权益查询/活动推荐/订单查询/通用问答）
        2. 如需查询用户数据，调用内部接口获取
        3. 构建带上下文的Prompt：
           - 系统角色：微生活券吧智能助手
           - 用户画像：会员数量、权益总值、消费偏好
           - 当前问题 + 历史对话
        4. 调用配置的AI模型（通义千问/DeepSeek/OpenAI等）
        5. 流式返回回答
        6. 记录token消耗和费用
    → 前端实时展示AI回答（打字机效果）
    
AI回答示例：
    "根据查询，您有以下积分即将过期：
     🔴 星巴克：500积分（价值50元），3天后过期
     🟡 海底捞：1200积分（价值60元），7天后过期
     建议您尽快使用，点击下方链接查看可兑换商品 →"
```

**核心设计原则**：
- AI回答需结合用户实际数据，不能凭空编造
- 涉及金额、过期时间等敏感信息需从数据库实时查询
- 支持快捷操作引导（跳转到具体页面）
- Token消耗和费用由平台统一承担，在AI管理后台可监控

### 10.10 发票管理流程

```
消费者进入"我的发票"页面
    → 调用 GET /v1/invoices 获取发票列表
    → 按状态分类展示：
        ├── 待开票（可申请开票）
        ├── 开票中（等待商户处理）
        ├── 已开票（可下载/查看）
        └── 开票失败（可重新申请）

申请开票流程：
    → 用户选择待开票的消费记录
    → 选择/新增发票抬头（个人/企业）
    → 提交开票申请 POST /v1/invoices/request
    → 后端记录申请，等待商户系统处理
    → 商户系统开票完成后同步发票信息
    → 用户收到开票完成通知

查看/下载电子发票：
    → 点击已开票记录
    → 显示发票详情（发票号、金额、开票日期）
    → 提供下载链接（PDF/OFD格式）
    → 支持发送到邮箱
```

## 12. API 设计规范

- 风格：RESTful
- 认证：JWT Token (`Authorization: Bearer {token}`)
- 统一响应：`{ code, message, data, timestamp }`

### 关键接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| **用户认证** | | |
| POST | /v1/auth/wechat/login | 微信登录 |
| **权益中心（新增）** | | |
| GET | /v1/equity/summary | 权益资产总览（积分价值+储值+券价值） |
| GET | /v1/equity/expiring | 即将过期的权益列表 |
| GET | /v1/equity/messages | 权益提醒消息列表 |
| PUT | /v1/equity/notification-settings | 更新提醒设置 |
| **会员数据** | | |
| GET | /v1/users/me/members | 消费者在各商户的会员列表 |
| GET | /v1/users/me/members/{merchantId} | 某商户会员详情 |
| GET | /v1/users/me/footprint | 消费足迹 |
| **活动** | | |
| GET | /v1/activities | 活动列表(支持类型/商户/位置筛选) |
| GET | /v1/activities/{id} | 活动详情 |
| GET | /v1/activities/nearby | 附近商户活动（基于位置） |
| GET | /v1/activities/exclusive | 我的专属活动（根据会员状态筛选） |
| **公共活动（无需登录）** | | |
| GET | /v1/public/activities | 同城公开活动列表（按类型分类，基于GPS定位城市） |
| GET | /v1/public/activities/{id} | 公开活动详情 |
| GET | /v1/public/merchants | 同城入驻商户列表 |
| **订单与券** | | |
| POST | /v1/orders | 创建订单 |
| POST | /v1/orders/{id}/pay | 发起支付 |
| GET | /v1/orders | 我的订单列表 |
| GET | /v1/vouchers | 我的券包 |
| **核销（商家端）** | | |
| POST | /v1/verification/verify | 扫码核销 |
| GET | /v1/verification/records | 核销记录 |
| **会员匹配（核心）** | | |
| POST | /v1/matching/trigger | 触发会员匹配（登录后调用） |
| GET | /v1/matching/status | 查询匹配状态 |
| **商户管理（查看数据为主）** | | |
| GET | /v1/merchant/dashboard | 数据概览（会员触达、核销统计） |
| GET | /v1/merchant/activities/sync-status | 活动同步状态 |
| GET | /v1/merchant/member-stats | 会员触达统计 |
| **入驻费管理（商户端）** | | |
| GET | /v1/merchant/onboarding/plans | 入驻费套餐列表 |
| POST | /v1/merchant/onboarding/pay | 创建入驻费支付订单 |
| GET | /v1/merchant/onboarding/records | 入驻费缴纳记录 |
| GET | /v1/merchant/onboarding/status | 当前入驻状态（有效期、是否到期） |
| **服务费账单（商户端）** | | |
| GET | /v1/merchant/billing/service-fees | 服务费扣除明细列表（分页） |
| GET | /v1/merchant/billing/service-fees/summary | 服务费月度/总汇总 |
| **平台运营后台（已实现）** | | |
| POST | /v1/admin/auth/login | 管理员登录（用户名+密码，返回JWT） |
| GET | /v1/admin/dashboard/stats | 仪表盘统计（12项核心指标） |
| GET | /v1/admin/merchants?page=&size=&keyword=&status= | 商户列表（分页+搜索+状态筛选） |
| GET | /v1/admin/merchants/{id} | 商户详情（含门店/费用/审核日志） |
| POST | /v1/admin/merchants/audit | 商户审核（通过/驳回+原因） |
| POST | /v1/admin/merchants/status | 商户状态更新（冻结/解冻+原因） |
| GET | /v1/admin/users?page=&size= | 用户列表（含消费指标） |
| GET | /v1/admin/activities?page=&size= | 活动列表（含类型/状态/库存） |
| GET | /v1/admin/billing?page=&size= | 入驻费记录列表 |
| GET | /v1/admin/logs?page=&size= | 操作日志列表 |
| **发票管理（消费者端）** | | |
| GET | /v1/invoices | 我的发票列表（分页、状态筛选） |
| GET | /v1/invoices/{id} | 发票详情 |
| POST | /v1/invoices/request | 申请开票 |
| GET | /v1/invoice-settings | 获取发票抬头设置列表 |
| POST | /v1/invoice-settings | 新增发票抬头 |
| PUT | /v1/invoice-settings/{id} | 更新发票抬头 |
| DELETE | /v1/invoice-settings/{id} | 删除发票抬头 |
| PUT | /v1/invoice-settings/{id}/default | 设为默认抬头 |
| **AI智能助手（消费者端）** | | |
| POST | /v1/ai/conversations | 创建新对话 |
| GET | /v1/ai/conversations | 对话历史列表 |
| GET | /v1/ai/conversations/{id} | 获取对话详情（含消息） |
| DELETE | /v1/ai/conversations/{id} | 删除对话 |
| POST | /v1/ai/conversations/{id}/messages | 发送消息（流式返回） |
| **AI模型管理（平台后台）** | | |
| GET | /v1/admin/ai/models | AI模型配置列表 |
| POST | /v1/admin/ai/models | 新增AI模型配置 |
| PUT | /v1/admin/ai/models/{id} | 更新AI模型配置 |
| DELETE | /v1/admin/ai/models/{id} | 删除AI模型配置 |
| PUT | /v1/admin/ai/models/{id}/default | 设为默认模型 |
| GET | /v1/admin/ai/usage/daily | AI调用日统计 |
| GET | /v1/admin/ai/usage/summary | AI调用汇总统计 |

## 13. 集成适配层设计

采用**策略模式 + 工厂模式**：

```java
// 统一适配器接口
interface MerchantDataAdapter {
    // 用手机号查询会员（核心匹配接口）
    MemberMatchResult matchMemberByPhone(String phone, Long merchantId);
    
    // 同步会员数据
    MemberDataDTO syncMemberData(Long userId, Long merchantId);
    
    // 同步消费记录
    List<ConsumeRecordDTO> syncConsumeRecords(Long userId, Long merchantId);
    
    // 同步商户活动（包含活动的目标会员类型）
    List<ActivityDTO> syncActivities(Long merchantId);
    
    // 核销同步（将平台核销信息同步回商户系统）
    boolean syncVerification(VerificationDTO dto);
}

// 会员匹配结果
class MemberMatchResult {
    boolean matched;           // 是否匹配到
    String sourceMemberId;     // 商户系统会员ID
    String memberStatus;       // 活跃/沉睡（商户系统判定）
    MemberDataDTO memberData;  // 会员详细数据
}

// 适配器工厂
@Component
class AdapterFactory {
    MerchantDataAdapter getAdapter(Long merchantId);
}
```

实现类：`ApiAdapter`（调用商户API）、`ManualAdapter`（手动模式）、`RpaAdapter`（二期：调度Python RPA）。

## 14. 分步实施计划

### 第一步：基础设施搭建

- 创建 Maven 父工程 + 子模块骨架
- 创建 uni-app 项目骨架
- 公共模块：统一响应 R、全局异常处理、雪花ID、Redis 工具
- MyBatis-Plus 配置 + 租户拦截器
- JWT + Spring Security 认证框架
- 执行 schema.sql 创建数据库表

### 第二步：用户与商户基础 + 入驻费

- 微信小程序登录（code2Session + JWT）
- 用户 CRUD + 提醒设置
- 商户 CRUD + 门店管理 + 位置信息
- 商户员工管理（绑定 openid）
- **入驻费套餐管理**：tb_onboarding_fee_plan CRUD
- **商户入驻费在线支付**：选择套餐 → 微信支付 → 开通/续费
- **入驻费到期检查定时任务**（OnboardingExpireJob）
- 小程序：登录页、首页框架、TabBar、**商户入驻费缴纳页**

### 第三步：会员匹配与权益聚合（核心）

- **会员匹配服务**：用手机号去商户系统匹配会员身份
- 集成适配层基础框架（接口 + 工厂 + ManualAdapter + ApiAdapter 骨架）
- 商户会员快照同步与缓存逻辑（含会员状态：活跃/沉睡）
- 消费记录同步与发票状态
- **权益汇总服务**：计算用户全部积分价值 + 储值 + 券价值
- **tb_user_equity_summary 缓存表**维护
- 平台会员服务（购买记录统计、消费足迹）
- 小程序：**登录授权手机号**、**权益资产总览页**、我的会员列表、会员详情页

### 第四步：权益提醒系统

- **权益扫描定时任务**：每日扫描即将过期的积分/储值/券
- **tb_equity_reminder 提醒记录表**
- **微信订阅消息**集成（权益过期提醒模板）
- **站内消息**服务
- 小程序：**过期预警横幅**、**消息中心页**、提醒设置

### 第五步：活动同步与专属活动匹配

- **活动同步服务**：从商户系统同步活动（包含 target_member_type）
- **公开活动标记**：商户可设置活动 `is_public` 为公开，控制非会员可见性
- 活动列表查询（按类型/商户/位置/会员状态筛选）
- **专属活动匹配**：根据用户在各商户的会员状态，筛选展示对应的专属活动
- 小程序：活动列表页、活动详情页、**"您有专属优惠"提示**

### 第六步：邻里活动发现 + 同城活动广场

- **基于位置的活动推荐**：GET /v1/activities/nearby
- **附近商户列表**：按距离排序
- **同城公开活动广场**：GET /v1/public/activities（无需登录）
  - GPS定位 → 城市识别逻辑
  - 按活动类型（代金券/储值/积分兑换/团购）分类Tab展示
  - 商户通过 `is_public` 控制活动公开可见性
- 小程序：**邻里发现页**、**同城活动广场页**（活动Tab改为同城活动广场入口）

### 第七步：订单与支付（延迟分账模式）

- 订单创建 + 状态机
- 微信支付统一下单（标记需分账）+ 回调处理
- **延迟分账**：支付完成后不立即分账，资金由微信代管，等待核销触发
- 券码生成
- 库存管理（Redis 原子扣减）
- 超时订单定时关闭
- 小程序：订单确认、支付流程、订单列表

### 第八步：核销 + 服务费扣除

- 券码核销接口
- **核销时触发分账**：计算服务费 → 写入 tb_service_fee_record → 调用微信分账API
- **服务费账单查询**：商户可查看每笔服务费扣除明细、月度汇总
- 核销数据同步到商户系统（如有API对接）
- 分账失败重试队列
- 小程序商家分包：扫码核销、核销记录、**服务费账单页**
- 小程序消费者：我的券包、券详情+二维码

### 第九步：API 对接深化

- 实现 1 个主流餐饮系统的 API 适配器（含会员匹配、活动同步）
- 定时任务：同步商户活动到平台

### 第十步：商户管理完善 + 测试上线

- 商户管理分包完善：数据概览、活动同步状态、订单查看
- **会员触达统计**：通过平台触达的会员数、核销率
- 全流程联调测试
- 安全测试
- 性能优化
- 小程序提审 + 上线

### 第十一步：平台运营后台（已完成）

- **后端 Admin API 实现**：
  - 新增 3 张管理员相关数据库表（tb_admin_user / tb_merchant_audit_log / tb_admin_operation_log）
  - 更新安全模块支持管理员角色（role=10, ROLE_PLATFORM_ADMIN）和独立 JWT Token（tokenType=admin）
  - 实现管理员认证接口（/v1/admin/auth/login）+ BCrypt 密码加密
  - 实现 6 个管理 Controller：Dashboard / Merchants / Users / Activities / Billing / Logs
  - 实现对应 Service 层、DTO 层、Mapper 层
  - 更新 DataInitializer 自动创建默认管理员账号（admin/admin123）
  - 更新 OpenApiConfig 添加平台运营后台 API 分组
- **前端 wsh-admin 实现**：
  - Vue 3 + TypeScript + Vite + Element Plus + Pinia 技术栈
  - 管理员登录页（用户名/密码认证）
  - 主布局（侧边栏导航 + 面包屑 + 用户菜单）
  - 7 个业务页面：仪表盘 / 商户管理 / 商户详情 / 用户 / 活动 / 费用 / 操作日志
  - 路由守卫（未登录跳转登录页）+ Axios 请求拦截器（JWT 注入 + 401 处理）
- **本地开发环境验证**：
  - 后端运行于 :8080（H2 内存数据库，local profile 自动初始化）
  - 前端运行于 :3001（Vite dev server，代理 /api → :8080）
  - 所有 API 接口联调验证通过

## 二期功能规划（概述）

- **美团团购API对接**：自动提取美团平台团购活动，替代手工创建
- **RPA模块**：Python Flask + Playwright + Celery
  - 商户系统网页端数据抓取
  - 会员数据/消费记录/活动信息自动同步
  - Java端RPA调度服务
  - RPA适配器集成到现有适配层框架

## 15. 验证方案

### 本地开发验证
1. **后端启动**：`mvn spring-boot:run`，访问 Swagger 文档
2. **数据库**：执行 `sql/schema.sql` 建表
3. **小程序**：`npm run dev:mp-weixin` 编译后导入微信开发者工具
4. **平台运营后台**（已实现）：
   - 后端：`cd wsh-backend && mvn spring-boot:run -pl wsh-service -Dspring-boot.run.profiles=local`（H2 内存数据库，端口 8080）
   - 前端：`cd wsh-admin && npm run dev`（Vite dev server，端口 3001，代理 /api → :8080）
   - 默认管理员账号：`admin` / `admin123`

### 核心流程验证

1. **会员匹配验证**
   - 配置测试商户的 API 适配器
   - 用测试手机号触发会员匹配
   - 验证 tb_merchant_member_snapshot 数据写入
   - 验证会员状态（活跃/沉睡）正确识别

2. **专属活动展示验证**
   - 同步商户活动（含 target_member_type）
   - 用活跃会员身份登录，验证看到活跃专属活动
   - 用沉睡会员身份登录，验证看到沉睡专属活动

3. **权益聚合验证**
   - 创建测试用户 + 多个商户会员快照
   - 调用 GET /v1/equity/summary 验证汇总计算正确
   - 验证权益资产总览页展示正确

4. **过期提醒验证**
   - 创建即将过期的积分/券数据
   - 手动触发扫描任务
   - 验证 tb_equity_reminder 记录生成
   - 验证消息推送

5. **支付与延迟分账验证**
   - 微信支付沙箱环境测试
   - 验证支付后不立即分账（status=待分账）
   - 验证核销后触发分账逻辑

6. **服务费扣除验证**
   - 配置商户服务费率为 2%
   - 创建 100 元订单 → 支付 → 核销
   - 验证 tb_service_fee_record 记录：服务费=2元，商户到账=98元
   - 验证 tb_profit_sharing 正确调用微信分账
   - 验证分账失败重试机制
   - 商户端验证服务费扣除明细可查询

7. **入驻费验证**
   - 创建入驻费套餐（单店2000元/年）
   - 商户选择套餐 → 支付 → 验证有效期设置正确
   - 验证到期前提醒逻辑（30/7/1天）
   - 验证到期后商户状态冻结
   - 验证续费后商户恢复正常

8. **邻里发现验证**
   - 创建带位置信息的商户和活动
   - 验证 GET /v1/activities/nearby 按距离排序正确

9. **公开活动展示验证**
   - 创建商户并设置 city 字段
   - 创建活动并标记 is_public=1
   - 未登录状态调用 GET /v1/public/activities?city=xxx
   - 验证返回结果按类型正确分类
   - 验证非公开活动不会出现在结果中

10. **平台运营后台验证（已通过）**
    - 管理员登录：POST /v1/admin/auth/login 返回 JWT Token
    - 仪表盘统计：GET /v1/admin/dashboard/stats 返回 12 项指标数据
    - 商户列表：GET /v1/admin/merchants 返回分页商户数据（含门店/活动计数）
    - 商户详情：GET /v1/admin/merchants/{id} 返回完整商户信息（含门店/费用/审核日志）
    - 用户列表：GET /v1/admin/users 返回用户数据（含消费指标）
    - 活动列表：GET /v1/admin/activities 返回全量活动数据
    - 费用记录：GET /v1/admin/billing 返回入驻费记录
    - 操作日志：GET /v1/admin/logs 返回管理员操作审计记录
    - 前端页面：登录/仪表盘/商户管理/商户详情/用户/活动/费用/日志 全部页面渲染正常
    - 前后端联调：Vite 代理 /api 到后端 :8080，所有接口通信正常
