package com.wsh.invoice.controller;

import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.invoice.dto.*;
import com.wsh.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * 获取我的发票列表
     */
    @GetMapping("/invoices")
    public R<PageResult<InvoiceResponse>> listInvoices(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getUserId();
        PageQueryRequest pageQuery = new PageQueryRequest();
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return R.ok(invoiceService.listInvoices(userId, status, pageQuery));
    }

    /**
     * 获取发票详情
     */
    @GetMapping("/invoices/{id}")
    public R<InvoiceResponse> getInvoiceDetail(@PathVariable("id") Long invoiceId) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(invoiceService.getInvoiceDetail(userId, invoiceId));
    }

    /**
     * 申请开票
     */
    @PostMapping("/invoices/request")
    public R<InvoiceResponse> requestInvoice(@Valid @RequestBody InvoiceRequestDto request) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(invoiceService.requestInvoice(userId, request));
    }

    /**
     * 获取发票抬头设置列表
     */
    @GetMapping("/invoice-settings")
    public R<List<InvoiceSettingResponse>> listInvoiceSettings() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(invoiceService.listInvoiceSettings(userId));
    }

    /**
     * 新增发票抬头
     */
    @PostMapping("/invoice-settings")
    public R<InvoiceSettingResponse> createInvoiceSetting(@Valid @RequestBody InvoiceSettingCreateRequest request) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(invoiceService.createInvoiceSetting(userId, request));
    }

    /**
     * 更新发票抬头
     */
    @PutMapping("/invoice-settings/{id}")
    public R<InvoiceSettingResponse> updateInvoiceSetting(
            @PathVariable("id") Long settingId,
            @Valid @RequestBody InvoiceSettingUpdateRequest request) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(invoiceService.updateInvoiceSetting(userId, settingId, request));
    }

    /**
     * 删除发票抬头
     */
    @DeleteMapping("/invoice-settings/{id}")
    public R<Void> deleteInvoiceSetting(@PathVariable("id") Long settingId) {
        Long userId = SecurityUtil.getUserId();
        invoiceService.deleteInvoiceSetting(userId, settingId);
        return R.ok();
    }

    /**
     * 设为默认抬头
     */
    @PutMapping("/invoice-settings/{id}/default")
    public R<Void> setDefaultSetting(@PathVariable("id") Long settingId) {
        Long userId = SecurityUtil.getUserId();
        invoiceService.setDefaultSetting(userId, settingId);
        return R.ok();
    }
}
