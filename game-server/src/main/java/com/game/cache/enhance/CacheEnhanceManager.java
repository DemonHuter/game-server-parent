package com.game.cache.enhance;

import com.game.cache.BaseCache;
import com.game.cache.CacheManager;
import com.game.cache.CounterCache;
import com.game.cache.PlayerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 缓存增强管理器
 * 负责管理所有缓存增强器的初始化和执行
 */
@Component
public class CacheEnhanceManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheEnhanceManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 初始化所有缓存增强器
     */
    public void initializeAllEnhancers() {
        logger.info("开始初始化所有缓存增强器");
        
        try {
            // 获取所有实现了CacheEnhancer接口的Bean
            Map<String, CacheEnhancer> enhancerBeans = applicationContext.getBeansOfType(CacheEnhancer.class);
            
            if (enhancerBeans.isEmpty()) {
                logger.info("未找到任何缓存增强器");
                return;
            }
            
            logger.info("找到 {} 个缓存增强器，开始初始化", enhancerBeans.size());
            
            // 将增强器按类型分组并排序
            List<CacheEnhancer> sortedEnhancers = new ArrayList<>(enhancerBeans.values());
            sortedEnhancers.sort((e1, e2) -> Integer.compare(e1.getOrder(), e2.getOrder()));
            
            // 循环调用每个增强器的初始化方法
            for (CacheEnhancer enhancer : sortedEnhancers) {
                try {
                    logger.info("正在初始化增强器: {} (order: {})", enhancer.getName(), enhancer.getOrder());
                    
                    // 根据增强器类型调用对应的初始化方法
                    initializeEnhancer(enhancer);
                    
                    logger.info("增强器 {} 初始化完成", enhancer.getName());
                } catch (Exception e) {
                    logger.error("初始化增强器 {} 失败", enhancer.getName(), e);
                    // 不中断其他增强器的初始化
                }
            }
            
            logger.info("所有缓存增强器初始化完成");
        } catch (Exception e) {
            logger.error("缓存增强器初始化过程中发生错误", e);
            throw new RuntimeException("缓存增强器初始化失败", e);
        }
    }
    
    /**
     * 根据增强器类型调用对应的初始化方法
     * @param enhancer 缓存增强器
     */
    @SuppressWarnings("unchecked")
    private void initializeEnhancer(CacheEnhancer enhancer) {
        // 通过增强器的getTargetCacheClass方法获取目标缓存类型
        Class<? extends BaseCache> targetCacheClass = enhancer.getTargetCacheClass();
        
        if (targetCacheClass != null) {
            // 从CacheManager中获取对应类型的缓存实例
            BaseCache cache = cacheManager.getCache(targetCacheClass);
            if (cache != null) {
                // 调用增强器的初始化方法
                enhancer.initialize(cache);
            } else {
                logger.warn("未找到类型为 {} 的缓存实例", targetCacheClass.getSimpleName());
            }
        } else {
            logger.warn("增强器 {} 未指定目标缓存类型", enhancer.getName());
        }
    }
}