package com.game.model.heartbeat;

import com.game.core.MessageSender;
import com.game.proto.HeartbeatMessage;
import com.game.proto.msgid.MsgIdEnum;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 心跳业务逻辑管理器
 * 处理与心跳相关的业务逻辑
 */
@Service
public class HeartBeatManager {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatManager.class);
    // 心跳超时时间（2分钟）
    private static final long HEARTBEAT_TIMEOUT = 2 * 60 * 1000; // 2分钟
    // 记录每个玩家的心跳计数
    private final ConcurrentHashMap<Long, AtomicLong> playerHeartbeatCount = new ConcurrentHashMap<>();

    // 记录每个玩家的最后心跳时间
    private final ConcurrentHashMap<Long, Long> playerLastHeartbeatTime = new ConcurrentHashMap<>();
    @Autowired
    private MessageSender messageSender;

    /**
     * 处理心跳请求并生成响应
     *
     * @param playerId 玩家ID
     * @param request  心跳请求消息
     * @param sequence  序列号
     * @return 心跳响应消息
     */
    public void handleHeartbeat(Long playerId, HeartbeatMessage request, int sequence) {
        logger.debug("Processing heartbeat request from player {}: {}", playerId, request.getMessage());

        // 更新玩家的最后心跳时间
        playerLastHeartbeatTime.put(playerId, System.currentTimeMillis());

        // 创建心跳响应
        HeartbeatMessage response = HeartbeatMessage.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setMessage("pong")
                .build();

        logger.debug("Heartbeat response created for player {}: {}", playerId, response.getMessage());
        messageSender.sendMessageToPlayer(playerId, MsgIdEnum.MSG_SYSTEM_HEARTBEAT_RES_VALUE, sequence, response.toBuilder());
    }

    /**
     * 注册玩家心跳
     * 当玩家登录后调用此方法开始心跳监控
     *
     * @param playerId 玩家ID
     */
    public void registerPlayerHeartbeat(Long playerId) {
        if (playerId == null || playerId <= 0) {
            logger.warn("Invalid playerId for heartbeat registration");
            return;
        }

        playerHeartbeatCount.putIfAbsent(playerId, new AtomicLong(0));
        playerLastHeartbeatTime.put(playerId, System.currentTimeMillis());
        logger.debug("Player heartbeat registered: playerId={}", playerId);
    }

    /**
     * 注销玩家心跳
     * 当玩家登出后调用此方法停止心跳监控
     *
     * @param playerId 玩家ID
     */
    public void unregisterPlayerHeartbeat(Long playerId) {
        if (playerId == null || playerId <= 0) {
            logger.warn("Invalid playerId for heartbeat unregistration");
            return;
        }

        playerHeartbeatCount.remove(playerId);
        playerLastHeartbeatTime.remove(playerId);
        logger.debug("Player heartbeat unregistered: playerId={}", playerId);
    }

    /**
     * 检查并断开超时的连接
     * 定期调用此方法检查所有玩家的心跳是否超时
     */
    public void checkHeartbeatTimeouts() {
        if (playerLastHeartbeatTime.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        logger.debug("Checking heartbeat timeouts for {} players", playerLastHeartbeatTime.size());

        // 检查所有玩家的心跳是否超时
        playerLastHeartbeatTime.forEach((playerId, lastHeartbeatTime) -> {
            try {
                // 检查是否超时
                if (currentTime - lastHeartbeatTime > HEARTBEAT_TIMEOUT) {
                    logger.warn("Player heartbeat timeout, disconnecting player: playerId={}", playerId);

                    // 获取玩家的网络连接上下文
                    ChannelHandlerContext ctx = messageSender.getPlayerContext(playerId);
                    if (ctx != null) {
                        // 断开连接
                        ctx.close();
                    }

                    // 注销玩家心跳
                    unregisterPlayerHeartbeat(playerId);
                }
            } catch (Exception e) {
                logger.error("Error checking heartbeat timeout for player: playerId={}", playerId, e);
            }
        });
    }

    /**
     * 获取当前注册心跳的玩家数量
     *
     * @return 玩家数量
     */
    public int getRegisteredPlayerCount() {
        return playerHeartbeatCount.size();
    }
}