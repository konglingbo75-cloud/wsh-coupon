package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户发票抬头设置实体
 */
@Data
@TableName("tb_user_invoice_setting")
public class UserInvoiceSetting {

    @TableId(type = IdType.ASSIGN_ID)
    private Long settingId;

    private Long userId;

    /** 抬头类型：1个人 2企业 */
    private Integer titleType;

    /** 发票抬头 */
    private String invoiceTitle;

    /** 税号（企业必填） */
    private String taxNumber;

    /** 开户银行 */
    private String bankName;

    /** 银行账号 */
    private String bankAccount;

    /** 公司地址 */
    private String companyAddress;

    /** 公司电话 */
    private String companyPhone;

    /** 是否默认：0否 1是 */
    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
