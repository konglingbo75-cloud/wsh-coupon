package com.wsh.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.AdminUserListResponse;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.PlatformMember;
import com.wsh.domain.entity.User;
import com.wsh.domain.mapper.MerchantMemberSnapshotMapper;
import com.wsh.domain.mapper.PlatformMemberMapper;
import com.wsh.domain.mapper.UserMapper;
import com.wsh.domain.entity.MerchantMemberSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final PlatformMemberMapper platformMemberMapper;
    private final MerchantMemberSnapshotMapper snapshotMapper;

    public PageResult<AdminUserListResponse> listUsers(PageQueryRequest query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(User::getNickname, query.getKeyword())
                    .or().like(User::getPhone, query.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = new Page<>(query.getPage(), query.getSize());
        Page<User> result = userMapper.selectPage(page, wrapper);

        List<AdminUserListResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    private AdminUserListResponse toResponse(User user) {
        PlatformMember member = platformMemberMapper.selectOne(
                new LambdaQueryWrapper<PlatformMember>()
                        .eq(PlatformMember::getUserId, user.getUserId()));

        Long merchantCount = snapshotMapper.selectCount(
                new LambdaQueryWrapper<MerchantMemberSnapshot>()
                        .eq(MerchantMemberSnapshot::getUserId, user.getUserId()));

        return AdminUserListResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .status(user.getStatus())
                .merchantCount(merchantCount.intValue())
                .totalConsumeAmount(member != null ? member.getTotalConsumeAmount() : null)
                .totalConsumeCount(member != null ? member.getTotalConsumeCount() : null)
                .memberLevel(member != null ? member.getMemberLevel() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
