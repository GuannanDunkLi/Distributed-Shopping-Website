package com.ecommerce.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private Integer totalPage;
    private List<T> items; // data in current page

    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageResult(Long total, Integer totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }
}