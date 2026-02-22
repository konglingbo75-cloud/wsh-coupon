package com.wsh.common.core.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long page, long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}
