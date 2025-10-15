package com.game.cache;

import com.game.core.AbstractSystemShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器
 */
@Component
public class CacheManager extends AbstractSystemShutdown implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    /**
     * 缓存列表
     */
    private final List<BaseCache<?>> cacheList = new ArrayList<>();

    /**
     * Spring 应用上下文
     */
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<BaseCache<?>> getCacheList() {
        return cacheList;
    }

    /**
     * 自动发现并注册所有缓存实例
     */
    public void autoDiscoverAndRegisterCaches() {
        try {
            // 获取所有 BaseCache 类型的 Bean
            Map<String, BaseCache> cacheBeans = applicationContext.getBeansOfType(BaseCache.class);

            for (BaseCache<?> cache : cacheBeans.values()) {
                registerCache(cache);
            }

            logger.info("Auto-discovered {} cache instances", cacheBeans.size());
        } catch (Exception e) {
            logger.error("Failed to auto-discover cache instances", e);
            throw new RuntimeException("Cache auto-discovery failed", e);
        }
    }

    /**
     * 注册缓存
     */
    public void registerCache(BaseCache<?> cache) {
        if (cache != null && !cacheList.contains(cache)) {
            cacheList.add(cache);
            logger.info("Registered cache: {}", cache.getClass().getSimpleName());
        }
    }

    /**
     * 并行加载所有缓存数据
     */
    public void loadAllCachesFromDatabase() {
        logger.info("Starting to load all caches from database...");

        try {
            // 创建并行任务列表
            List<CompletableFuture<Void>> loadTasks = new ArrayList<>();

            for (BaseCache<?> cache : cacheList) {
                CompletableFuture<Void> loadTask = CompletableFuture.runAsync(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        cache.loadFromDatabase();
                        long endTime = System.currentTimeMillis();

                        logger.info("Cache {} loaded successfully in {}ms, size: {}",
                                cache.getClass().getSimpleName(),
                                endTime - startTime,
                                cache.size());
                    } catch (Exception e) {
                        logger.error("Failed to load cache: {}", cache.getClass().getSimpleName(), e);
                        throw new RuntimeException("Cache loading failed: " + cache.getClass().getSimpleName(), e);
                    }
                });

                loadTasks.add(loadTask);
            }

            // 等待所有加载任务完成
            CompletableFuture<Void> allLoadTasks = CompletableFuture.allOf(
                    loadTasks.toArray(new CompletableFuture[0])
            );

            // 设置超时时间防止无限等待
            allLoadTasks.get(30, TimeUnit.SECONDS);

            logger.info("All {} caches loaded successfully from database", cacheList.size());
        } catch (Exception e) {
            logger.error("Failed to load caches from database", e);
            throw new RuntimeException("Cache initialization failed", e);
        }
    }

    /**
     * 同步所有缓存数据到数据库
     */
    public void syncAllDataToDatabase() {
        logger.debug("Starting scheduled cache data sync");
        try {
            for (BaseCache<?> cache : cacheList) {
                cache.asyncSyncAllDataToDatabase();
            }
            logger.debug("Cache data sync completed");
        } catch (Exception e) {
            logger.error("Error during cache data sync", e);
        }
    }

    /**
     * 持久化所有缓存（保持原有方法以兼容性）
     */
    private void persistAllCaches() {
        syncAllDataToDatabase();
    }

    /**
     * 手动触发缓存持久化
     */
    public void manualPersistence() {
        logger.info("Manual cache persistence triggered");
        syncAllDataToDatabase();
    }

    /**
     * 清理缓存
     */
    private void cleanupCaches() {
        logger.debug("Starting cache cleanup");
        try {
            // 简化的清理逻辑，只记录日志
            // 具体的清理逻辑可在各个缓存子类中实现
            logger.debug("Cache cleanup completed");
        } catch (Exception e) {
            logger.error("Error during cache cleanup", e);
        }
    }

    /**
     * 手动触发缓存清理
     */
    public void manualCleanup() {
        logger.info("Manual cache cleanup triggered");
        cleanupCaches();
    }

    /**
     * 获取所有缓存的统计信息
     */
    public List<String> getCacheStats() {
        List<String> stats = new ArrayList<>();
        return stats;
    }

    /**
     * 获取缓存健康状态
     */
    public boolean isHealthy() {
        try {
            // 检查所有缓存是否正常
            for (BaseCache<?> cache : cacheList) {
                if (cache.size() < 0) { // 基本健康检查
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error checking cache health", e);
            return false;
        }
    }

    /**
     * 重新加载所有缓存数据
     */
    public void reloadAllCaches() {
        logger.info("Reloading all cache data");
        try {
            // 清空所有缓存
            for (BaseCache<?> cache : cacheList) {
                cache.clear();
            }

            // 重新并行加载所有缓存
            loadAllCachesFromDatabase();

            logger.info("All caches reloaded successfully");
        } catch (Exception e) {
            logger.error("Error reloading caches", e);
            throw e;
        }
    }

    /**
     * 获取所有缓存实例
     */
    public List<BaseCache<?>> getAllCaches() {
        return new ArrayList<>(cacheList);
    }

    /**
     * 手动加载指定缓存
     */
    public void loadCache(Class<? extends BaseCache<?>> cacheClass) {
        BaseCache<?> cache = getCache(cacheClass);
        if (cache != null) {
            logger.info("Manually loading cache: {}", cacheClass.getSimpleName());
            cache.clear();
            cache.loadFromDatabase();
            logger.info("Cache {} loaded successfully, size: {}",
                    cacheClass.getSimpleName(), cache.size());
        } else {
            logger.warn("Cache not found: {}", cacheClass.getSimpleName());
        }
    }

    /**
     * 获取缓存详细信息
     */
    public List<String> getCacheDetailedStats() {
        List<String> stats = new ArrayList<>();
        for (BaseCache<?> cache : cacheList) {
            String cacheInfo = String.format("%s: size=%d",
                    cache.getClass().getSimpleName(),
                    cache.size());
            stats.add(cacheInfo);
        }
        return stats;
    }

    /**
     * 根据缓存类类型获取缓存实例
     *
     * @param cacheClass 缓存类类型
     * @param <T>        缓存类型
     * @return 缓存实例
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseCache<?>> T getCache(Class<T> cacheClass) {
        for (BaseCache<?> cache : cacheList) {
            if (cacheClass.isInstance(cache)) {
                return (T) cache;
            }
        }
        return null;
    }

    /**
     * 同步所有缓存数据到数据库（阻塞操作，用于系统关闭时）
     */
    public void syncAllCachesToDatabase() {
        for (BaseCache<?> cache : cacheList) {
            try {
                cache.saveAllToDatabase(); // 使用同步方法而不是异步方法
            } catch (Exception e) {
                logger.error("Failed to sync cache {} to database", cache.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void shutdown() {
        // 最后一次持久化，使用同步方法确保数据保存完成
        syncAllCachesToDatabase();
    }
}