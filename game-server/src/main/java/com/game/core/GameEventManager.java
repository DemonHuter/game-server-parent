package com.game.core;

import com.game.model.heartbeat.HeartBeatManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 游戏事件管理器
 * 用于处理组件间的事件交互，避免循环依赖
 */
@Component
public class GameEventManager implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private HeartBeatManager heartBeatManager;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 获取HeartBeatManager实例
     * 延迟初始化以避免循环依赖
     * 
     * @return HeartBeatManager实例
     */
    private HeartBeatManager getHeartBeatManager() {
        if (heartBeatManager == null) {
            heartBeatManager = applicationContext.getBean(HeartBeatManager.class);
        }
        return heartBeatManager;
    }
    
    /**
     * 处理玩家注销事件
     * 当玩家会话解绑时调用此方法
     * 
     * @param playerId 玩家ID
     */
    public void handlePlayerUnregistered(Long playerId) {
        getHeartBeatManager().unregisterPlayerHeartbeat(playerId);
    }
    
    /**
     * 处理玩家注册事件
     * 当玩家会话绑定时调用此方法
     * 
     * @param playerId 玩家ID
     */
    public void handlePlayerRegistered(Long playerId) {
        getHeartBeatManager().registerPlayerHeartbeat(playerId);
    }
}