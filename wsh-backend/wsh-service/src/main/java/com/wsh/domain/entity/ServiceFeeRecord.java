package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务费记录实体
 */
@Data
@TableName("tb_service_fee_record")
public class ServiceFeeRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

    private Long merchantId;
    private String orderNo;
    private Long voucherId;

    /** 关联核销记录 */
    private Long verifyRecordId;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 服务费率（快照） */
    private BigDecimal serviceFeeRate;

    /** 服务费金额 */
    private BigDecimal serviceFee;

    /** 商户到账金额 */
    private BigDecimal merchantAmount;

    /** 分账状态：0待分账 1已分账 2分账失败 */
    private Integer sharingStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
