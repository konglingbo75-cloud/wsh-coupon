package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发票信息实体
 */
@Data
@TableName("tb_invoice")
public class Invoice {

    @TableId(type = IdType.ASSIGN_ID)
    private Long invoiceId;

    private Long userId;
    private Long merchantId;
    private Long consumeRecordId;

    /** 发票号码 */
    private String invoiceNo;

    /** 发票代码 */
    private String invoiceCode;

    /** 发票类型：1电子普票 2电子专票 3纸质普票 */
    private Integer invoiceType;

    /** 发票状态：0未开 1已开 2开票中 3开票失败 */
    private Integer invoiceStatus;

    /** 发票金额 */
    private BigDecimal invoiceAmount;

    /** 发票抬头 */
    private String invoiceTitle;

    /** 税号 */
    private String taxNumber;

    /** 电子发票下载链接 */
    private String invoiceUrl;

    /** 开票日期 */
    private LocalDate invoiceDate;

    /** 申请开票时间 */
    private LocalDateTime requestTime;

    /** 开票完成时间 */
    private LocalDateTime completeTime;

    /** 最近同步时间 */
    private LocalDateTime syncTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
