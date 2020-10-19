package com.fda.db;

import lombok.Data;

/**
 * 数值类型属性查询条件
 *
 * @author Hulunliang
 * @since 2020/3/29.
 */
@Data
public class NumberQueryCondition {
    /**
     * 原始值，取值第一优先级
     */
    private Number originalValue;

    /**
     * 最小值，取值第二优先级
     */
    private Number minValue;

    /**
     * 最大值，取值第二优先级
     */
    private Number maxValue;

    /**
     * 在给定的范围内，取值第三优先级
     */
    private String inValue;
}
