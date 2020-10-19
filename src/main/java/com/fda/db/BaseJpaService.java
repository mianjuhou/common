package com.fda.db;

import com.github.wenhao.jpa.Specifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Transactional
public abstract class BaseJpaService<T extends BaseEntity, R extends BaseRepository> extends AbstractDatabaseBaseOperationService<T> {
    @Autowired
    public R repository;

    @Override
    protected T insert(T data) {
        return (T) repository.save(data);
    }

    @Override
    protected T removeById(Serializable id) {
        T retData = queryById(id);
        repository.deleteById(id);
        return retData;
    }

    @Override
    protected T modify(T data, boolean useNotNull) {
        T retData = queryById(data.getId());
        if (useNotNull) {
            //属性拷贝
            //只拷贝源的非空属性
            //只拷贝目标中为空的属性
            BeanUtils.copyProperties(data, retData, getNullPropertyNames(data));
            return (T) repository.save(retData);
        } else {
            data.setGmtCreated(retData.getGmtCreated());
            data.setGmtModified(new Date());
            return (T) repository.save(data);
        }
    }

    @Override
    protected T queryById(Serializable id) {
        Optional data = repository.findById(id);
        return data.isPresent() ? (T) data.get() : null;
    }

    @Override
    protected List<T> queryAll(boolean deleteFlagSense) {
        if (deleteFlagSense) {
            Specification<T> specification =
                    Specifications.<T>and()
                            .eq("deleteFlag", 0)
                            .build();
            List<T> results = repository.findAll(specification);
            return results;
        } else {
            return repository.findAll();
        }
    }

    @Override
    protected List<T> queryByIds(List<Serializable> ids) {
        List<T> results = repository.findAllById(ids);
        return results;
    }

    @Override
    protected Result<T> queryByCondition(Class<T> clazz, IQueryCondition condition, boolean deleteFlagSense) {
        return null;
    }

    @Override
    protected Result<T> queryExample(T data, Integer pageSize, Integer pageNum, String sort, boolean deleteFlagSense) {
        if (deleteFlagSense) {
            data.setDeleteFlag(0);
        }
        Example<T> example = Example.of(data);
        Page<T> pageResult;
        List<Sort.Order> orderList = new ArrayList<>();
        if (!StringUtils.isEmpty(sort)) {
            String[] split = sort.split(",");
            for (int i = 0; i < split.length; i = i + 2) {
                String proper = split[i];
                String direct = i + 1 >= split.length ? "" : split[i + 1].toLowerCase();
                if ("desc".equals(direct)) {
                    orderList.add(Sort.Order.desc(proper));
                } else if ("asc".equals(direct)) {
                    orderList.add(Sort.Order.asc(proper));
                }
            }
        } else {
            orderList.add(Sort.Order.desc("gmtCreated"));
        }
        if (pageSize == null || pageSize <= 0 || pageNum == null || pageNum < 0) {
            List<T> list;
            if (orderList.isEmpty()) {
                list = repository.findAll(example);
            } else {
                list = repository.findAll(example, Sort.by(orderList));
            }
            pageResult = new PageImpl<>(list);
        } else {
            PageRequest pageRequest;
            if (orderList.isEmpty()) {
                pageRequest = PageRequest.of(pageNum, pageSize);
            } else {
                pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(orderList));
            }
            pageResult = repository.findAll(example, pageRequest);
        }
        return Result.success("", pageResult.getContent(), pageResult.getTotalElements());
    }

    /**
     * 获取对象中的值非空的字段名称
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}