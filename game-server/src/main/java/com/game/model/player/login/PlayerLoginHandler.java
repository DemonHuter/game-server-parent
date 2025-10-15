package com.game.model.player.login;

import com.game.core.GameHandler;
import com.game.core.GameMessageHandler;
import com.game.proto.login.CS_player_login;
import com.game.proto.msgid.MsgIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户模块处理器
 * 处理用户相关的消息，如登录、登出、信息查询等
 */
@Component
@GameHandler(msgId = MsgIdEnum.CS_player_login_VALUE)
public class PlayerLoginHandler extends GameMessageHandler<CS_player_login> {

    private static final Logger logger = LoggerFactory.getLogger(PlayerLoginHandler.class);

    // 注入用户业务逻辑管理器
    @Autowired
    private PlayerLoginManager playerLoginManager;

    @Override
    public void handleMessage(CS_player_login msg) throws Exception {
        logger.info("Login request from {}: userId={}", getCtx().channel().remoteAddress(), msg.getUserId());

        // 调用业务逻辑管理器处理登录请求
        playerLoginManager.handleLogin(getCtx(), msg, getSequence());
    }
}