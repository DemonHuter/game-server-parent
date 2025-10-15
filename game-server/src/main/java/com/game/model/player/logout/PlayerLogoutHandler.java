package com.game.model.player.logout;

import com.game.core.GameHandler;
import com.game.core.GameMessageHandler;
import com.game.core.MessageUtils;
import com.game.model.player.login.PlayerLoginManager;
import com.game.model.player.login.PlayerSessionManager;
import com.game.proto.login.LogoutRequest;
import com.game.proto.login.LogoutResponse;
import com.game.proto.msgid.MsgIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户登出处理器
 * 处理用户登出消息
 */
@Component
@GameHandler(msgId = MsgIdEnum.CS_player_logout_VALUE)
public class PlayerLogoutHandler extends GameMessageHandler<LogoutRequest> {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerLogoutHandler.class);
    
    // 注入用户业务逻辑管理器
    @Autowired
    private PlayerLoginManager playerLoginManager;
    
    // 注入玩家会话管理器
    @Autowired
    private PlayerSessionManager playerSessionManager;
    
    @Override
    public void handleMessage(LogoutRequest msg) throws Exception {
        logger.info("Logout request from {}: userId={}", getCtx().channel().remoteAddress(), msg.getUserId());
        
        // 调用业务逻辑管理器处理登出请求
        LogoutResponse response = playerLoginManager.handleLogout(msg);
        
        // 解绑玩家会话
        playerSessionManager.unbindPlayerSession(msg.getUserId());
        
        // 发送响应，使用从消息中获取的序列号
        MessageUtils.sendResponse(getCtx(), MsgIdEnum.SC_player_logout_VALUE, getSequence(), response.toByteString());
        
        // 关闭连接
        getCtx().close();
    }
}