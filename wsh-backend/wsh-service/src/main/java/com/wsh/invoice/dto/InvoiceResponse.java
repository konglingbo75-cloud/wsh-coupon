package com.wsh.invoice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InvoiceResponse {
    private Long invoiceId;
    private Long merchantId;
    private String merchantName;
    private Long consumeRecordId;
    private String invoiceNo;
    private String invoiceCode;
    private Integer invoiceType;
    private String invoiceTypeName;
    private Integer invoiceStatus;
    private String invoiceStatusName;
    private BigDecimal invoiceAmount;
    private String invoiceTitle;
    private String taxNumber;
    private String invoiceUrl;
    private LocalDate invoiceDate;
    private LocalDateTime requestTime;
    private LocalDateTime completeTime;
    private LocalDateTime createdAt;
}
