package com.game.cache;

import com.game.common.constant.PersistType;
import com.game.dao.entity.BaseEntity;
import com.game.dao.mapper.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础缓存类 - 仅提供基于主键的基本增删改查操作
 * <p>
 * 优化说明：
 * 1. 引入分批处理机制，避免一次性处理大量数据导致的性能问题
 * 2. 批量操作采用BATCH_SIZE（默认1000）条记录为一批进行处理
 * 3. 提供详细的错误统计和日志记录
 * 4. 在批量操作失败时提供逐条重试机制
 *
 * @param <T> 实体类型
 */
public abstract class BaseCache<T extends BaseEntity> {

    /**
     * 批量操作大小
     */
    private static final int BATCH_SIZE = 1000;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 内存缓存映射
     */
    protected final ConcurrentHashMap<String, T> cacheMap = new ConcurrentHashMap<>();
    /**
     * 插入操作映射，记录需要插入到数据库的实体
     */
    protected final ConcurrentHashMap<String, T> insertMap = new ConcurrentHashMap<>();
    /**
     * 更新操作映射，记录需要更新到数据库的实体
     */
    protected final ConcurrentHashMap<String, T> updateMap = new ConcurrentHashMap<>();
    /**
     * 删除标记映射，记录需要从数据库删除的实体ID
     */
    protected final ConcurrentHashMap<String, Boolean> deleteMap = new ConcurrentHashMap<>();
    /**
     * 数据访问对象
     */
    protected final BaseDao<T> dao;
    /**
     * 对象锁，用于防止并发操作导致的脏数据
     */
    private final Object lock = new Object();

    public BaseCache(BaseDao<T> dao) {
        this.dao = dao;
    }

    private void add(T entity, PersistType persistType) {
        synchronized (lock) {
            if (persistType == PersistType.INSERT) {
                if (entity == null) {
                    return;
                }
                // 只添加到缓存
                if (entity.getIdx() != null) {
                    cacheMap.put(entity.getIdx(), entity);
                    // 添加到插入映射
                    insertMap.put(entity.getIdx(), entity);
                    // 从更新映射中移除（如果存在）
                    updateMap.remove(entity.getIdx());
                    // 如果之前标记为删除，则取消删除标记
                    deleteMap.remove(entity.getIdx());
                }
            } else if (persistType == PersistType.UPDATE) {
                if (entity == null) {
                    return;
                }
                // 只更新缓存
                if (entity.getIdx() != null) {
                    cacheMap.put(entity.getIdx(), entity);
                    // 如果不在插入映射中，则添加到更新映射
                    if (!insertMap.containsKey(entity.getIdx())) {
                        updateMap.put(entity.getIdx(), entity);
                    }
                    // 如果之前标记为删除，则取消删除标记
                    deleteMap.remove(entity.getIdx());
                }
            } else if (persistType == PersistType.DELETE) {
                if (entity.getIdx() != null) {
                    // 从缓存中移除
                    T removed = cacheMap.remove(entity.getIdx());
                    // 从插入和更新映射中移除
                    insertMap.remove(entity.getIdx());
                    updateMap.remove(entity.getIdx());
                    // 标记为需要从数据库删除
                    deleteMap.put(entity.getIdx(), true);
                }
            }
        }
    }

    /**
     * 根据主键ID获取实体
     * 优先从缓存获取，缓存未命中则从数据库查询并缓存
     */
    public T get(Long idx) {
        if (idx == null) {
            return null;
        }

        // 先无锁读取
        T entity = cacheMap.get(idx + "");
        if (entity != null) {
            return entity;
        }

        // 检查是否已标记为删除
        if (deleteMap.containsKey(idx + "")) {
            return null;
        }

        // 只在需要查询数据库时才加锁
        synchronized (lock) {
            // 双重检查
            entity = cacheMap.get(idx + "");
            if (entity != null) {
                return entity;
            }

            if (deleteMap.containsKey(idx + "")) {
                return null;
            }

            // 缓存中没有，从数据库中查询
            entity = dao.selectById(idx);
            if (entity != null) {
                cacheMap.put(entity.getIdx(), entity);
            }

            return entity;
        }
    }

