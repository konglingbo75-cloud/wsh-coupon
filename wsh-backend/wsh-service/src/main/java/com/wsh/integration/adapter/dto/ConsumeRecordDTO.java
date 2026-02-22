package com.wsh.integration.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 从商户系统同步的消费记录
 */
@Data
@Builder
public class ConsumeRecordDTO {

    private String sourceOrderNo;
    private Long branchId;
    private LocalDateTime consumeTime;
    private BigDecimal consumeAmount;
    /** 发票状态：0未开 1已开 2开票中 */
    private Integer invoiceStatus;
    private String invoiceNo;
    private String invoiceUrl;
}
