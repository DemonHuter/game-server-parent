package com.game.model.player.create;

import com.game.core.GameHandler;
import com.game.core.GameMessageHandler;
import com.game.proto.login.CS_player_login;
import com.game.proto.msgid.MsgIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 玩家创角模块处理器
 */
@Component
@GameHandler(msgId = MsgIdEnum.CS_create_role_VALUE)
public class PlayerCreateHandler extends GameMessageHandler<CS_player_login> {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCreateHandler.class);

    // 注入用户业务逻辑管理器
    @Autowired
    private PlayerCreateManager playerCreateManager;

    @Override
    public void handleMessage(CS_player_login msg) throws Exception {
        logger.info("Login request from {}: userId={}", getCtx().channel().remoteAddress(), msg.getUserId());

        // 调用业务逻辑管理器处理登录请求
        playerCreateManager.handleCreate(getCtx(), msg, getSequence());
    }
}