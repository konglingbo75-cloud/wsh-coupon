package com.wsh.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 开通城市实体
 */
@Data
@TableName("tb_open_city")
public class OpenCity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long cityId;

    /** 城市编码(如440100) */
    private String cityCode;

    /** 城市名称 */
    private String cityName;

    /** 省份名称 */
    private String provinceName;

    /** 拼音首字母(A-Z) */
    private String pinyin;

    /** 城市级别 1:一线 2:新一线 3:二线 4:三线 */
    private Integer level;

    /** 城市中心经度 */
    private BigDecimal longitude;

    /** 城市中心纬度 */
    private BigDecimal latitude;

    /** 入驻商户数(统计) */
    private Integer merchantCount;

    /** 活动数(统计) */
    private Integer activityCount;

    /** 状态 0:未开放 1:已开放 */
    private Integer status;

    /** 排序权重(越大越靠前) */
    private Integer sortOrder;

    /** 开通日期 */
    private LocalDate openDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
