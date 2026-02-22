package com.wsh.equity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import com.wsh.equity.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权益提醒服务
 * 负责扫描即将过期的权益（积分/储值/券）、生成提醒记录、查询提醒列表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquityReminderService {

    private final EquityReminderMapper reminderMapper;
    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final VoucherMapper voucherMapper;
    private final MerchantMapper merchantMapper;
    private final UserNotificationSettingMapper settingMapper;
    private final UserMapper userMapper;

    // ==================== 提醒扫描（供定时任务调用） ====================

    /**
     * 扫描所有用户的即将过期权益并生成提醒记录
     * 检查规则：
     *   1. 积分即将过期（7天/3天/1天内）
     *   2. 储值余额长期未使用（30天未消费且余额>0）
     *   3. 优惠券即将过期（7天/3天/1天内）
     */
    @Transactional
    public int scanAndCreateReminders() {
        LocalDate today = LocalDate.now();
        int totalCreated = 0;

        // 1. 扫描积分即将过期
        totalCreated += scanExpiringPoints(today);

        // 2. 扫描储值长期未使用
        totalCreated += scanDormantBalance(today);

        // 3. 扫描优惠券即将过期
        totalCreated += scanExpiringVouchers(today);

        return totalCreated;
    }

    /**
     * 扫描积分即将过期的会员快照
     */
    private int scanExpiringPoints(LocalDate today) {
        int created = 0;
        // 检查7天内过期
        LocalDate endDate = today.plusDays(7);
        List<MerchantMemberSnapshot> expiring = snapshotMapper.selectExpiringPoints(today, endDate);

        for (MerchantMemberSnapshot snapshot : expiring) {
            // 查用户通知设置
            if (!isNotifyEnabled(snapshot.getUserId(), "points")) {
                continue;
            }

            // 避免重复提醒
            if (reminderMapper.countDuplicate(snapshot.getUserId(), snapshot.getMerchantId(),
                    Constants.REMINDER_TYPE_POINTS_EXPIRE, snapshot.getPointsExpireDate()) > 0) {
                continue;
            }

            EquityReminder reminder = new EquityReminder();
            reminder.setUserId(snapshot.getUserId());
            reminder.setMerchantId(snapshot.getMerchantId());
            reminder.setReminderType(Constants.REMINDER_TYPE_POINTS_EXPIRE);
            reminder.setEquityType("points");
            reminder.setEquityValue(snapshot.getPointsValue());
            reminder.setExpireDate(snapshot.getPointsExpireDate());
            reminder.setRemindStatus(0);
            reminderMapper.insert(reminder);
            created++;
        }

        log.info("积分过期扫描: 发现{}条即将过期, 创建{}条提醒", expiring.size(), created);
        return created;
    }

    /**
     * 扫描储值余额长期未使用（30天未消费且余额>0）
     */
    private int scanDormantBalance(LocalDate today) {
        int created = 0;
        LocalDateTime threshold = today.minusDays(30).atStartOfDay();

        // 查询余额>0 且 30天未消费的快照
        List<MerchantMemberSnapshot> allSnapshots = snapshotMapper.selectList(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                        .eq(MerchantMemberSnapshot::getSyncStatus, 1)
                        .gt(MerchantMemberSnapshot::getBalance, BigDecimal.ZERO)
                        .isNotNull(MerchantMemberSnapshot::getLastConsumeTime)
                        .lt(MerchantMemberSnapshot::getLastConsumeTime, threshold));

        for (MerchantMemberSnapshot snapshot : allSnapshots) {
            if (!isNotifyEnabled(snapshot.getUserId(), "balance")) {
                continue;
            }

            // 用今天作为 expireDate 标识此次扫描（避免重复）
            if (reminderMapper.countDuplicate(snapshot.getUserId(), snapshot.getMerchantId(),
                    Constants.REMINDER_TYPE_BALANCE, today) > 0) {
                continue;
            }

            EquityReminder reminder = new EquityReminder();
            reminder.setUserId(snapshot.getUserId());
            reminder.setMerchantId(snapshot.getMerchantId());
            reminder.setReminderType(Constants.REMINDER_TYPE_BALANCE);
            reminder.setEquityType("balance");
            reminder.setEquityValue(snapshot.getBalance());
            reminder.setExpireDate(null);
            reminder.setRemindStatus(0);
            reminderMapper.insert(reminder);
            created++;
        }

        log.info("储值余额扫描: 发现{}条长期未使用, 创建{}条提醒", allSnapshots.size(), created);
        return created;
    }

    /**
     * 扫描优惠券即将过期
     */
    private int scanExpiringVouchers(LocalDate today) {
        int created = 0;
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(7).atTime(23, 59, 59);

        List<Voucher> expiring = voucherMapper.selectExpiringVouchers(start, end);

        for (Voucher voucher : expiring) {
            if (!isNotifyEnabled(voucher.getUserId(), "voucher")) {
                continue;
            }

            LocalDate expireDate = voucher.getValidEndTime().toLocalDate();
            if (reminderMapper.countDuplicate(voucher.getUserId(), voucher.getMerchantId(),
                    Constants.REMINDER_TYPE_VOUCHER_EXPIRE, expireDate) > 0) {
                continue;
            }

            EquityReminder reminder = new EquityReminder();
            reminder.setUserId(voucher.getUserId());
            reminder.setMerchantId(voucher.getMerchantId());
            reminder.setReminderType(Constants.REMINDER_TYPE_VOUCHER_EXPIRE);
            reminder.setEquityType("voucher");
            reminder.setEquityValue(voucher.getVoucherValue());
            reminder.setExpireDate(expireDate);
            reminder.setRemindStatus(0);
            reminderMapper.insert(reminder);
            created++;
        }

        log.info("券过期扫描: 发现{}条即将过期, 创建{}条提醒", expiring.size(), created);
        return created;
    }

    // ==================== 查询接口 ====================

    /**
     * 获取用户的权益提醒消息列表
     */
    public List<EquityReminderResponse> getUserReminders(Long userId) {
        List<EquityReminder> reminders = reminderMapper.selectByUserId(userId);

        // 批量查商户名称
        List<Long> merchantIds = reminders.stream()
                .map(EquityReminder::getMerchantId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> merchantNameMap = merchantIds.isEmpty()
                ? Map.of()
                : merchantMapper.selectBatchIds(merchantIds).stream()
                        .collect(Collectors.toMap(Merchant::getMerchantId, Merchant::getMerchantName));

        return reminders.stream().map(r -> EquityReminderResponse.builder()
                .reminderId(r.getReminderId())
                .merchantId(r.getMerchantId())
                .merchantName(r.getMerchantId() != null ? merchantNameMap.get(r.getMerchantId()) : null)
                .reminderType(r.getReminderType())
                .reminderTypeText(getReminderTypeText(r.getReminderType()))
                .equityType(r.getEquityType())
                .equityValue(r.getEquityValue())
                .expireDate(r.getExpireDate())
                .remindStatus(r.getRemindStatus())
                .createdAt(r.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 获取用户即将过期的权益列表
     */
    public ExpiringEquityResponse getExpiringEquities(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);
        List<ExpiringEquityItem> items = new ArrayList<>();

        // 1. 查即将过期的积分
        List<MerchantMemberSnapshot> snapshots = snapshotMapper.selectByUserId(userId);
        Map<Long, String> merchantNameMap = loadMerchantNames(snapshots.stream()
                .map(MerchantMemberSnapshot::getMerchantId).distinct().collect(Collectors.toList()));

        for (MerchantMemberSnapshot s : snapshots) {
            if (s.getPointsExpireDate() != null && s.getPointsValue() != null
                    && s.getPointsValue().compareTo(BigDecimal.ZERO) > 0
                    && !s.getPointsExpireDate().isBefore(today)
                    && !s.getPointsExpireDate().isAfter(endDate)) {

                long daysUntil = ChronoUnit.DAYS.between(today, s.getPointsExpireDate());
                items.add(ExpiringEquityItem.builder()
                        .merchantId(s.getMerchantId())
                        .merchantName(merchantNameMap.get(s.getMerchantId()))
                        .equityType("points")
                        .equityTypeText("积分")
                        .equityValue(s.getPointsValue())
                        .expireDate(s.getPointsExpireDate())
                        .daysUntilExpire((int) daysUntil)
                        .referenceId(s.getSnapshotId())
                        .build());
            }
        }

        // 2. 查即将过期的券
        List<Voucher> vouchers = voucherMapper.selectUnusedByUserId(userId);
        for (Voucher v : vouchers) {
            if (v.getValidEndTime() != null) {
                LocalDate vExpire = v.getValidEndTime().toLocalDate();
                if (!vExpire.isBefore(today) && !vExpire.isAfter(endDate)) {
                    long daysUntil = ChronoUnit.DAYS.between(today, vExpire);
                    items.add(ExpiringEquityItem.builder()
                            .merchantId(v.getMerchantId())
                            .merchantName(merchantNameMap.getOrDefault(v.getMerchantId(), ""))
                            .equityType("voucher")
                            .equityTypeText("优惠券")
                            .equityValue(v.getVoucherValue())
                            .expireDate(vExpire)
                            .daysUntilExpire((int) daysUntil)
                            .referenceId(v.getVoucherId())
                            .build());
                }
            }
        }

        // 按过期日期排序（最先过期的排在前面）
        items.sort((a, b) -> {
            if (a.getExpireDate() == null) return 1;
            if (b.getExpireDate() == null) return -1;
            return a.getExpireDate().compareTo(b.getExpireDate());
        });

        return ExpiringEquityResponse.builder()
                .items(items)
                .totalCount(items.size())
                .build();
    }

    // ==================== 通知设置 ====================

    /**
     * 获取用户通知设置
     */
    public NotificationSettingResponse getNotificationSetting(Long userId) {
        UserNotificationSetting setting = getOrCreateSetting(userId);
        return NotificationSettingResponse.builder()
                .pointsExpireNotify(setting.getPointsExpireNotify())
                .balanceNotify(setting.getBalanceNotify())
                .voucherExpireNotify(setting.getVoucherExpireNotify())
                .activityNotify(setting.getActivityNotify())
                .build();
    }

    /**
     * 更新用户通知设置
     */
    @Transactional
    public NotificationSettingResponse updateNotificationSetting(Long userId, NotificationSettingUpdateRequest request) {
        UserNotificationSetting setting = getOrCreateSetting(userId);

        if (request.getPointsExpireNotify() != null) {
            setting.setPointsExpireNotify(request.getPointsExpireNotify());
        }
        if (request.getBalanceNotify() != null) {
            setting.setBalanceNotify(request.getBalanceNotify());
        }
        if (request.getVoucherExpireNotify() != null) {
            setting.setVoucherExpireNotify(request.getVoucherExpireNotify());
        }
        if (request.getActivityNotify() != null) {
            setting.setActivityNotify(request.getActivityNotify());
        }

        settingMapper.updateById(setting);

        return NotificationSettingResponse.builder()
                .pointsExpireNotify(setting.getPointsExpireNotify())
                .balanceNotify(setting.getBalanceNotify())
                .voucherExpireNotify(setting.getVoucherExpireNotify())
                .activityNotify(setting.getActivityNotify())
                .build();
    }

    // ==================== 私有方法 ====================

    private UserNotificationSetting getOrCreateSetting(Long userId) {
        UserNotificationSetting setting = settingMapper.selectOne(
                new LambdaQueryWrapper<UserNotificationSetting>()
                        .eq(UserNotificationSetting::getUserId, userId));

        if (setting == null) {
            setting = new UserNotificationSetting();
            setting.setUserId(userId);
            setting.setPointsExpireNotify(1);
            setting.setBalanceNotify(1);
            setting.setVoucherExpireNotify(1);
            setting.setActivityNotify(1);
            settingMapper.insert(setting);
        }
        return setting;
    }

    private boolean isNotifyEnabled(Long userId, String type) {
        UserNotificationSetting setting = settingMapper.selectOne(
                new LambdaQueryWrapper<UserNotificationSetting>()
                        .eq(UserNotificationSetting::getUserId, userId));

        if (setting == null) {
            return true; // 默认开启
        }

        return switch (type) {
            case "points" -> setting.getPointsExpireNotify() != null && setting.getPointsExpireNotify() == 1;
            case "balance" -> setting.getBalanceNotify() != null && setting.getBalanceNotify() == 1;
            case "voucher" -> setting.getVoucherExpireNotify() != null && setting.getVoucherExpireNotify() == 1;
            default -> true;
        };
    }

    private Map<Long, String> loadMerchantNames(List<Long> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return Map.of();
        }
        return merchantMapper.selectBatchIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getMerchantId, Merchant::getMerchantName));
    }

    private String getReminderTypeText(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "积分即将过期";
            case 2 -> "储值余额提醒";
            case 3 -> "优惠券即将过期";
            case 4 -> "会员等级即将降级";
            case 5 -> "沉睡会员唤醒";
            default -> "权益提醒";
        };
    }
}
