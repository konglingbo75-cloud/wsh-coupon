package com.wsh.common.core.util;

import cn.hutool.core.util.IdUtil;

/**
 * ID 生成器（雪花算法）
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * 生成雪花 ID（Long 型）
     */
    public static long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成雪花 ID（String 型）
     */
    public static String nextIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 生成订单号：日期前缀 + 雪花ID后8位
     */
    public static String nextOrderNo() {
        String datePrefix = cn.hutool.core.date.DateUtil.format(
                new java.util.Date(), "yyyyMMdd");
        String snowflake = nextIdStr();
        String suffix = snowflake.substring(Math.max(0, snowflake.length() - 8));
        return datePrefix + suffix;
    }

    /**
     * 生成券码：32位UUID（无横杠）
     */
    public static String nextVoucherCode() {
        return IdUtil.fastSimpleUUID();
    }
}
