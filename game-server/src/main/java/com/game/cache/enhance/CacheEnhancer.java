package com.game.cache.enhance;

import com.game.cache.BaseCache;
import com.game.dao.entity.BaseEntity;

/**
 * 缓存增强器接口
 * 定义缓存增强器的通用接口
 */
public interface CacheEnhancer<T extends BaseEntity, C extends BaseCache<T>> {
    
    /**
     * 初始化增强缓存数据
     * @param cache 对应的缓存类实例
     */
    void initialize(C cache);
    
    /**
     * 获取增强器的名称
     * @return 增强器名称
     */
    String getName();
    
    /**
     * 获取增强器的执行顺序
     * @return 执行顺序，数字越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 获取目标缓存类型
     * @return 目标缓存类
     */
    Class<C> getTargetCacheClass();
}