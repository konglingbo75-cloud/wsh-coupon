package com.wsh.notification.service;

import com.wsh.domain.entity.EquityReminder;
import com.wsh.domain.mapper.EquityReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 站内消息服务
 * 负责处理站内消息展示逻辑
 * 说明：站内消息直接使用 tb_equity_reminder 表存储，
 *       用户进入消息中心时从该表读取未读消息列表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InAppMessageService {

    private final EquityReminderMapper reminderMapper;

    /**
     * 将提醒标记为站内消息已发送
     * （对于站内消息，创建即发送，用户打开消息中心即可看到）
     */
    @Transactional
    public void markAsInAppSent(EquityReminder reminder) {
        // 站内消息直接标记为已发送
        reminder.setRemindStatus(1);
        reminder.setRemindTime(LocalDateTime.now());
        reminderMapper.updateById(reminder);
        log.debug("站内消息已标记: reminderId={}", reminder.getReminderId());
    }

    /**
     * 批量标记站内消息已发送
     */
    @Transactional
    public int markAllPendingAsInAppSent() {
        var pending = reminderMapper.selectPending();
        int count = 0;
        for (var reminder : pending) {
            markAsInAppSent(reminder);
            count++;
        }
        return count;
    }
}