    /**
     * 新增实体到缓存（只操作缓存，不操作数据库）
     */
    public void add(T entity) {
        if(contains(Long.valueOf(entity.getIdx()))) {
            add(entity, PersistType.UPDATE);
        }else{
            add(entity, PersistType.INSERT);
        }
    }

    /**
     * 更新实体到缓存（只操作缓存，不操作数据库）
     */
    public void update(T entity) {
        add(entity, PersistType.UPDATE);
    }

    /**
     * 根据主键ID删除实体（只操作缓存，不操作数据库）
     */
    public boolean delete(Long id) {
        add(null, PersistType.DELETE);
        return true;
    }

    /**
     * 异步新增实体
     */
    public CompletableFuture<Void> asyncInsert(T entity) {
        return CompletableFuture.runAsync(() -> add(entity));
    }

    /**
     * 异步更新实体
     */
    public CompletableFuture<Void> asyncUpdate(T entity) {
        return CompletableFuture.runAsync(() -> update(entity));
    }

    /**
     * 异步删除实体
     */
    public CompletableFuture<Boolean> asyncDelete(Long id) {
        return CompletableFuture.supplyAsync(() -> delete(id));
    }

    /**
     * 检查缓存中是否存在指定ID的实体
     */
    public boolean contains(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        String idx = id + "";
        // 如果标记为删除，则认为不存在
        if (deleteMap.containsKey(idx)) {
            return false;
        }
        return cacheMap.containsKey(idx);
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return cacheMap.size();
    }

    /**
     * 清空缓存（不影响数据库）
     */
    public void clear() {
        cacheMap.clear();
        insertMap.clear();
        updateMap.clear();
        deleteMap.clear();
    }

    /**
     * 从数据库加载数据到缓存
     */
    public void loadFromDatabase() {
        try {
            logger.info("Loading data from database for cache: {}", getClass().getSimpleName());
            List<T> entities = dao.selectAll();

            cacheMap.clear();
            insertMap.clear();
            updateMap.clear();
            deleteMap.clear();
            for (T entity : entities) {
                if (entity.getIdx() != null) {
                    cacheMap.put(entity.getIdx(), entity);
                }
            }

            logger.info("Loaded {} entities from database", entities.size());
        } catch (Exception e) {
            logger.error("Failed to load data from database", e);
            throw e;
        }
    }

    /**
     * 同步缓存中的所有数据到数据库
     * 将cacheMap中所有数据同步到数据库，并处理删除标记
     * 优化版本：采用分批处理方式，避免一次性处理大量数据导致的性能问题
     */
    public void syncAllDataToDatabase() {
        logger.debug("Starting to sync all cache data to database, cache size: {}, insert size: {}, update size: {}, delete size: {}",
                cacheMap.size(), insertMap.size(), updateMap.size(), deleteMap.size());
        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        int errorCount = 0;

        try {
            // 处理需要删除的记录（分批处理）
            if (!deleteMap.isEmpty()) {
                List<String> deleteIds = new ArrayList<>(deleteMap.keySet());
                deleteCount = processBatchDeletes(deleteIds);
            }

            // 处理需要插入的记录（分批处理）
            if (!insertMap.isEmpty()) {
                List<T> insertEntities = new ArrayList<>(insertMap.values());
                insertCount = processBatchInserts(insertEntities);
            }

            // 处理需要更新的记录（分批处理）
            if (!updateMap.isEmpty()) {
                List<T> updateEntities = new ArrayList<>(updateMap.values());
                updateCount = processBatchUpdates(updateEntities);
            }

            // 清除已处理的标记
            insertMap.clear();
            updateMap.clear();
            deleteMap.clear();

            logger.debug("Cache sync completed: inserts={}, updates={}, deletes={}, errors={}",
                    insertCount, updateCount, deleteCount, errorCount);
        } catch (Exception e) {
            logger.error("Failed to sync all cache data to database", e);
            throw new RuntimeException("Failed to sync cache data to database", e);
        }
    }

