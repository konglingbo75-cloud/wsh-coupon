package com.wsh.groupbuy.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 发起拼团请求
 */
@Data
public class InitiateGroupRequest {
    
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
}
