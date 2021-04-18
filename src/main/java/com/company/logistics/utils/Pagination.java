package com.company.logistics.utils;

import lombok.Data;

import java.util.List;

/**
 * @author wzj
 * @date 2020/12/27 0:02
 */
@Data
public class Pagination<T> {

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 总条数
     */
    private Integer total;

    /**
     * 数据
     */
    private List<T> rows;


    public Pagination() {
    }

    public Pagination(Integer page, Integer size, Integer total, List<T> rows) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.rows = rows;
        this.totalPage = (int) Math.ceil((double)total / size);
    }
}
