package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分账记录实体
 */
@Data
@TableName("tb_profit_sharing")
public class ProfitSharing {

    @TableId(type = IdType.ASSIGN_ID)
    private Long sharingId;

    private String orderNo;

    /** 关联券码ID */
    private Long voucherId;

    private Long merchantId;

    private String transactionId;

    /** 交易总金额 */
    private BigDecimal totalAmount;

    /** 服务费率（快照） */
    private BigDecimal serviceFeeRate;

    /** 平台服务费 */
    private BigDecimal serviceFee;

    /** 商户到账金额 */
    private BigDecimal merchantAmount;

    /** 状态：0待分账(待核销) 1核销触发分账中 2已分账 3失败 */
    private Integer status;

    /** 关联核销记录ID */
    private Long verifyRecordId;

    private Integer retryCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
