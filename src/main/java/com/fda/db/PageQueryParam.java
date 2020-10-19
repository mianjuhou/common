package com.fda.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询参数
 *
 * @author Hulunliang
 * @since 2020/3/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQueryParam {

    /**
     * 查询当前页
     */
    private Integer pageIndex;

    /**
     * 查询当前页
     */
    private Integer pageSize;

    /**
     * 排序顺序，可为多个
     */
    private String sortOrders;

    /**
     * 排序属性，可为多个
     */
    private String sortProperties;

    public PageQueryParam(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public PageQueryParam(String sortOrders, String sortProperties) {
        this.sortOrders = sortOrders;
        this.sortProperties = sortProperties;
    }

}
