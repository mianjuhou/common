package com.fda.db;

import com.fda.exceptions.UipServiceException;
import com.fda.reflect.Reflections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hulunliang
 * @since 2020/4/4.
 */
@Slf4j
@Transactional
public abstract class AbstractDatabaseBaseOperationService<T extends BaseEntity> implements IDatabaseBaseOperationService<T> {
    /**
     * 物理新增
     */
    @Override
    public T createPhysical(T data) {
        Serializable id = data.getId();
        if (id != null && (null != queryById(id))) {
            throw new UipServiceException("主键冲突，要创建的" + ServiceUtil.getEntityDisplayName(data.getClass()) + "记录已存在！");
        }
        data.setGmtCreated(new Date());
        data.setDeleteFlag(0);
        data.setGmtModified(new Date());
        try {
            T retData = insert(data);
            return retData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<T> createTransactionPhysical(List<T> datas) {
        List<T> failList = new ArrayList<>();
        List<T> succList = new ArrayList<>();
        datas.forEach(data -> {
            try {
                succList.add(this.createPhysical(data));
            } catch (UipServiceException e) {
                failList.add(data);
            }
        });
        if (!failList.isEmpty()) {
            this.handleBatchOperationFailResult(succList, failList);
        }
        return succList;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<T> createEffortPhysical(List<T> datas) {
        List succList = new ArrayList<>();
        datas.forEach(data -> {
            T retData = this.createPhysical(data);
            succList.add(retData);
        });
        return succList;
    }

    /**
     * 逻辑新增
     */
    @Override
    public T createLogic(T data) {
        T targetData = (T) Reflections.newInstance(data.getClass());
        BeanUtils.copyProperties(data, targetData, "gmt_created", "gmt_modified", "id");
        targetData.setDeleteFlag(1);
        Result<T> retData = queryExample(targetData, null, null, null, false);
        if (retData.getData() == null || retData.getData().isEmpty()) {
            return createPhysical(data);
        } else {
            T dbData = retData.getData().get(0);
            dbData.setDeleteFlag(0);
            dbData.setGmtCreated(new Date());
            dbData.setGmtModified(new Date());
            return updateNotNull(dbData).getNewData();
        }
    }

    @Override
    public List<T> createTransactionLogic(List<T> datas) {
        List<T> failList = new ArrayList<>();
        List<T> succList = new ArrayList<>();
        datas.forEach(data -> {
            try {
                succList.add(this.createLogic(data));
            } catch (UipServiceException e) {
                failList.add(data);
            }
        });
        if (!failList.isEmpty()) {
            this.handleBatchOperationFailResult(succList, failList);
        }
        return succList;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<T> createEffortLogic(List<T> datas) {
        List succList = new ArrayList<>();
        datas.forEach(data -> {
            T retData = this.createLogic(data);
            succList.add(retData);
        });
        return succList;
    }

    /**
     * 物理删除
     */
    @Override
    public T deleteByIdPhysical(Serializable id) {
        return removeById(id);
    }

    @Override
    public List<T> deleteTransactionPhysical(List<Serializable> ids) {
        List succList = new ArrayList<>();
        ids.forEach(id -> {
            T retData = this.deleteByIdPhysical(id);
            succList.add(retData);
        });
        return succList;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<T> deleteEffortPhysical(List<Serializable> ids) {
        List failList = new ArrayList<>();
        List<T> succList = new ArrayList<>();
        ids.forEach(id -> {
            try {
                T retData = this.deleteByIdPhysical(id);
                succList.add(retData);
            } catch (UipServiceException e) {
                failList.add(id);
            }
        });
        if (!failList.isEmpty()) {
            this.handleBatchOperationFailResult(succList, failList);
        }
        return succList;
    }

    /**
     * 逻辑删除
     */
    @Override
    public T deleteByIdLogic(Serializable id) {
        T retData = queryById(id);
        if (null == retData) {
            return null;
        }
        retData.setDeleteFlag(1);
        return update(retData).getNewData();
    }

    @Override
    public List<T> deleteTransactionLogic(List<Serializable> ids) {
        List succList = new ArrayList<>();
        ids.forEach(id -> {
            T retData = this.deleteByIdLogic(id);
            succList.add(retData);
        });
        return succList;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<T> deleteEffortLogic(List<Serializable> ids) {
        List failList = new ArrayList<>();
        List<T> succList = new ArrayList<>();
        ids.forEach(id -> {
            try {
                T retData = this.deleteByIdLogic(id);
                succList.add(retData);
            } catch (UipServiceException e) {
                failList.add(id);
            }
        });
        if (!failList.isEmpty()) {
            this.handleBatchOperationFailResult(succList, failList);
        }
        return succList;
    }

    /**
     * 全量修改
     */
    @Override
    public ModifyResult<T> update(T data) {
        T retData = modify(data, false);
        ModifyResult<T> modifyResult = new ModifyResult<>();
        modifyResult.setNewData(retData);
        modifyResult.setOldData(data);
        return modifyResult;
    }

    @Override
    public List<ModifyResult<T>> updateTransaction(List<T> datas) {
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<ModifyResult<T>> updateEffort(List<T> datas) {
        return null;
    }

    /**
     * 非空修改
     */
    @Override
    public ModifyResult<T> updateNotNull(T data) {
        T retData = modify(data, true);
        ModifyResult<T> modifyResult = new ModifyResult<>();
        modifyResult.setNewData(retData);
        modifyResult.setOldData(data);
        return modifyResult;
    }

    @Override
    public List<ModifyResult<T>> updateNotNullTransaction(List<T> datas) {
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<ModifyResult<T>> updateNotNullEffort(List<T> datas) {
        return null;
    }

    /**
     * 物理查询
     */
    @Override
    public T findByIdPhysical(Serializable id) {
        if (id == null) {
            throw new UipServiceException("必须给出主键ID值");
        }
        T data = this.queryById(id);
        if (data == null) {
            throw new UipServiceException("记录不存在");
        }
        return data;
    }

    @Override
    public List<T> findAllPhysical() {
        List<T> datas = queryAll(false);
        return datas;
    }

    @Override
    public List<T> findByIdsPhysical(List<Serializable> ids) {
        return queryByIds(ids);
    }

    @Override
    public Result<T> findByConditionPhysical(Class<T> clazz, IQueryCondition condition) {
        Result<T> result = queryByCondition(clazz, condition, false);
        return result;
    }

    @Override
    public Result<T> findByExamplePhysical(T data, Integer pageSize, Integer pageNum, String sort) {
        return queryExample(data, pageSize, pageNum, sort, false);
    }

    /**
     * 逻辑查询
     */
    @Override
    public T findByIdLogic(Serializable id) {
        if (id == null) {
            throw new UipServiceException("必须给出主键ID值");
        }
        T data = this.queryById(id);
        if (data == null || data.getDeleteFlag() == 1) {
            throw new UipServiceException("记录不存在");
        }
        return data;
    }

    @Override
    public List<T> findAllLogic() {
        List<T> datas = queryAll(true);
        return datas;
    }

    @Override
    public List<T> findByIdsLogic(List<Serializable> ids) {
        if (ids == null || ids.size() == 0) {
            throw new UipServiceException("必须给出主键ID值");
        }
        List<T> results = queryByIds(ids).stream().filter(data -> data.getDeleteFlag() == 0).collect(Collectors.toList());
        return results;
    }

    @Override
    public Result<T> findByConditionLogic(Class<T> clazz, IQueryCondition condition) {
        Result<T> result = queryByCondition(clazz, condition, true);
        return result;
    }

    @Override
    public Result<T> findByExampleLogic(T data, Integer pageSize, Integer pageNum, String sort) {
        return queryExample(data, pageSize, pageNum, sort, true);
    }

    /**
     * 下面是被继承实现的接口
     */

    /**
     * 新增
     */
    protected abstract T insert(T data);


    /**
     * 删除
     */
    protected abstract T removeById(Serializable id);

    /**
     * 修改
     */
    protected abstract T modify(T data, boolean useNotNull);

    /**
     * 查询
     */
    protected abstract T queryById(Serializable id);

    protected abstract List<T> queryAll(boolean deleteFlagSense);

    protected abstract List<T> queryByIds(List<Serializable> ids);

    protected abstract Result<T> queryByCondition(Class<T> clazz, IQueryCondition condition, boolean deleteFlagSense);

    protected abstract Result<T> queryExample(T data, Integer pageSize, Integer pageNum, String sort, boolean deleteFlagSense);


    /**
     * 处理批处理操作返回结果
     *
     * @param succList 执行成功列表
     * @param failList 执行失败列表
     * @return 根据成功失败的数量确定返回成功结果列表，失败结果异常信息
     */
    private void handleBatchOperationFailResult(List succList, List<T> failList) {
        if (succList.isEmpty()) {
            throw new UipServiceException("批量操作全部失败");
        } else {
            throw new UipServiceException("批量操作部分失败");
        }
    }
}
