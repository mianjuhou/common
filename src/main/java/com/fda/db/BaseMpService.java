package com.fda.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fda.reflect.Reflections;
import com.fda.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Transactional
public abstract class BaseMpService<M extends BaseMapper<T>, T extends BaseEntity<T>> extends AbstractDatabaseBaseOperationService<T> {
    @Autowired
    public M mapper;

    @Override
    protected T insert(T data) {
        int retNum = mapper.insert(data);
        if (retNum <= 0) {
            return null;
        }
        return data;
    }

    @Override
    protected T removeById(Serializable id) {
        T retData = queryById(id);
        if (retData == null) {
            return null;
        }
        int retNum = mapper.deleteById(id);
        if (retNum <= 0) {
            return null;
        }
        return retData;
    }

    @Override
    protected T modify(T data, boolean useNotNull) {
        if (useNotNull) {
            int updateNum = mapper.updateById(data);
            if (updateNum <= 0) {
                return null;
            }
            Serializable id = BaseEntity.getIdValue(data);
            return queryById(id);
        } else {
            String idPropertyName = BaseEntity.getIdPropertyName(data);
            Serializable id = BaseEntity.getIdValue(data);
            UpdateWrapper<T> wrapper = new UpdateWrapper<>();
            wrapper.eq(idPropertyName, id);
            List<String> fieldNameList = Stream.of(data.getClass().getFields()).map(field -> field.getName()).collect(Collectors.toList());
            Stream.of(data.getClass().getDeclaredFields())
                    .filter(declaredField -> !fieldNameList.contains(declaredField.getName()))
                    .forEach(declaredField -> {
                        String fieldName = declaredField.getName();
                        String columnName = ServiceUtil.getFieldNameFromBaseEntityByPropertyName(data, fieldName);
                        Object fieldValue = Reflections.getFieldValue(data, fieldName);
                        wrapper.set(fieldValue == null, columnName, fieldValue);
                    });
            int update = mapper.update(data, wrapper);
            if (update <= 0) {
                return null;
            }
            return queryById(id);
        }
    }

    @Override
    protected T queryById(Serializable id) {
        T result = mapper.selectById(id);
        return result;
    }

    @Override
    protected List<T> queryAll(boolean deleteFlagSense) {
        if (deleteFlagSense) {
            QueryWrapper<T> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", 1);
            return mapper.selectList(wrapper);
        } else {
            return mapper.selectList(null);
        }
    }

    @Override
    protected List<T> queryByIds(List<Serializable> ids) {
        List<T> results = mapper.selectBatchIds(ids);
        return results;
    }

    @Override
    protected Result<T> queryByCondition(Class<T> clazz, IQueryCondition queryCondition, boolean deleteFlagSense) {
        T entity = Reflections.newInstance(clazz);
        if (!deleteFlagSense) {
            entity.setDeleteFlag(null);
        }
        QueryWrapper<T> queryWrapper = new QueryWrapper<>(entity);
        this.setQueryWrapperByQueryCondition(queryCondition, entity, queryWrapper);

        if (queryCondition instanceof PageQueryCondition) {
            PageQueryCondition pageQueryCondition = (PageQueryCondition) queryCondition;
            if (pageQueryCondition.getPageIndex() == null || pageQueryCondition.getPageSize() == null || pageQueryCondition.getPageIndex() < 1 || pageQueryCondition.getPageSize() < 1) {
                PageQueryParam pageQueryParam = new PageQueryParam(pageQueryCondition.getSortOrders(), pageQueryCondition.getSortProperties());
                // 不分页查询，直接返回符合条件的所有记录集合
                ServiceUtil.handleQueryWrapperOrderFields(queryWrapper, pageQueryParam);
                if (log.isDebugEnabled()) {
                    log.debug("$$$ CustomSqlSegment = [{}]", queryWrapper.getCustomSqlSegment());
                }
                List<T> list = this.mapper.selectList(queryWrapper);
                return Result.success("不分页查询成功", list, (long) list.size());
            } else {
                // 分页查询，直接返回符合条件的所有记录集合
                Page<T> page = new Page();
                PageQueryParam pageQueryParam = new PageQueryParam(pageQueryCondition.getPageIndex(), pageQueryCondition.getPageSize(), pageQueryCondition.getSortOrders(), pageQueryCondition.getSortProperties());
                ServiceUtil.fillPagePropertiesByPageQueryParam(clazz, page, pageQueryParam);
                if (log.isDebugEnabled()) {
                    log.debug("$$$ CustomSqlSegment = [{}]", queryWrapper.getCustomSqlSegment());
                }
                IPage<T> selectPage = this.mapper.selectPage(page, queryWrapper);
                return Result.success("分页查询成功", selectPage.getRecords(), selectPage.getTotal());
            }
        } else {
            List<T> list = this.mapper.selectList(queryWrapper);
            return Result.success("查询成功", list, (long) list.size());
        }
    }

