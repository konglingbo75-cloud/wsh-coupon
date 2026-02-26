package com.wsh.invoice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InvoiceSettingUpdateRequest {
    
    private Integer titleType;
    
    @Size(max = 128, message = "发票抬头长度不能超过128字符")
    private String invoiceTitle;
    
    @Size(max = 32, message = "税号长度不能超过32字符")
    private String taxNumber;
    
    @Size(max = 64, message = "开户银行长度不能超过64字符")
    private String bankName;
    
    @Size(max = 32, message = "银行账号长度不能超过32字符")
    private String bankAccount;
    
    @Size(max = 255, message = "公司地址长度不能超过255字符")
    private String companyAddress;
    
    @Size(max = 16, message = "公司电话长度不能超过16字符")
    private String companyPhone;
}
