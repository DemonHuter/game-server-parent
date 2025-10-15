package com.game.model.counter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.cache.CounterCache;
import com.game.common.constant.PersistType;
import com.game.dao.entity.Counter;
import com.game.model.CommonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计数器管理器
 * 处理玩家计数器和全局计数器的添加、修改、删除操作
 * 只操作内存数据，不操作数据库
 * <p>
 * 注意：playerId=-1 表示全局计数器，所有玩家共享
 */
@Service
public class CounterManager extends CommonManager {

    private static final Logger logger = LoggerFactory.getLogger(CounterManager.class);
    private static final long GLOBAL_ID = -1L;
    private final CounterCache counterCache;

    public CounterManager(CounterCache counterCache) {
        this.counterCache = counterCache;
    }

    /**
     * 设置玩家计数器值（增量添加key）
     *
     * @param playerId 玩家ID，playerId=-1 表示全局计数器，所有玩家共享
     * @param key      计数器键
     * @param value    计数器值
     * @return 是否设置成功
     */
    public void setPlayerCounter(Long playerId, String key, long value) {
        try {
            // 获取现有的计数器数据
            Map<String, Long> counterData = getPlayerCounterData(playerId);
            // 增量添加key，而不是覆盖整个map
            counterData.put(key, value);

            // 更新counterCache，以便定时同步到数据库
            updateCounterCache(playerId, counterData);

        } catch (Exception e) {
            logger.error("Failed to set player counter for playerId: {}, key: {}, value: {}", playerId, key, value, e);
        }
    }

    /**
     * 设置全局计数器值（增量添加key）
     *
     * @param key   计数器键
     * @param value 计数器值
     */
    public void setGlobalCounter(String key, long value) {
        try {
            // 获取现有的计数器数据
            Map<String, Long> counterData = getPlayerCounterData(GLOBAL_ID);
            // 增量添加key，而不是覆盖整个map
            counterData.put(key, value);

            // 更新counterCache，以便定时同步到数据库
            updateCounterCache(GLOBAL_ID, counterData); // 全局计数器使用-1作为playerId

        } catch (Exception e) {
            logger.error("Failed to set global counter for key: {}, value: {}", key, value, e);
        }
    }

    /**
     * 获取玩家计数器值
     *
     * @param playerId 玩家ID，playerId=-1 表示全局计数器，所有玩家共享
     * @param key      计数器键
     * @return 计数器值，如果不存在返回0
     */
    public long getPlayerCounterValue(Long playerId, String key) {
        try {
            Map<String, Long> counterData = getPlayerCounterData(playerId);
            return counterData.getOrDefault(key, 0L);
        } catch (Exception e) {
            logger.error("Failed to get player counter value for playerId: {}, key: {}", playerId, key, e);
            return 0;
        }
    }

    /**
     * 获取全局计数器值
     *
     * @param key 计数器键
     * @return 计数器值，如果不存在返回0
     */
    public long getGlobalCounterValue(String key) {
        try {
            Map<String, Long> counterData = getPlayerCounterData(GLOBAL_ID);
            return counterData.getOrDefault(key, 0L);
        } catch (Exception e) {
            logger.error("Failed to get global counter value for key: {}", key, e);
            return 0;
        }
    }

    /**
     * 删除玩家计数器中的指定key
     *
     * @param playerId 玩家ID，playerId=-1 表示全局计数器，所有玩家共享
     * @param key      要删除的计数器键
     * @return 是否删除成功
     */
    public boolean removePlayerCounterKey(Long playerId, String key) {
        try {
            // 获取现有的计数器数据
            Map<String, Long> counterData = getPlayerCounterData(playerId);
            
            // 删除指定的key
            Long removedValue = counterData.remove(key);
            
            // 如果key存在则更新缓存
            if (removedValue != null) {
                // 更新counterCache，以便定时同步到数据库
                updateCounterCache(playerId, counterData);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Failed to remove player counter key for playerId: {}, key: {}", playerId, key, e);
            return false;
        }
    }

    /**
     * 删除全局计数器中的指定key
     *
     * @param key 要删除的计数器键
     * @return 是否删除成功
     */
    public boolean removeGlobalCounterKey(String key) {
        try {
            // 获取现有的计数器数据
            Map<String, Long> counterData = getPlayerCounterData(GLOBAL_ID);
            
            // 删除指定的key
            Long removedValue = counterData.remove(key);
            
            // 如果key存在则更新缓存
            if (removedValue != null) {
                // 更新counterCache，以便定时同步到数据库
                updateCounterCache(GLOBAL_ID, counterData);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Failed to remove global counter key for key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取玩家计数器数据
     *
     * @param playerId 玩家ID，-1表示全局计数器
     * @return 计数器数据Map
     */
    private Map<String, Long> getPlayerCounterData(Long playerId) {
        try {
            Counter counter = counterCache.get(playerId);
            if (counter != null && counter.getCounterdata() != null) {
                JSONObject jsonObject = JSON.parseObject(counter.getCounterdata());
                Map<String, Long> counterMap = new ConcurrentHashMap<>();
                for (String key : jsonObject.keySet()) {
                    counterMap.put(key, jsonObject.getLongValue(key));
                }
                return counterMap;
            }
        } catch (Exception e) {
            logger.error("Failed to get player counter data for playerId: {}", playerId, e);
        }
        return new ConcurrentHashMap<>();
    }

    /**
     * 更新counterCache中的计数器数据
     *
     * @param playerId    玩家ID，-1表示全局计数器
     * @param counterData 计数器数据
     */
    private void updateCounterCache(Long playerId, Map<String, Long> counterData) {
        try {
            // 尝试从缓存中获取现有的Counter对象
            Counter counter = counterCache.get(playerId);

            // 如果不存在，则创建新的Counter对象
            PersistType persistType = PersistType.UPDATE;
            if (counter == null) {
                counter = new Counter();
                counter.setPlayerid(playerId);
                counter.setCreatetime(System.currentTimeMillis());
                persistType = PersistType.INSERT;
            }

            // 更新时间戳
            counter.setUpdatetime(System.currentTimeMillis());

            // 将计数器数据转换为JSON格式
            String counterDataJson = JSON.toJSONString(counterData);
            counter.setCounterdata(counterDataJson);

            // 更新counterCache
            counterCache.add(counter, persistType);
        } catch (Exception e) {
            logger.error("Failed to update counter cache for playerId: {}", playerId, e);
        }
    }
}