package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_activity")
public class Activity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long activityId;

    private Long merchantId;
    private String sourceActivityId;
    private Integer activityType;
    private String activityName;
    private String activityDesc;
    private String coverImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String config;
    private Integer stock;
    private Integer soldCount;
    private Integer targetMemberType;
    private Integer isPublic;
    private String syncSource;
    private LocalDateTime syncTime;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
