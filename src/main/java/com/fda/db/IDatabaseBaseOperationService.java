package com.fda.db;

import java.io.Serializable;
import java.util.List;

public interface IDatabaseBaseOperationService<T extends BaseEntity> {
    /**
     * 增加
     */
    T createPhysical(T data);

    T createLogic(T data);

    List<T> createTransactionPhysical(List<T> datas);

    List<T> createTransactionLogic(List<T> datas);

    List<T> createEffortPhysical(List<T> datas);

    List<T> createEffortLogic(List<T> datas);

    /**
     * 删除
     */
    T deleteByIdPhysical(Serializable id);

    T deleteByIdLogic(Serializable id);

    List<T> deleteTransactionLogic(List<Serializable> ids);

    List<T> deleteTransactionPhysical(List<Serializable> ids);

    List<T> deleteEffortLogic(List<Serializable> ids);

    List<T> deleteEffortPhysical(List<Serializable> ids);


    /**
     * 修改
     */
    ModifyResult<T> update(T data);

    List<ModifyResult<T>> updateTransaction(List<T> datas);

    List<ModifyResult<T>> updateEffort(List<T> datas);

    ModifyResult<T> updateNotNull(T data);

    List<ModifyResult<T>> updateNotNullTransaction(List<T> datas);

    List<ModifyResult<T>> updateNotNullEffort(List<T> datas);

    /**
     * 查询
     */
    T findByIdPhysical(Serializable id);

    T findByIdLogic(Serializable id);

    List<T> findAllPhysical();

    List<T> findAllLogic();

    List<T> findByIdsPhysical(List<Serializable> ids);

    List<T> findByIdsLogic(List<Serializable> ids);

    Result<T> findByConditionPhysical(Class<T> clazz, IQueryCondition condition);

    Result<T> findByConditionLogic(Class<T> clazz, IQueryCondition condition);

    Result<T> findByExamplePhysical(T data, Integer pageSize, Integer pageNum, String sort);

    Result<T> findByExampleLogic(T data, Integer pageSize, Integer pageNum, String sort);

}