package com.wsh.invoice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceRequestDto {
    
    @NotNull(message = "消费记录ID不能为空")
    private Long consumeRecordId;
    
    @NotNull(message = "发票抬头设置ID不能为空")
    private Long invoiceSettingId;
}
