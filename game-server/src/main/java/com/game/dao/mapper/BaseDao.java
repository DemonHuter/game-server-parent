package com.game.dao.mapper;

import com.game.dao.entity.BaseEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 基础DAO接口
 * @param <T> 实体类型
 */
public interface BaseDao<T extends BaseEntity> {
    
    /**
     * 根据ID查询
     */
    T selectById(@Param("id") Long id);
    
    /**
     * 查询所有记录
     */
    List<T> selectAll();
    
    /**
     * 插入记录
     */
    int insert(T entity);
    
    /**
     * 更新记录
     */
    int update(T entity);
    
    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 统计总数
     */
    long count();
}