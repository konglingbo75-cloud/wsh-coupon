package com.wsh.matching.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "会员匹配结果响应")
public class MemberMatchResponse {

    @Schema(description = "匹配成功的商户数量")
    private Integer matchedCount;

    @Schema(description = "匹配成功的商户ID列表")
    private List<Long> matchedMerchantIds;

    @Schema(description = "匹配到的专属活动数量")
    private Integer activityCount;
}
