package com.wsh.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "商户活动列表响应")
public class MerchantActivityListResponse {

    @Schema(description = "活动列表")
    private List<ActivityListItem> list;

    @Schema(description = "各状态数量统计，key: all/draft/active/paused/ended")
    private Map<String, Integer> statusCounts;

    @Data
    @Builder
    @Schema(description = "活动列表项")
    public static class ActivityListItem {

        @Schema(description = "活动ID")
        private Long id;

        @Schema(description = "活动标题")
        private String title;

        @Schema(description = "活动类型：1代金券 3积分兑换 4团购")
        private Integer type;

        @Schema(description = "封面图片URL")
        private String coverUrl;

        @Schema(description = "售价")
        private BigDecimal price;

        @Schema(description = "原价")
        private BigDecimal originalPrice;

        @Schema(description = "活动状态：0草稿 1进行中 2暂停 3结束")
        private Integer status;

        @Schema(description = "已售数量")
        private Integer soldCount;

        @Schema(description = "剩余库存")
        private Integer remainStock;

        @Schema(description = "已核销数量")
        private Integer verifiedCount;

        @Schema(description = "收入金额")
        private BigDecimal revenue;

        @Schema(description = "有效开始时间")
        private String validStart;

        @Schema(description = "有效结束时间")
        private String validEnd;
    }
}