    /**
     * 批量保存缓存中的所有数据到数据库（用于系统关闭时的完整同步）
     * 优化版本：采用分批处理方式，避免一次性处理大量数据导致的性能问题
     */
    public void saveAllToDatabase() {
        synchronized (lock) {
            int insertCount = 0;
            int updateCount = 0;
            int deleteCount = 0;
            int errorCount = 0;
            try {
                // 处理需要删除的记录（分批处理）
                if (!deleteMap.isEmpty()) {
                    List<String> deleteIds = new ArrayList<>(deleteMap.keySet());
                    deleteCount = processBatchDeletes(deleteIds);
                }

                // 处理需要插入的记录（分批处理）
                if (!insertMap.isEmpty()) {
                    List<T> insertEntities = new ArrayList<>(insertMap.values());
                    insertCount = processBatchInserts(insertEntities);
                }

                // 处理需要更新的记录（分批处理）
                if (!updateMap.isEmpty()) {
                    List<T> updateEntities = new ArrayList<>(updateMap.values());
                    updateCount = processBatchUpdates(updateEntities);
                }

                // 处理缓存中剩余的记录（未在insertMap和updateMap中的记录）
                // 这些记录可能是直接放入缓存的，需要根据ID判断是插入还是更新
                int[] remainingStats = processRemainingCacheEntities();
                errorCount = remainingStats[0];

                // 清除已处理的标记
                insertMap.clear();
                updateMap.clear();
                deleteMap.clear();

            } catch (Exception e) {
                logger.error("Failed to save all cache data to database", e);
                throw new RuntimeException("Failed to save cache data to database", e);
            }
        }

    }

    /**
     * 分批处理删除操作
     */
    private int processBatchDeletes(List<String> deleteIds) {
        int deleteCount = 0;
        int errorCount = 0;
        for (int i = 0; i < deleteIds.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, deleteIds.size());
            List<String> batch = deleteIds.subList(i, end);

            try {
                // 处理一批删除操作
                for (String idx : batch) {
                    // 如果缓存中没有该记录且标记为删除，则从数据库删除
                    if (!cacheMap.containsKey(idx)) {
                        try {
                            Long id = Long.parseLong(idx);
                            if (id > 0) {
                                dao.deleteById(id);
                                deleteCount++;
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid ID format for deletion: {}", idx);
                        }
                    }
                }
            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to delete batch of entities from database, batch size: {}", batch.size(), e);
            }
        }
        if (errorCount > 0) {
            logger.warn("Encountered {} errors during batch delete operations", errorCount);
        }
        return deleteCount;
    }

    /**
     * 分批处理插入操作
     */
    private int processBatchInserts(List<T> insertEntities) {
        int insertCount = 0;
        int errorCount = 0;

        // 分批处理，避免一次性处理大量数据
        for (int i = 0; i < insertEntities.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, insertEntities.size());
            List<T> batch = insertEntities.subList(i, end);

            try {
                // 批量插入优化
                for (T entity : batch) {
                    dao.insert(entity);
                    insertCount++;
                }
                logger.debug("Inserted batch of {} entities", batch.size());
            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to insert batch of entities to database, batch size: {}", batch.size(), e);

                // 逐个重试
                for (T entity : batch) {
                    try {
                        dao.insert(entity);
                        insertCount++;
                    } catch (Exception innerE) {
                        errorCount++;
                        logger.error("Failed to insert entity to database: {}", entity, innerE);
                    }
                }
            }
        }