    @Override
    protected Result<T> queryExample(T data, Integer pageSize, Integer pageNum, String sort, boolean deleteFlagSense) {
        return null;
    }

    public void setQueryWrapperByQueryCondition(IQueryCondition queryCondition, BaseEntity<T> entity, QueryWrapper<T> queryWrapper) {
        List<Field> queryConditionFieldList = Reflections.getAllFieldsList(queryCondition.getClass());
        List<KeywordQueryCondition> keywordQueryConditions = new ArrayList<>();
        for (Field field : queryConditionFieldList) {
            String fieldName = field.getName();
            Object fieldValue = Reflections.getFieldValue(queryCondition, fieldName);
            if (fieldValue == null) {
                continue;
            }

            Field entityField = Reflections.getAccessibleField(entity, fieldName);
            if (entityField == null) {
                continue;
            }
            String columnName = ServiceUtil.getFieldNameFromBaseEntityByPropertyName(entity, fieldName);

            Class entityFieldType = entityField.getType(), conditionFieldType = field.getType();
            if (entityFieldType.equals(Date.class) && conditionFieldType.equals(DateQueryCondition.class)) {
                handleDateCondition(queryWrapper, columnName, fieldValue);
            } else if (Number.class.isAssignableFrom(entityFieldType) && conditionFieldType.equals(NumberQueryCondition.class)) {
                handleNumberCondition(queryWrapper, columnName, fieldValue);
            } else if (conditionFieldType.equals(KeywordQueryCondition.class)) {
                keywordQueryConditions.add((KeywordQueryCondition) fieldValue);
            } else if (conditionFieldType.equals(entityFieldType)) {
                if (conditionFieldType.equals(String.class)) {
                    String strValue = StrUtil.trim((String) fieldValue);
                    if (StringUtils.isNotEmpty(strValue)) {
                        handleStringCondition(queryWrapper, columnName, strValue);
                    }
                } else {
                    Reflections.setFieldValue(entity, fieldName, fieldValue);
                }
            }
        }
        if (!keywordQueryConditions.isEmpty()) {
            handleKeywordCondition(queryWrapper, keywordQueryConditions);
        }
    }

    /**
     * 处理全局模糊查询类型字段的条件
     *
     * @param queryWrapper
     * @param keywordQueryConditions
     */
    public void handleKeywordCondition(QueryWrapper<T> queryWrapper, List<KeywordQueryCondition> keywordQueryConditions) {
        queryWrapper.and(tQueryWrapper -> {
            for (int i = 0; i < keywordQueryConditions.size(); i++) {
                KeywordQueryCondition keywordQueryCondition = keywordQueryConditions.get(i);
                String keyValue = (String) Reflections.getFieldValue(keywordQueryCondition, "keyValue");
                String columnNames = (String) Reflections.getFieldValue(keywordQueryCondition, "columnNames");
                if (columnNames.startsWith("[") && columnNames.endsWith("]")) {
                    List<String> columnNameList = ServiceUtil.convertStringCollectionToList(columnNames);
                    for (int j = 0; j < columnNameList.size(); j++) {
                        String columnName = columnNameList.get(j);
                        if (keyValue.startsWith("%") && keyValue.endsWith("%")) {
                            tQueryWrapper.like(columnName, keyValue.substring(1, keyValue.length() - 1));
                        } else if (keyValue.startsWith("%")) {
                            tQueryWrapper.likeLeft(columnName, keyValue.substring(1));
                        } else if (keyValue.endsWith("%")) {
                            tQueryWrapper.likeRight(columnName, keyValue.substring(0, keyValue.length() - 1));
                        } else if (keyValue.contains("%")) {
                            tQueryWrapper.like(columnName, keyValue);
                        } else {
                            tQueryWrapper.eq(columnName, keyValue);
                        }
                        if (j < columnNameList.size() - 1) {
                            tQueryWrapper.or();
                        }
                    }
                }
                if (i < keywordQueryConditions.size() - 1) {
                    tQueryWrapper.or();
                }
            }
        });
    }

