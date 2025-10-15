package com.game.model.player.create;

import com.game.cache.enhance.PlayerCacheEnhancer;
import com.game.common.util.IdGenerator;
import com.game.constant.CounterConstant;
import com.game.dao.entity.Player;
import com.game.model.CommonManager;
import com.game.model.counter.CounterManager;
import com.game.model.player.login.PlayerLoginManager;
import com.game.proto.login.CS_player_login;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 创角业务逻辑管理器
 */
@Service
public class PlayerCreateManager extends CommonManager {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCreateManager.class);

    @Autowired
    private PlayerCacheEnhancer playerCacheEnhancer;

    @Autowired
    private PlayerLoginManager playerLoginManager;

    @Autowired
    private CounterManager counterManager;

    @Value("${game.serverIndex:0}")
    private int serverIndex;
    @Value("${game.createnum:0}")
    private int initCreatenum;

    /**
     * 处理用户创角请求
     *
     * @param ctx      网络连接上下文
     * @param request  登录请求消息
     * @param sequence 消息序列号
     */
    public void handleCreate(ChannelHandlerContext ctx, CS_player_login request, int sequence) {
        logger.info("Processing login request for userId: {}", request.getUserId());

        Player player = playerCacheEnhancer.getPlayer(request.getUserId(), serverIndex);
        if (player == null) {
            player = new Player();
            player.setPlayerid(IdGenerator.getInstance().generateId());
            int createnum = (int) counterManager.getGlobalCounterValue(CounterConstant.GlobalCounter.CREATE_NUM);
            if (createnum == 0) {
                createnum = initCreatenum;
            }
            counterManager.setGlobalCounter(CounterConstant.GlobalCounter.CREATE_NUM, createnum);
            player.setCreatenum(createnum);
            player.setUserid(request.getUserId());
            player.setName(request.getName());
            player.setServerindex(serverIndex);
            player.setCreatetime(System.currentTimeMillis());
            playerCacheEnhancer.addPlayer(player);
        }

        playerLoginManager.handleLogin(ctx, request, sequence);
    }
}