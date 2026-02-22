package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 核销记录实体
 */
@Data
@TableName("tb_verification_record")
public class VerificationRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

    private Long voucherId;
    private String voucherCode;
    private Long userId;
    private Long merchantId;
    private Long branchId;
    private Long employeeId;

    private LocalDateTime verifyTime;

    /** 是否沉睡唤醒核销：0否 1是 */
    private Integer isDormancyAwake;

    /** 同步状态：0未同步 1已同步 2同步失败 */
    private Integer syncStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