    /**
     * 处理数值类型字段的条件
     *
     * @param queryWrapper QueryWrapper对象
     * @param columnName   字段名
     * @param fieldValue   字段值
     */
    public void handleStringCondition(QueryWrapper<T> queryWrapper, String columnName, String fieldValue) {
        if (fieldValue.startsWith("[") && fieldValue.endsWith("]")) {
            List<String> valList = ServiceUtil.convertStringCollectionToList(fieldValue);
            queryWrapper.in(columnName, valList);
        } else if (fieldValue.startsWith("%") && fieldValue.endsWith("%")) {
            queryWrapper.like(columnName, fieldValue.substring(1, fieldValue.length() - 1));
        } else if (fieldValue.startsWith("%")) {
            queryWrapper.likeLeft(columnName, fieldValue.substring(1));
        } else if (fieldValue.endsWith("%")) {
            queryWrapper.likeRight(columnName, fieldValue.substring(0, fieldValue.length() - 1));
        } else if (fieldValue.contains("%")) {
            queryWrapper.like(columnName, fieldValue);
        } else {
            queryWrapper.eq(columnName, fieldValue);
        }
    }

    /**
     * 处理数值类型字段的条件
     *
     * @param queryWrapper QueryWrapper对象
     * @param columnName   字段名
     * @param fieldValue   字段值
     */
    public void handleNumberCondition(QueryWrapper<T> queryWrapper, String columnName, Object fieldValue) {
        NumberQueryCondition numberQueryCondition = (NumberQueryCondition) fieldValue;
        if (numberQueryCondition.getOriginalValue() != null) {
            queryWrapper.eq(columnName, numberQueryCondition.getOriginalValue());
            numberQueryCondition.setInValue(null);
            numberQueryCondition.setMaxValue(null);
            numberQueryCondition.setMinValue(null);
            log.info("处理数值型条件: 字段 {} 设置相等值 {}", columnName, numberQueryCondition.getOriginalValue());
        } else if (numberQueryCondition.getMaxValue() != null || numberQueryCondition.getMinValue() != null) {
            if (numberQueryCondition.getMaxValue() != null) {
                queryWrapper.le(columnName, numberQueryCondition.getMaxValue());
                numberQueryCondition.setInValue(null);
                log.info("处理数值型条件: 字段 {} 设置最大值 {}", columnName, numberQueryCondition.getMaxValue());
            }
            if (numberQueryCondition.getMinValue() != null) {
                queryWrapper.ge(columnName, numberQueryCondition.getMinValue());
                numberQueryCondition.setInValue(null);
                log.info("处理数值型条件: 字段 {} 设置最小值 {}", columnName, numberQueryCondition.getMinValue());
            }
        } else if (StringUtils.isNotBlank(numberQueryCondition.getInValue())) {
            List<String> valList = ServiceUtil.convertStringCollectionToList(numberQueryCondition.getInValue().trim());
            for (String val : valList) {
                try {
                    Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    log.error("in 集合 {} 中存在非法数字: {}", valList, val);
                    return;
                }
            }
            log.info("处理数值型条件: 字段 {} 设置集合值 {}", columnName, valList);
            queryWrapper.in(columnName, valList);
        }
    }

    /**
     * 处理日期类型字段的条件
     *
     * @param queryWrapper QueryWrapper对象
     * @param columnName   字段名
     * @param fieldValue   字段值
     */
    public void handleDateCondition(QueryWrapper<T> queryWrapper, String columnName, Object fieldValue) {
        DateQueryCondition dateQueryCondition = (DateQueryCondition) fieldValue;
        if (dateQueryCondition.isTimeNotLimit()) {
            return;
        }
        if (dateQueryCondition.isQueryTimeScope()) {
            log.info("{}设置时间范围：{} - {}", columnName, dateQueryCondition.getStartTime(), dateQueryCondition.getEndTime());
            Date start = dateQueryCondition.getStartTime();
            if (start != null) {
                queryWrapper.ge(columnName, start);
            }
            Date end = dateQueryCondition.getEndTime();
            if (end != null) {
                queryWrapper.le(columnName, end);
            }
            dateQueryCondition.setCurrentTime(null);
        } else {
            log.info("{}设置精确时间条件：{} ", columnName, dateQueryCondition.getCurrentTime());
            queryWrapper.eq(columnName, dateQueryCondition.getCurrentTime());
        }
    }
}