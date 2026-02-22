package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_platform_member")
public class PlatformMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long memberId;

    private Long userId;
    private Integer memberLevel;
    private BigDecimal totalConsumeAmount;
    private Integer totalConsumeCount;
    private Integer merchantCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
