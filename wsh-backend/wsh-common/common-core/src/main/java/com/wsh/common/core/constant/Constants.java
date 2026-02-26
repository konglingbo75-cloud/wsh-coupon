package com.wsh.common.core.constant;

/**
 * 系统常量定义
 */
public final class Constants {

    private Constants() {
    }

    // ========== 用户角色 ==========
    /** 普通消费者 */
    public static final int ROLE_CONSUMER = 0;
    /** 商户管理员 */
    public static final int ROLE_MERCHANT_ADMIN = 1;
    /** 商户员工（核销员） */
    public static final int ROLE_MERCHANT_STAFF = 2;
    /** 平台运营管理员 */
    public static final int ROLE_PLATFORM_ADMIN = 10;

    // ========== 商户状态 ==========
    /** 待审核 */
    public static final int MERCHANT_STATUS_PENDING = 0;
    /** 正常 */
    public static final int MERCHANT_STATUS_ACTIVE = 1;
    /** 冻结（入驻费到期） */
    public static final int MERCHANT_STATUS_FROZEN = 2;

    // ========== 订单状态 ==========
    /** 待支付 */
    public static final int ORDER_STATUS_PENDING = 0;
    /** 已支付 */
    public static final int ORDER_STATUS_PAID = 1;
    /** 已关闭 */
    public static final int ORDER_STATUS_CLOSED = 2;
    /** 已退款 */
    public static final int ORDER_STATUS_REFUNDED = 3;

    // ========== 券状态 ==========
    /** 未使用 */
    public static final int VOUCHER_STATUS_UNUSED = 0;
    /** 已使用（已核销） */
    public static final int VOUCHER_STATUS_USED = 1;
    /** 已过期 */
    public static final int VOUCHER_STATUS_EXPIRED = 2;

    // ========== 分账状态 ==========
    /** 待分账（待核销） */
    public static final int SHARING_STATUS_PENDING = 0;
    /** 分账中 */
    public static final int SHARING_STATUS_PROCESSING = 1;
    /** 已分账 */
    public static final int SHARING_STATUS_SUCCESS = 2;
    /** 分账失败 */
    public static final int SHARING_STATUS_FAILED = 3;

    // ========== 活动类型 ==========
    /** 代金券 */
    public static final int ACTIVITY_TYPE_VOUCHER = 1;
    /** 储值充值 */
    public static final int ACTIVITY_TYPE_DEPOSIT = 2;
    /** 积分兑换 */
    public static final int ACTIVITY_TYPE_POINTS = 3;
    /** 团购 */
    public static final int ACTIVITY_TYPE_GROUP = 4;

    // ========== 活动状态 ==========
    /** 草稿 */
    public static final int ACTIVITY_STATUS_DRAFT = 0;
    /** 进行中 */
    public static final int ACTIVITY_STATUS_ACTIVE = 1;
    /** 已暂停 */
    public static final int ACTIVITY_STATUS_PAUSED = 2;
    /** 已结束 */
    public static final int ACTIVITY_STATUS_ENDED = 3;

    // ========== 目标会员类型 ==========
    /** 全部会员 */
    public static final int TARGET_MEMBER_ALL = 0;
    /** 仅活跃会员 */
    public static final int TARGET_MEMBER_ACTIVE = 1;
    /** 仅沉睡会员 */
    public static final int TARGET_MEMBER_DORMANT = 2;

    // ========== 沉睡等级 ==========
    /** 活跃 */
    public static final int DORMANCY_ACTIVE = 0;
    /** 轻度沉睡（30天+） */
    public static final int DORMANCY_LIGHT = 1;
    /** 中度沉睡（60天+） */
    public static final int DORMANCY_MEDIUM = 2;
    /** 深度沉睡（90天+） */
    public static final int DORMANCY_DEEP = 3;

    // ========== 同步来源 ==========
    public static final String SYNC_SOURCE_API = "api";
    public static final String SYNC_SOURCE_MANUAL = "manual";
    public static final String SYNC_SOURCE_RPA = "rpa";
    public static final String SYNC_SOURCE_DEMO = "demo";

    // ========== 入驻费状态 ==========
    /** 待支付 */
    public static final int ONBOARDING_PAY_PENDING = 0;
    /** 已支付 */
    public static final int ONBOARDING_PAY_SUCCESS = 1;
    /** 已关闭 */
    public static final int ONBOARDING_PAY_CLOSED = 2;

    // ========== 服务费模式 ==========
    /** 预付费模式 */
    public static final int SERVICE_FEE_MODE_PREPAID = 1;
    /** 按单扣费模式 */
    public static final int SERVICE_FEE_MODE_PER_ORDER = 2;

    // ========== 余额变动类型 ==========
    /** 充值 */
    public static final int BALANCE_CHANGE_RECHARGE = 1;
    /** 扣费 */
    public static final int BALANCE_CHANGE_DEDUCT = 2;
    /** 退款 */
    public static final int BALANCE_CHANGE_REFUND = 3;

    // ========== 权益提醒类型 ==========
    /** 积分过期 */
    public static final int REMINDER_TYPE_POINTS_EXPIRE = 1;
    /** 储值余额提醒 */
    public static final int REMINDER_TYPE_BALANCE = 2;
    /** 优惠券过期 */
    public static final int REMINDER_TYPE_VOUCHER_EXPIRE = 3;
    /** 会员等级降级 */
    public static final int REMINDER_TYPE_LEVEL_DOWNGRADE = 4;
    /** 沉睡唤醒 */
    public static final int REMINDER_TYPE_DORMANCY_AWAKE = 5;

    // ========== 缓存 Key 前缀 ==========
    public static final String CACHE_USER_EQUITY = "equity:summary:";
    public static final String CACHE_PUBLIC_ACTIVITY = "activity:public:";
    public static final String CACHE_MERCHANT_DASHBOARD = "merchant:dashboard:";
    public static final String LOCK_MEMBER_SYNC = "lock:member:sync";
    public static final String LOCK_ONBOARDING_EXPIRE = "lock:onboarding:expire";
    public static final String LOCK_EQUITY_SCAN = "lock:equity:scan";

    // ========== 管理员状态 ==========
    /** 正常 */
    public static final int ADMIN_STATUS_ACTIVE = 1;
    /** 禁用 */
    public static final int ADMIN_STATUS_DISABLED = 0;

    // ========== 商户审核动作 ==========
    public static final String AUDIT_ACTION_APPROVE = "APPROVE";
    public static final String AUDIT_ACTION_REJECT = "REJECT";
    public static final String AUDIT_ACTION_FREEZE = "FREEZE";
    public static final String AUDIT_ACTION_UNFREEZE = "UNFREEZE";

    // ========== 订单超时时间（分钟） ==========
    public static final int ORDER_TIMEOUT_MINUTES = 30;

    // ========== 分账重试 ==========
    public static final int SHARING_MAX_RETRY = 3;
}
