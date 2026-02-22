package com.wsh.matching.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantConsumeRecord;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import com.wsh.domain.mapper.MerchantConsumeRecordMapper;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import com.wsh.integration.adapter.AdapterFactory;
import com.wsh.integration.adapter.MerchantDataAdapter;
import com.wsh.integration.adapter.dto.ConsumeRecordDTO;
import com.wsh.integration.adapter.dto.MemberDataDTO;
import com.wsh.integration.adapter.dto.MemberMatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员匹配服务
 * 核心功能：用手机号去所有已对接商户系统匹配会员身份，
 * 匹配成功后拉取会员数据写入 tb_merchant_member_snapshot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberMatchingService {

    private final MerchantMapper merchantMapper;
    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final MerchantConsumeRecordMapper consumeRecordMapper;
    private final AdapterFactory adapterFactory;

    /**
     * 对指定用户执行全量会员匹配
     * 遍历所有正常状态的商户，用手机号匹配会员
     *
     * @param userId 平台用户ID
     * @param phone  用户手机号
     * @return 匹配成功的商户ID列表
     */
    @Transactional
    public List<Long> matchAllMerchants(Long userId, String phone) {
        // 查询所有正常状态的商户
        List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, Constants.MERCHANT_STATUS_ACTIVE));

        List<Long> matchedMerchantIds = new ArrayList<>();

        for (Merchant merchant : merchants) {
            try {
                boolean matched = matchSingleMerchant(userId, phone, merchant);
                if (matched) {
                    matchedMerchantIds.add(merchant.getMerchantId());
                }
            } catch (Exception e) {
                log.error("匹配商户{}时异常: {}", merchant.getMerchantId(), e.getMessage(), e);
            }
        }

        log.info("用户{}会员匹配完成: 共{}家商户, 匹配成功{}家",
                userId, merchants.size(), matchedMerchantIds.size());
        return matchedMerchantIds;
    }

    /**
     * 匹配单个商户
     *
     * @return 是否匹配成功
     */
    public boolean matchSingleMerchant(Long userId, String phone, Merchant merchant) {
        MerchantDataAdapter adapter = adapterFactory.getAdapter(merchant.getMerchantId());
        MemberMatchResult result = adapter.matchMemberByPhone(phone, merchant.getMerchantId());

        if (!result.isMatched()) {
            return false;
        }

        // 匹配成功，保存/更新会员快照
        saveMemberSnapshot(userId, merchant.getMerchantId(), result);

        // 同步消费记录
        syncConsumeRecords(userId, merchant.getMerchantId(), adapter);

        return true;
    }

    /**
     * 同步指定用户在指定商户的最新数据
     */
    @Transactional
    public void syncMemberData(Long userId, Long merchantId) {
        MerchantDataAdapter adapter = adapterFactory.getAdapter(merchantId);
        MemberDataDTO data = adapter.syncMemberData(userId, merchantId);

        if (data == null) {
            log.debug("商户{}无法同步用户{}的会员数据", merchantId, userId);
            return;
        }

        updateSnapshot(userId, merchantId, data);
        syncConsumeRecords(userId, merchantId, adapter);
    }

    // ==================== 私有方法 ====================

    private void saveMemberSnapshot(Long userId, Long merchantId, MemberMatchResult result) {
        MemberDataDTO data = result.getMemberData();

        // 查询是否已有快照
        MerchantMemberSnapshot existing = snapshotMapper.selectOne(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                        .eq(MerchantMemberSnapshot::getUserId, userId)
                        .eq(MerchantMemberSnapshot::getMerchantId, merchantId));

        if (existing != null) {
            // 更新已有快照
            if (data != null) {
                fillSnapshotFromData(existing, data);
            }
            existing.setSourceMemberId(result.getSourceMemberId());
            existing.setSyncTime(LocalDateTime.now());
            existing.setSyncStatus(1);
            snapshotMapper.updateById(existing);
        } else {
            // 创建新快照
            MerchantMemberSnapshot snapshot = new MerchantMemberSnapshot();
            snapshot.setUserId(userId);
            snapshot.setMerchantId(merchantId);
            snapshot.setSourceMemberId(result.getSourceMemberId());
            if (data != null) {
                fillSnapshotFromData(snapshot, data);
            }
            snapshot.setSyncTime(LocalDateTime.now());
            snapshot.setSyncStatus(1);
            snapshotMapper.insert(snapshot);
        }
    }

    private void updateSnapshot(Long userId, Long merchantId, MemberDataDTO data) {
        MerchantMemberSnapshot existing = snapshotMapper.selectOne(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                        .eq(MerchantMemberSnapshot::getUserId, userId)
                        .eq(MerchantMemberSnapshot::getMerchantId, merchantId));

        if (existing != null) {
            fillSnapshotFromData(existing, data);
            existing.setSyncTime(LocalDateTime.now());
            existing.setSyncStatus(1);
            snapshotMapper.updateById(existing);
        }
    }

    private void fillSnapshotFromData(MerchantMemberSnapshot snapshot, MemberDataDTO data) {
        snapshot.setMemberLevelName(data.getMemberLevelName());
        snapshot.setPoints(data.getPoints());
        snapshot.setPointsValue(data.getPointsValue());
        snapshot.setPointsExpireDate(data.getPointsExpireDate());
        snapshot.setBalance(data.getBalance());
        snapshot.setConsumeCount(data.getConsumeCount());
        snapshot.setTotalConsumeAmount(data.getTotalConsumeAmount());
        snapshot.setLastConsumeTime(data.getLastConsumeTime());
        snapshot.setDormancyLevel(data.getDormancyLevel() != null ? data.getDormancyLevel() : 0);
    }

    private void syncConsumeRecords(Long userId, Long merchantId, MerchantDataAdapter adapter) {
        List<ConsumeRecordDTO> records = adapter.syncConsumeRecords(userId, merchantId);
        if (records == null || records.isEmpty()) {
            return;
        }

        for (ConsumeRecordDTO dto : records) {
            // 按 sourceOrderNo 去重
            if (dto.getSourceOrderNo() != null) {
                Long count = consumeRecordMapper.selectCount(
                        new LambdaQueryWrapper<MerchantConsumeRecord>()
                                .eq(MerchantConsumeRecord::getUserId, userId)
                                .eq(MerchantConsumeRecord::getMerchantId, merchantId)
                                .eq(MerchantConsumeRecord::getSourceOrderNo, dto.getSourceOrderNo()));
                if (count > 0) {
                    continue;
                }
            }

            MerchantConsumeRecord record = new MerchantConsumeRecord();
            record.setUserId(userId);
            record.setMerchantId(merchantId);
            record.setBranchId(dto.getBranchId());
            record.setConsumeTime(dto.getConsumeTime());
            record.setConsumeAmount(dto.getConsumeAmount());
            record.setInvoiceStatus(dto.getInvoiceStatus());
            record.setInvoiceNo(dto.getInvoiceNo());
            record.setInvoiceUrl(dto.getInvoiceUrl());
            record.setSourceOrderNo(dto.getSourceOrderNo());
            record.setSyncTime(LocalDateTime.now());
            consumeRecordMapper.insert(record);
        }
    }
}
