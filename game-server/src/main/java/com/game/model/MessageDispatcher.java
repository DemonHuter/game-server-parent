package com.game.model;

import com.game.core.GameHandlerManager;
import com.game.proto.GameMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息分发器
 * 根据消息ID将消息分发到对应的模块处理器
 */
@Component
public class MessageDispatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);
    
    @Autowired
    private GameHandlerManager gameHandlerManager;
    
    /**
     * 根据消息类型分发消息到对应的模块处理器
     */
    public void dispatch(ChannelHandlerContext ctx, GameMessage msg) {
        try {
            // 使用GameHandlerManager处理消息
            gameHandlerManager.handle(ctx, msg);
        } catch (Exception e) {
            logger.error("Error dispatching message: msgId={}, sequence={}", msg.getMsgId(), msg.getSequence(), e);
        }
    }
}