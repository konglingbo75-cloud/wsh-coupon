package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "操作日志列表项")
public class AdminOperationLogResponse {

    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "管理员名称")
    private String adminName;

    @Schema(description = "模块")
    private String module;

    @Schema(description = "操作动作")
    private String action;

    @Schema(description = "操作目标ID")
    private String targetId;

    @Schema(description = "操作详情")
    private String detail;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "操作时间")
    private LocalDateTime createdAt;
}
