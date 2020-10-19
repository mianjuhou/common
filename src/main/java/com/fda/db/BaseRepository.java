package com.fda.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * 基于DO实体类基类的JpaRepository
 *
 * @author Hulunliang
 * @since 2019/6/5.
 */
@NoRepositoryBean
public interface BaseRepository<M extends BaseEntity> extends JpaRepository<M , Serializable>, JpaSpecificationExecutor<M> {
}
