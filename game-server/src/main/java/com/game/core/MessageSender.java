package com.game.core;

import com.game.model.player.login.PlayerSessionManager;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 消息发送器
 * 提供通过玩家ID发送消息的功能
 */
@Component
public class MessageSender implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private ApplicationContext applicationContext;

    private PlayerSessionManager playerSessionManager;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取PlayerSessionManager实例
     * 延迟初始化以避免循环依赖
     *
     * @return PlayerSessionManager实例
     */
    private PlayerSessionManager getPlayerSessionManager() {
        if (playerSessionManager == null) {
            playerSessionManager = applicationContext.getBean(PlayerSessionManager.class);
        }
        return playerSessionManager;
    }

    /**
     * 向指定玩家发送消息
     *
     * @param playerId 玩家ID
     * @param msgId    消息ID
     * @param data     消息数据
     * @return 发送是否成功
     */
    public boolean sendMessageToPlayer(long playerId, int msgId, com.google.protobuf.GeneratedMessageV3.Builder data) {
        return sendMessageToPlayer(playerId, msgId, 0, data);
    }

    public boolean sendMessageToPlayer(long playerId, int msgId, int sequence, com.google.protobuf.GeneratedMessageV3.Builder data) {
        // 获取玩家的网络连接上下文
        ChannelHandlerContext ctx = getPlayerSessionManager().getPlayerContext(playerId);

        if (ctx == null) {
            logger.warn("Player context not found for playerId: {}", playerId);
            return false;
        }

        // 发送消息
        AsyncMessageUtils.sendPlayerMsgAsync(ctx, msgId, sequence, data.build().toByteString());
//        MessageUtils.sendResponse(ctx, msgId, sequence, data.build().toByteString());
        return true;
    }

    /**
     * 检查玩家是否在线
     *
     * @param playerId 玩家ID
     * @return 如果玩家在线返回true，否则返回false
     */
    public boolean isPlayerOnline(long playerId) {
        return getPlayerSessionManager().isPlayerOnline(playerId);
    }

    /**
     * 获取玩家的网络连接上下文
     *
     * @param playerId 玩家ID
     * @return 网络连接上下文，如果不存在则返回null
     */
    public ChannelHandlerContext getPlayerContext(long playerId) {
        return getPlayerSessionManager().getPlayerContext(playerId);
    }
}