package com.wsh.merchant.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantBranch;
import com.wsh.domain.entity.MerchantEmployee;
import com.wsh.domain.mapper.MerchantBranchMapper;
import com.wsh.domain.mapper.MerchantEmployeeMapper;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.merchant.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户入驻与管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;
    private final MerchantBranchMapper merchantBranchMapper;
    private final MerchantEmployeeMapper merchantEmployeeMapper;

    /**
     * 商户入驻申请
     * 1. 创建商户记录（待审核）
     * 2. 创建门店记录
     * 3. 将当前登录用户绑定为商户管理员
     */
    @Transactional
    public MerchantResponse register(MerchantRegisterRequest request) {
        Long currentUserId = SecurityUtil.getUserId();
        String currentOpenid = SecurityUtil.getLoginUser().getOpenid();

        // 检查是否已有商户身份
        MerchantEmployee existing = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getOpenid, currentOpenid)
                        .eq(MerchantEmployee::getStatus, 1));
        if (existing != null) {
            throw new BusinessException(400, "您已是商户员工，不能重复入驻");
        }

        // 1. 创建商户
        Merchant merchant = new Merchant();
        merchant.setMerchantCode(generateMerchantCode());
        merchant.setMerchantName(request.getMerchantName());
        merchant.setLogoUrl(request.getLogoUrl());
        merchant.setContactName(request.getContactName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setAddress(request.getAddress());
        merchant.setCity(request.getCity());
        merchant.setLongitude(request.getLongitude());
        merchant.setLatitude(request.getLatitude());
        merchant.setBusinessCategory(request.getBusinessCategory());
        merchant.setIntegrationType(request.getIntegrationType() != null ? request.getIntegrationType() : 3);
        merchant.setStatus(Constants.MERCHANT_STATUS_PENDING);
        merchant.setProfitSharingRate(new BigDecimal("0.0200")); // 默认2%
        merchantMapper.insert(merchant);

        // 2. 创建门店
        List<MerchantBranch> branches = request.getBranches().stream().map(bi -> {
            MerchantBranch branch = new MerchantBranch();
            branch.setMerchantId(merchant.getMerchantId());
            branch.setBranchName(bi.getBranchName());
            branch.setAddress(bi.getAddress());
            branch.setLongitude(bi.getLongitude());
            branch.setLatitude(bi.getLatitude());
            branch.setStatus(1);
            return branch;
        }).collect(Collectors.toList());

        for (MerchantBranch branch : branches) {
            merchantBranchMapper.insert(branch);
        }

        // 3. 绑定当前用户为管理员
        MerchantEmployee admin = new MerchantEmployee();
        admin.setMerchantId(merchant.getMerchantId());
        admin.setBranchId(branches.get(0).getBranchId()); // 默认挂在第一家门店
        admin.setName(request.getContactName());
        admin.setPhone(request.getContactPhone());
        admin.setOpenid(currentOpenid);
        admin.setRole(1); // 管理员
        admin.setStatus(1);
        merchantEmployeeMapper.insert(admin);

        log.info("商户入驻申请成功: merchantId={}, merchantCode={}, userId={}",
                merchant.getMerchantId(), merchant.getMerchantCode(), currentUserId);

        return buildMerchantResponse(merchant, branches);
    }

    /**
     * 查询当前用户关联的商户信息
     */
    public MerchantResponse getMyMerchant() {
        String openid = SecurityUtil.getLoginUser().getOpenid();

        MerchantEmployee employee = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getOpenid, openid)
                        .eq(MerchantEmployee::getStatus, 1)
                        .last("LIMIT 1"));

        if (employee == null) {
            throw new BusinessException(404, "您还未入驻商户");
        }

        Merchant merchant = merchantMapper.selectById(employee.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(404, "商户信息不存在");
        }

        List<MerchantBranch> branches = merchantBranchMapper.selectList(
                new LambdaQueryWrapper<MerchantBranch>()
                        .eq(MerchantBranch::getMerchantId, merchant.getMerchantId()));

        return buildMerchantResponse(merchant, branches);
    }

    /**
     * 添加员工
     */
    @Transactional
    public MerchantEmployeeResponse addEmployee(MerchantEmployeeRequest request) {
        // 校验商户存在且当前用户是管理员
        verifyMerchantAdmin(request.getMerchantId());

        // 检查手机号是否已存在
        MerchantEmployee existing = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getMerchantId, request.getMerchantId())
                        .eq(MerchantEmployee::getPhone, request.getPhone())
                        .eq(MerchantEmployee::getStatus, 1));
        if (existing != null) {
            throw new BusinessException(400, "该手机号已是本商户员工");
        }

        MerchantEmployee employee = new MerchantEmployee();
        employee.setMerchantId(request.getMerchantId());
        employee.setBranchId(request.getBranchId());
        employee.setName(request.getName());
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());
        employee.setStatus(1);
        merchantEmployeeMapper.insert(employee);

        log.info("添加员工成功: employeeId={}, merchantId={}", employee.getEmployeeId(), request.getMerchantId());

        return buildEmployeeResponse(employee);
    }

    /**
     * 查询商户员工列表
     */
    public List<MerchantEmployeeResponse> listEmployees(Long merchantId) {
        verifyMerchantAdmin(merchantId);

        List<MerchantEmployee> employees = merchantEmployeeMapper.selectList(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getMerchantId, merchantId)
                        .eq(MerchantEmployee::getStatus, 1)
                        .orderByDesc(MerchantEmployee::getCreatedAt));

        return employees.stream().map(this::buildEmployeeResponse).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 校验当前用户是否为指定商户的管理员
     */
    private void verifyMerchantAdmin(Long merchantId) {
        String openid = SecurityUtil.getLoginUser().getOpenid();
        MerchantEmployee admin = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getMerchantId, merchantId)
                        .eq(MerchantEmployee::getOpenid, openid)
                        .eq(MerchantEmployee::getRole, 1)
                        .eq(MerchantEmployee::getStatus, 1));
        if (admin == null) {
            throw new BusinessException(403, "无权操作此商户");
        }
    }

    /**
     * 生成商户编码：M + 年月日 + 4位随机数字
     */
    private String generateMerchantCode() {
        return "M" + cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyyMMdd")
                + RandomUtil.randomNumbers(4);
    }

    private MerchantResponse buildMerchantResponse(Merchant merchant, List<MerchantBranch> branches) {
        List<MerchantBranchResponse> branchResponses = branches.stream()
                .map(b -> MerchantBranchResponse.builder()
                        .branchId(b.getBranchId())
                        .merchantId(b.getMerchantId())
                        .branchName(b.getBranchName())
                        .address(b.getAddress())
                        .longitude(b.getLongitude())
                        .latitude(b.getLatitude())
                        .status(b.getStatus())
                        .build())
                .collect(Collectors.toList());

        return MerchantResponse.builder()
                .merchantId(merchant.getMerchantId())
                .merchantCode(merchant.getMerchantCode())
                .merchantName(merchant.getMerchantName())
                .logoUrl(merchant.getLogoUrl())
                .contactName(merchant.getContactName())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .city(merchant.getCity())
                .longitude(merchant.getLongitude())
                .latitude(merchant.getLatitude())
                .businessCategory(merchant.getBusinessCategory())
                .status(merchant.getStatus())
                .integrationType(merchant.getIntegrationType())
                .profitSharingRate(merchant.getProfitSharingRate())
                .createdAt(merchant.getCreatedAt())
                .branches(branchResponses)
                .build();
    }

    private MerchantEmployeeResponse buildEmployeeResponse(MerchantEmployee e) {
        return MerchantEmployeeResponse.builder()
                .employeeId(e.getEmployeeId())
                .merchantId(e.getMerchantId())
                .branchId(e.getBranchId())
                .name(e.getName())
                .phone(e.getPhone())
                .openid(e.getOpenid())
                .role(e.getRole())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
