package com.fda.db;

import lombok.Data;

/**
 * 全表模糊查询条件类型
 * keyValue 查询关键字 需要加入%通配符
 * columnNames 需要查询的字段 字段类型要和数据库中的一致，使用[]括起来，使用逗号分隔的字符串，例如： [name,value,remark]
 */
@Data
public class KeywordQueryCondition {
    private String keyValue;
    private String columnNames;
}
