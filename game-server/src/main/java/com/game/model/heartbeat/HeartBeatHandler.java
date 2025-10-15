package com.game.model.heartbeat;

import com.game.core.GameHandler;
import com.game.core.GameMessageHandler;
import com.game.model.player.login.PlayerSessionManager;
import com.game.proto.HeartbeatMessage;
import com.game.proto.msgid.MsgIdEnum;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 系统模块处理器
 * 处理系统相关的消息，如心跳等
 */
@Component
@GameHandler(msgId = MsgIdEnum.MSG_SYSTEM_HEARTBEAT_REQ_VALUE)
public class HeartBeatHandler extends GameMessageHandler<HeartbeatMessage> {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    // 注入心跳业务逻辑管理器
    @Autowired
    private HeartBeatManager heartBeatManager;
    
    // 注入玩家会话管理器
    @Autowired
    private PlayerSessionManager playerSessionManager;

    @Override
    public void handleMessage(HeartbeatMessage msg) throws Exception {
        logger.debug("Heartbeat from {}: {}", getCtx().channel().remoteAddress(), msg.getMessage());

        // 从会话中获取真实的玩家ID
        Long playerId = findPlayerIdByContext(getCtx());
        
        if (playerId == null) {
            logger.warn("Player ID not found for heartbeat from {}", getCtx().channel().remoteAddress());
            return;
        }

        // 调用业务逻辑管理器处理心跳请求
        heartBeatManager.handleHeartbeat(playerId, msg, getSequence());
    }
    
    /**
     * 根据ChannelHandlerContext查找对应的玩家ID
     * 
     * @param ctx ChannelHandlerContext
     * @return 玩家ID，如果未找到则返回null
     */
    private Long findPlayerIdByContext(ChannelHandlerContext ctx) {
        // 遍历所有在线玩家，找到匹配的上下文
        for (Long playerId : playerSessionManager.getOnlinePlayerIds()) {
            ChannelHandlerContext playerCtx = playerSessionManager.getPlayerContext(playerId);
            if (playerCtx != null && playerCtx == ctx) {
                return playerId;
            }
        }
        return null;
    }
}