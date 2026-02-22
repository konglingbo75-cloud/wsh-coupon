package com.wsh.equity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@Schema(description = "即将过期的权益列表响应")
public class ExpiringEquityResponse {

    @Schema(description = "即将过期的权益项列表")
    private List<ExpiringEquityItem> items;

    @Schema(description = "即将过期权益总数")
    private Integer totalCount;
}
