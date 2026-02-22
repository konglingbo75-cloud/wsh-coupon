package com.wsh.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询请求")
public class PageQueryRequest {

    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer size = 20;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "状态筛选")
    private Integer status;

    public int getOffset() {
        return (page - 1) * size;
    }
}
