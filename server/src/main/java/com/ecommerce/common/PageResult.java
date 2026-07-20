package com.ecommerce.common;

import java.util.List;

/**
 * 分页返回结果
 * @param <T> 数据类型
 */
public class PageResult<T> {
    private long total;      // 总记录数
    private long page;       // 当前页码
    private long size;       // 每页大小
    private List<T> records; // 当前页数据

    public PageResult() {}

    public PageResult(long total, long page, long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
