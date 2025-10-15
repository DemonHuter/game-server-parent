package com.game.cache;

import com.game.dao.entity.Player;
import com.game.dao.mapper.PlayerDao;
import org.springframework.stereotype.Component;

/**
 * Player缓存类
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
@Component
public class PlayerCache extends BaseCache<Player> {

    private final PlayerDao playerdao;

    public PlayerCache(PlayerDao playerdao) {
        super(playerdao);
        this.playerdao = playerdao;
    }
}
