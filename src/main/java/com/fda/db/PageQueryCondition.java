package com.fda.db;

import lombok.Data;

/**
 * @author Hulunliang
 * @since 2020/4/10.
 */
@Data
public abstract class PageQueryCondition implements IQueryCondition {

    /**
     * 查询当前页
     */
    private Integer pageIndex;

    /**
     * 查询当前页
     */
    private Integer pageSize;

    /**
     * 排序顺序，取值为ASC/DESC，可为多个，多个属性间用逗号分隔
     */
    private String sortOrders;

    /**
     * 排序属性，可为多个，多个属性间用逗号分隔
     */
    private String sortProperties;
}
