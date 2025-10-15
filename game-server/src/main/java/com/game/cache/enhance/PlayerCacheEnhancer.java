package com.game.cache.enhance;

import com.game.cache.PlayerCache;
import com.game.dao.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player缓存增强器
 * 提供基于userId和serverIndex的快速查询功能
 */
@Component
public class PlayerCacheEnhancer implements CacheEnhancer<Player, PlayerCache> {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCacheEnhancer.class);

    @Autowired
    private PlayerCache playerCache;

    /**
     * 用户映射：userId_serverIndex -> Player
     */
    private final ConcurrentHashMap<String, Player> userMap = new ConcurrentHashMap<>();

    @Override
    public void initialize(PlayerCache cache) {
        try {
            logger.info("开始初始化Player缓存增强数据");
            
            // 获取所有缓存的玩家数据
            List<Player> players = cache.getAllCache();
            
            // 清空现有的增强数据
            userMap.clear();
            
            // 将所有玩家数据按userId_serverIndex格式加载到增强缓存中
            int count = 0;
            for (Player player : players) {
                if (player.getUserid() != null && player.getServerindex() != null) {
                    String key = generateKey(player.getUserid(), player.getServerindex());
                    userMap.put(key, player);
                    count++;
                }
            }
            
            logger.info("Player缓存增强初始化完成，共加载 {} 条数据", count);
        } catch (Exception e) {
            logger.error("初始化Player缓存增强数据失败", e);
            throw new RuntimeException("初始化Player缓存增强数据失败", e);
        }
    }

    @Override
    public String getName() {
        return "PlayerCacheEnhancer";
    }

    @Override
    public int getOrder() {
        return 10;
    }
    
    @Override
    public Class<PlayerCache> getTargetCacheClass() {
        return PlayerCache.class;
    }

    /**
     * 根据userId和serverIndex获取玩家信息
     * @param userId 用户ID
     * @param serverIndex 服务器索引
     * @return 玩家对象，如果不存在则返回null
     */
    public Player getPlayer(String userId, Integer serverIndex) {
        if (userId == null || serverIndex == null) {
            return null;
        }
        
        // 构造key
        String key = generateKey(userId, serverIndex);
        
        // 从增强缓存中获取
        return userMap.get(key);
    }

    /**
     * 生成userId和serverIndex的组合键
     * @param userId 用户ID
     * @param serverIndex 服务器索引
     * @return 组合键
     */
    private String generateKey(String userId, Integer serverIndex) {
        return userId + "_" + serverIndex;
    }

    /**
     * 添加玩家到增强缓存（同时更新主缓存）
     * @param player 玩家对象
     */
    public void addPlayer(Player player) {
        if (player == null || player.getUserid() == null || player.getServerindex() == null) {
            return;
        }
        
        // 更新主缓存
        playerCache.insert(player);
        
        // 更新增强缓存
        String key = generateKey(player.getUserid(), player.getServerindex());
        userMap.put(key, player);
    }

    /**
     * 从增强缓存中移除玩家（同时更新主缓存）
     * @param userId 用户ID
     * @param serverIndex 服务器索引
     */
    public void removePlayer(String userId, Integer serverIndex) {
        if (userId == null || serverIndex == null) {
            return;
        }
        
        // 从增强缓存中移除
        String key = generateKey(userId, serverIndex);
        Player player = userMap.remove(key);
        
        // 从主缓存中移除
        if (player != null && player.getPlayerid() != null) {
            playerCache.delete(player.getPlayerid());
        }
    }

    /**
     * 更新玩家信息（同时更新主缓存）
     * @param player 玩家对象
     */
    public void updatePlayer(Player player) {
        if (player == null || player.getUserid() == null || player.getServerindex() == null) {
            return;
        }
        
        // 更新主缓存
        playerCache.update(player);
        
        // 更新增强缓存
        String key = generateKey(player.getUserid(), player.getServerindex());
        userMap.put(key, player);
    }

    /**
     * 清空增强缓存（同时清空主缓存）
     */
    public void clear() {
        // 清空主缓存
        playerCache.clear();
        
        // 清空增强缓存
        userMap.clear();
    }

    /**
     * 获取增强缓存大小
     * @return 缓存大小
     */
    public int size() {
        return userMap.size();
    }
}