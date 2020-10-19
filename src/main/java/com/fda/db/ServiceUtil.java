package com.fda.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fda.reflect.Reflections;
import com.fda.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServiceUtil {
    private static final IdWorker idWorker = new IdWorker();

    /**
     * 将形如“[abc,def,ge]”的表示集合的字符串转换为集合
     *
     * @param values 表示集合的字符串
     * @return 转换后的集合List
     */
    public static List<String> convertStringCollectionToList(String values) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(values) || values.length() == 2) {
            return list;
        }
        String[] arr = values.substring(1, values.length() - 1).split(",");
        for (int i = 0, size = arr.length; i < size; i++) {
            if (!StringUtils.isEmpty(arr[i])) {
                list.add(arr[i]);
            }
        }
        return list;
    }

    /**
     * 将给定对象给定属性设置为Snowflake ID，如果该竖向为空则设置，否则不设置
     *
     * @param entity   给定的对象
     * @param property 给定的属性
     */
    public static void setSnowflakeValue2NullProperty(Object entity, String property) {
        Object primaryKeyValue = Reflections.getFieldValue(entity, property);
        if (primaryKeyValue == null) {
            Reflections.setFieldValue(entity, property, idWorker.nextId());
        }
    }

    /**
     * 根据给定的属性名从BaseEntity对象中获取数据库字段名
     *
     * @param entity       BaseEntity对象
     * @param propertyName 属性名
     * @return 数据库字段名
     */
    public static String getFieldNameFromBaseEntityByPropertyName(BaseEntity entity, String propertyName) {
        if (Objects.isNull(entity) || StringUtils.isEmpty(propertyName)) {
            return null;
        }
        return getFieldNameByPropertyName(entity.getClass(), propertyName);
    }

    /**
     * 根据给定的属性名和类获取数据库字段名
     *
     * @param clazz        实体类Class
     * @param propertyName 属性名
     * @return 数据库字段名
     */
    public static String getFieldNameByPropertyName(Class clazz, String propertyName) {
        if (StringUtils.isEmpty(propertyName)) {
            return null;
        }
        String fieldName = null;
        Field[] fields = FieldUtils.getAllFields(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals(propertyName)) {
                TableField annotation = field.getAnnotation(TableField.class);
                if (annotation != null) {
                    fieldName = annotation.value();
                } else {
                    TableId idAnnotation = field.getAnnotation(TableId.class);
                    if (idAnnotation != null) {
                        fieldName = idAnnotation.value();
                    }
                }
                break;
            }
        }
        return fieldName;
    }

    /**
     * 处理QueryWrapper对象的排序
     *
     * @param queryWrapper   QueryWrapper对象
     * @param pageQueryParam PageQueryParam对象
     */
    public static void handleQueryWrapperOrderFields(QueryWrapper queryWrapper, PageQueryParam pageQueryParam) {
        if (Objects.isNull(queryWrapper) || Objects.isNull(pageQueryParam)) {
            return;
        }
        handlePageAndQueryWrapperOrderFields(pageQueryParam, queryWrapper.getEntity().getClass(), queryWrapper, null);
    }

    /**
     * 处理排序
     *
     * @param pageQueryParam 分页查询条件
     * @param clazz          实体对象JAVA类型
     * @param queryWrapper   条件构造器对象，通过该对象进行分页查询时用
     * @param page           分页对象，通过该对象进行分页查询时用
     * @return 排序部分SQL，自拼SQL时使用
     */
    private static String handlePageAndQueryWrapperOrderFields(PageQueryParam pageQueryParam, Class clazz, QueryWrapper queryWrapper, Page page) {
        String sortProperties = pageQueryParam.getSortProperties();
        if (StringUtils.isBlank(sortProperties)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<String> propertyList = StrUtil.split(sortProperties, ',');
        List<String> orderList = StrUtil.split(pageQueryParam.getSortOrders(), ',');
        if (orderList.size() < propertyList.size()) {
            for (int i = 0, len = propertyList.size() - orderList.size(); i < len; i++) {
                orderList.add("ASC");
            }
        }
        for (int i = 0, len = propertyList.size(); i < len; i++) {
            String property = propertyList.get(i).trim();
            String orderByColumnName = ServiceUtil.getFieldNameByPropertyName(clazz, property);
            if (orderByColumnName != null) {
                String order = ("DESC".equalsIgnoreCase(orderList.get(i).trim())) ? "DESC" : "ASC";
                stringBuilder.append(orderByColumnName).append(" ").append(order);
                if (i < (len - 1)) {
                    stringBuilder.append(",");
                }
                if (order.equals("DESC")) {
                    if (page != null) {
                        page.addOrder(OrderItem.desc(orderByColumnName));
                    }
                    if (queryWrapper != null) {
                        queryWrapper.orderByDesc(orderByColumnName);
                    }
                } else {
                    if (page != null) {
                        page.addOrder(OrderItem.asc(orderByColumnName));
                    }
                    if (queryWrapper != null) {
                        queryWrapper.orderByAsc(orderByColumnName);
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 根据分页查询条件参数填充分页对象Page的属性
     *
     * @param clazz          分页查询实体类Class
     * @param page           分页对象Page
     * @param pageQueryParam 前端分页查询条件参数对象PageQueryParam
     */
    public static void fillPagePropertiesByPageQueryParam(Class clazz, Page page, PageQueryParam pageQueryParam) {
        if (Objects.isNull(page) || Objects.isNull(pageQueryParam)) {
            return;
        }
        if (pageQueryParam.getPageIndex() != null && pageQueryParam.getPageSize() != null) {
            page.setSize(pageQueryParam.getPageSize());
            page.setCurrent(pageQueryParam.getPageIndex());
        }
        handlePageAndQueryWrapperOrderFields(pageQueryParam, clazz, null, page);
    }

    /**
     * 获取DO实体类显示名称
     *
     * @param clazz DO实体类Class
     * @return 显示名称
     */
    public static String getEntityDisplayName(Class clazz) {
        try {
            return (String) Reflections.readDeclaredStaticField(clazz, "ENTITY_DISPLAY_NAME", true);
        } catch (IllegalAccessException e) {
            return clazz.getSimpleName();
        }
    }
}
