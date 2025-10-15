package com.game.init;

import com.game.cache.CacheManager;
import com.game.cache.enhance.CacheEnhanceManager;
import com.game.common.util.ScheduleUtil;
import com.game.constant.SystemInitializeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 缓存初始化器
 * 在系统启动时初始化所有缓存
 */
@Component
public class CacheInitializer implements SystemInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheInitializer.class);
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private CacheEnhanceManager cacheEnhanceManager;
    
    @Override
    public int getOrder() {
        return SystemInitializeOrder.CACHE;
    }
    
    @Override
    public void initialize() throws Exception {
        // 自动发现并注册所有缓存
        cacheManager.autoDiscoverAndRegisterCaches();

        // 并行加载所有缓存数据
        cacheManager.loadAllCachesFromDatabase();

        logger.info("CacheManager initialized with {} caches", cacheManager.getCacheList().size());
        
        // 执行缓存增强初始化
        cacheEnhanceManager.initializeAllEnhancers();
    }
}