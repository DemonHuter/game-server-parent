package com.game.model.player.login;

import com.game.dao.entity.Player;

/**
 * 玩家登录后处理接口
 * 所有需要在玩家登录后执行的逻辑都应该实现此接口
 */
public interface IPlayerLogin {
    
    /**
     * 玩家登录后执行的逻辑
     * 
     * @param player 登录的玩家对象
     */
    void login(Player player);
}