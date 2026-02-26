package com.wsh.invoice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvoiceSettingResponse {
    private Long settingId;
    private Integer titleType;
    private String titleTypeName;
    private String invoiceTitle;
    private String taxNumber;
    private String bankName;
    private String bankAccount;
    private String companyAddress;
    private String companyPhone;
    private Integer isDefault;
    private LocalDateTime createdAt;
}