        if (errorCount > 0) {
            logger.warn("Encountered {} errors during batch insert operations", errorCount);
        }
        return insertCount;
    }

    /**
     * 分批处理更新操作
     */
    private int processBatchUpdates(List<T> updateEntities) {
        int updateCount = 0;
        int errorCount = 0;
        for (int i = 0; i < updateEntities.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, updateEntities.size());
            List<T> batch = updateEntities.subList(i, end);

            try {
                // 批量更新
                for (T entity : batch) {
                    dao.update(entity);
                    updateCount++;
                }
                logger.debug("Updated batch of {} entities", batch.size());
            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to update batch of entities to database, batch size: {}", batch.size(), e);

                // 如果批量更新失败，尝试逐个更新
                for (T entity : batch) {
                    try {
                        dao.update(entity);
                        updateCount++;
                    } catch (Exception innerE) {
                        errorCount++;
                        logger.error("Failed to update entity to database: {}", entity, innerE);
                    }
                }
            }
        }
        if (errorCount > 0) {
            logger.warn("Encountered {} errors during batch update operations", errorCount);
        }
        return updateCount;
    }

    /**
     * 处理缓存中剩余的记录
     *
     * @return 返回一个包含错误计数的数组，[0]为错误计数
     */
    private int[] processRemainingCacheEntities() {
        int errorCount = 0;
        // 收集未在insertMap和updateMap中的记录
        List<T> remainingEntities = new ArrayList<>();
        for (T entity : cacheMap.values()) {
            String idx = entity.getIdx();
            // 如果记录不在insertMap和updateMap中，说明是直接放入缓存的
            if (!insertMap.containsKey(idx) && !updateMap.containsKey(idx)) {
                remainingEntities.add(entity);
            }
        }

        // 分批处理剩余记录
        for (int i = 0; i < remainingEntities.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, remainingEntities.size());
            List<T> batch = remainingEntities.subList(i, end);

            try {
                // 处理一批剩余记录
                for (T entity : batch) {
                    try {
                        processRemainingEntity(entity);
                    } catch (Exception e) {
                        errorCount++;
                        logger.error("Failed to process remaining entity: {}", entity, e);
                    }
                }
            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to process batch of remaining entities, batch size: {}", batch.size(), e);
            }
        }

        if (errorCount > 0) {
            logger.warn("Encountered {} errors during processing of remaining cache entities", errorCount);
        }
        return new int[]{errorCount};
    }

    /**
     * 处理单个剩余记录
     */
    private void processRemainingEntity(T entity) {
        String idx = entity.getIdx();
        // 检查实体是否已存在（通过ID判断）
        if (idx != null && !idx.isEmpty()) {
            try {
                // 尝试将idx转换为数字ID来判断实体是否已存在
                Long id = Long.parseLong(idx);

                if (updateMap.containsKey(id)) {
                    // 已存在的实体，执行更新
                    dao.update(entity);
                } else if (insertMap.containsKey(id)) {
                    // 新实体，执行插入
                    dao.insert(entity);
                } else if (deleteMap.containsKey(id)) {
                    // 删除实体
                    dao.deleteById(id);
                }
            } catch (NumberFormatException e) {
                // 如果idx不是数字，我们假设它是新实体
                dao.insert(entity);
            }
        } else {
            // idx为空，执行插入
            dao.insert(entity);
        }
    }

    /**
     * 异步批量保存
     */
    public CompletableFuture<Void> asyncSaveAllToDatabase() {
        return CompletableFuture.runAsync(this::saveAllToDatabase);
    }

    /**
     * 异步同步所有数据
     */
    public CompletableFuture<Void> asyncSyncAllDataToDatabase() {
        return CompletableFuture.runAsync(this::syncAllDataToDatabase);
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format("%s: size=%d, toInsert=%d, toUpdate=%d, toDelete=%d",
                getClass().getSimpleName(), size(), insertMap.size(), updateMap.size(), deleteMap.size());
    }

    public List<T> getAllCache() {
        return new ArrayList<>(cacheMap.values());
    }
}