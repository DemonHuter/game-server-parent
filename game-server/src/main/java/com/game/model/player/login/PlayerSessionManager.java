package com.game.model.player.login;

import com.game.core.GameEventManager;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家会话管理器
 * 管理玩家ID与网络连接上下文的映射关系
 */
@Component
public class PlayerSessionManager implements ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerSessionManager.class);
    
    private ApplicationContext applicationContext;
    
    private GameEventManager gameEventManager;
    
    /** 玩家ID与网络连接上下文的映射 */
    private final ConcurrentHashMap<Long, ChannelHandlerContext> playerSessionMap = new ConcurrentHashMap<>();
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 获取GameEventManager实例
     * 延迟初始化以避免循环依赖
     * 
     * @return GameEventManager实例
     */
    private GameEventManager getGameEventManager() {
        if (gameEventManager == null) {
            gameEventManager = applicationContext.getBean(GameEventManager.class);
        }
        return gameEventManager;
    }
    
    /**
     * 绑定玩家ID与网络连接上下文
     * 
     * @param playerId 玩家ID
     * @param ctx 网络连接上下文
     */
    public void bindPlayerSession(Long playerId, ChannelHandlerContext ctx) {
        if (playerId == null || playerId <= 0 || ctx == null) {
            logger.warn("Invalid playerId or ctx for binding player session");
            return;
        }
        
        playerSessionMap.put(playerId, ctx);
        logger.debug("Bound player session: playerId={}, remoteAddress={}", playerId, ctx.channel().remoteAddress());
        
        // 通知事件管理器玩家已注册
        getGameEventManager().handlePlayerRegistered(playerId);
    }
    
    /**
     * 解绑玩家会话
     * 
     * @param playerId 玩家ID
     */
    public void unbindPlayerSession(Long playerId) {
        if (playerId == null || playerId <= 0) {
            logger.warn("Invalid playerId for unbinding player session");
            return;
        }
        
        ChannelHandlerContext removed = playerSessionMap.remove(playerId);
        if (removed != null) {
            logger.debug("Unbound player session: playerId={}", playerId);
        }
        
        // 通知事件管理器玩家已注销
        getGameEventManager().handlePlayerUnregistered(playerId);
    }
    
    /**
     * 根据玩家ID获取网络连接上下文
     * 
     * @param playerId 玩家ID
     * @return 网络连接上下文，如果不存在则返回null
     */
    public ChannelHandlerContext getPlayerContext(Long playerId) {
        if (playerId == null || playerId <= 0) {
            return null;
        }
        
        return playerSessionMap.get(playerId);
    }
    
    /**
     * 检查玩家是否在线
     * 
     * @param playerId 玩家ID
     * @return 如果玩家在线返回true，否则返回false
     */
    public boolean isPlayerOnline(Long playerId) {
        if (playerId == null || playerId <= 0) {
            return false;
        }
        
        return playerSessionMap.containsKey(playerId);
    }
    
    /**
     * 获取在线玩家数量
     * 
     * @return 在线玩家数量
     */
    public int getOnlinePlayerCount() {
        return playerSessionMap.size();
    }
    
    /**
     * 获取所有在线玩家ID
     * 
     * @return 在线玩家ID集合
     */
    public Set<Long> getOnlinePlayerIds() {
        return playerSessionMap.keySet();
    }
    
    /**
     * 清空所有玩家会话
     */
    public void clearAllSessions() {
        // 通知事件管理器所有玩家已注销
        for (Long playerId : playerSessionMap.keySet()) {
            getGameEventManager().handlePlayerUnregistered(playerId);
        }
        
        playerSessionMap.clear();
        logger.info("Cleared all player sessions");
    }
}