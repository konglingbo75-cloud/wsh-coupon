package com.wsh.groupbuy.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 参与拼团请求
 */
@Data
public class JoinGroupRequest {
    
    @NotNull(message = "拼团ID不能为空")
    private Long groupOrderId;
}
