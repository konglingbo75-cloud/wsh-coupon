package com.wsh.invoice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.Invoice;
import com.wsh.domain.entity.Merchant;
import com.wsh.domain.entity.MerchantConsumeRecord;
import com.wsh.domain.entity.UserInvoiceSetting;
import com.wsh.domain.mapper.InvoiceMapper;
import com.wsh.domain.mapper.MerchantConsumeRecordMapper;
import com.wsh.domain.mapper.MerchantMapper;
import com.wsh.domain.mapper.UserInvoiceSettingMapper;
import com.wsh.invoice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceMapper invoiceMapper;
    private final UserInvoiceSettingMapper invoiceSettingMapper;
    private final MerchantConsumeRecordMapper consumeRecordMapper;
    private final MerchantMapper merchantMapper;

    /**
     * 获取用户发票列表
     */
    public PageResult<InvoiceResponse> listInvoices(Long userId, Integer status, PageQueryRequest pageQuery) {
        Page<Invoice> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<Invoice>()
                .eq(Invoice::getUserId, userId)
                .eq(status != null, Invoice::getInvoiceStatus, status)
                .orderByDesc(Invoice::getCreatedAt);
        
        IPage<Invoice> result = invoiceMapper.selectPage(page, wrapper);
        
        List<InvoiceResponse> list = result.getRecords().stream()
                .map(this::toInvoiceResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取发票详情
     */
    public InvoiceResponse getInvoiceDetail(Long userId, Long invoiceId) {
        Invoice invoice = invoiceMapper.selectOne(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getInvoiceId, invoiceId)
                        .eq(Invoice::getUserId, userId)
        );
        if (invoice == null) {
            throw new RuntimeException("发票不存在");
        }
        return toInvoiceResponse(invoice);
    }

    /**
     * 申请开票
     */
    @Transactional
    public InvoiceResponse requestInvoice(Long userId, InvoiceRequestDto request) {
        // 查询消费记录
        MerchantConsumeRecord record = consumeRecordMapper.selectOne(
                new LambdaQueryWrapper<MerchantConsumeRecord>()
                        .eq(MerchantConsumeRecord::getRecordId, request.getConsumeRecordId())
                        .eq(MerchantConsumeRecord::getUserId, userId)
        );
        if (record == null) {
            throw new RuntimeException("消费记录不存在");
        }
        
        // 检查是否已有发票
        Invoice existing = invoiceMapper.selectOne(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getConsumeRecordId, request.getConsumeRecordId())
                        .eq(Invoice::getUserId, userId)
        );
        if (existing != null && existing.getInvoiceStatus() != 3) {
            throw new RuntimeException("该消费记录已申请发票");
        }
        
        // 查询发票抬头设置
        UserInvoiceSetting setting = invoiceSettingMapper.selectById(request.getInvoiceSettingId());
        if (setting == null || !setting.getUserId().equals(userId)) {
            throw new RuntimeException("发票抬头设置不存在");
        }
        
        // 创建发票申请
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setMerchantId(record.getMerchantId());
        invoice.setConsumeRecordId(record.getRecordId());
        invoice.setInvoiceType(setting.getTitleType() == 2 ? 2 : 1);
        invoice.setInvoiceStatus(2); // 开票中
        invoice.setInvoiceAmount(record.getConsumeAmount());
        invoice.setInvoiceTitle(setting.getInvoiceTitle());
        invoice.setTaxNumber(setting.getTaxNumber());
        invoice.setRequestTime(LocalDateTime.now());
        
        invoiceMapper.insert(invoice);
        log.info("用户[{}]申请开票成功, invoiceId={}", userId, invoice.getInvoiceId());
        
        return toInvoiceResponse(invoice);
    }

    /**
     * 获取用户发票抬头设置列表
     */
    public List<InvoiceSettingResponse> listInvoiceSettings(Long userId) {
        List<UserInvoiceSetting> settings = invoiceSettingMapper.selectList(
                new LambdaQueryWrapper<UserInvoiceSetting>()
                        .eq(UserInvoiceSetting::getUserId, userId)
                        .orderByDesc(UserInvoiceSetting::getIsDefault)
                        .orderByDesc(UserInvoiceSetting::getCreatedAt)
        );
        return settings.stream()
                .map(this::toSettingResponse)
                .collect(Collectors.toList());
    }

    /**
     * 新增发票抬头
     */
    @Transactional
    public InvoiceSettingResponse createInvoiceSetting(Long userId, InvoiceSettingCreateRequest request) {
        // 企业类型需要税号
        if (request.getTitleType() == 2 && (request.getTaxNumber() == null || request.getTaxNumber().isBlank())) {
            throw new RuntimeException("企业发票需要填写税号");
        }
        
        UserInvoiceSetting setting = new UserInvoiceSetting();
        BeanUtils.copyProperties(request, setting);
        setting.setUserId(userId);
        
        // 如果设为默认，先取消其他默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefaultSetting(userId);
        }
        
        invoiceSettingMapper.insert(setting);
        log.info("用户[{}]新增发票抬头, settingId={}", userId, setting.getSettingId());
        
        return toSettingResponse(setting);
    }

    /**
     * 更新发票抬头
     */
    @Transactional
    public InvoiceSettingResponse updateInvoiceSetting(Long userId, Long settingId, InvoiceSettingUpdateRequest request) {
        UserInvoiceSetting setting = invoiceSettingMapper.selectById(settingId);
        if (setting == null || !setting.getUserId().equals(userId)) {
            throw new RuntimeException("发票抬头设置不存在");
        }
        
        if (request.getTitleType() != null) setting.setTitleType(request.getTitleType());
        if (request.getInvoiceTitle() != null) setting.setInvoiceTitle(request.getInvoiceTitle());
        if (request.getTaxNumber() != null) setting.setTaxNumber(request.getTaxNumber());
        if (request.getBankName() != null) setting.setBankName(request.getBankName());
        if (request.getBankAccount() != null) setting.setBankAccount(request.getBankAccount());
        if (request.getCompanyAddress() != null) setting.setCompanyAddress(request.getCompanyAddress());
        if (request.getCompanyPhone() != null) setting.setCompanyPhone(request.getCompanyPhone());
        
        invoiceSettingMapper.updateById(setting);
        log.info("用户[{}]更新发票抬头, settingId={}", userId, settingId);
        
        return toSettingResponse(setting);
    }

    /**
     * 删除发票抬头
     */
    @Transactional
    public void deleteInvoiceSetting(Long userId, Long settingId) {
        UserInvoiceSetting setting = invoiceSettingMapper.selectById(settingId);
        if (setting == null || !setting.getUserId().equals(userId)) {
            throw new RuntimeException("发票抬头设置不存在");
        }
        invoiceSettingMapper.deleteById(settingId);
        log.info("用户[{}]删除发票抬头, settingId={}", userId, settingId);
    }

    /**
     * 设为默认抬头
     */
    @Transactional
    public void setDefaultSetting(Long userId, Long settingId) {
        UserInvoiceSetting setting = invoiceSettingMapper.selectById(settingId);
        if (setting == null || !setting.getUserId().equals(userId)) {
            throw new RuntimeException("发票抬头设置不存在");
        }
        
        clearDefaultSetting(userId);
        setting.setIsDefault(1);
        invoiceSettingMapper.updateById(setting);
        log.info("用户[{}]设置默认发票抬头, settingId={}", userId, settingId);
    }

    private void clearDefaultSetting(Long userId) {
        List<UserInvoiceSetting> defaults = invoiceSettingMapper.selectList(
                new LambdaQueryWrapper<UserInvoiceSetting>()
                        .eq(UserInvoiceSetting::getUserId, userId)
                        .eq(UserInvoiceSetting::getIsDefault, 1)
        );
        for (UserInvoiceSetting s : defaults) {
            s.setIsDefault(0);
            invoiceSettingMapper.updateById(s);
        }
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        InvoiceResponse resp = new InvoiceResponse();
        BeanUtils.copyProperties(invoice, resp);
        
        // 设置类型和状态名称
        resp.setInvoiceTypeName(getTypeName(invoice.getInvoiceType()));
        resp.setInvoiceStatusName(getStatusName(invoice.getInvoiceStatus()));
        
        // 获取商户名称
        Merchant merchant = merchantMapper.selectById(invoice.getMerchantId());
        if (merchant != null) {
            resp.setMerchantName(merchant.getMerchantName());
        }
        
        return resp;
    }

    private InvoiceSettingResponse toSettingResponse(UserInvoiceSetting setting) {
        InvoiceSettingResponse resp = new InvoiceSettingResponse();
        BeanUtils.copyProperties(setting, resp);
        resp.setTitleTypeName(setting.getTitleType() == 1 ? "个人" : "企业");
        return resp;
    }

    private String getTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "电子普票";
            case 2 -> "电子专票";
            case 3 -> "纸质普票";
            default -> "未知";
        };
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "未开票";
            case 1 -> "已开票";
            case 2 -> "开票中";
            case 3 -> "开票失败";
            default -> "未知";
        };
    }
}
