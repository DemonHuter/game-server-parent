package com.game.server.service;

import com.game.cache.PlayerCache;
import com.game.dao.entity.Player;
import com.game.common.constant.GameConstants;
import com.game.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 玩家服务
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private PlayerCache playerCache;
    
    /**
     * 根据ID获取玩家
     */
    public Player getPlayerById(Long playerId) {
        if (!ValidationUtils.isValidId(playerId)) {
            return null;
        }
        return playerCache.get(playerId);
    }

    /**
     * 检查玩家是否在线
     */
    public boolean isPlayerOnline(Long playerId) {
        Player player = playerCache.get(playerId);
        return player != null; // Player实体中没有isOnline字段，这里简化处理
    }
}