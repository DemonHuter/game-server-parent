package com.game.core;

import com.game.proto.GameMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

/**
 * 异步消息工具类
 * 提供异步消息发送功能，避免阻塞Netty IO线程
 */
@Component
public class AsyncMessageUtils {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMessageUtils.class);
    
    // 注入玩家消息处理线程池
    @Autowired
    @Qualifier("playerMessageExecutor")
    private ExecutorService playerMessageExecutor;
    
    // 注入系统消息处理线程池
    @Autowired
    @Qualifier("systemMessageExecutor")
    private ExecutorService systemMessageExecutor;
    
    private static ExecutorService playerExecutor;
    private static ExecutorService systemExecutor;

    @PostConstruct
    public void init() {
        playerExecutor = playerMessageExecutor;
        systemExecutor = systemMessageExecutor;
    }
    
    /**
     * 获取玩家消息处理线程池
     * 
     * @return 玩家消息处理线程池
     */
    public ExecutorService getPlayerMessageExecutor() {
        return playerMessageExecutor;
    }
    
    /**
     * 获取系统消息处理线程池
     * 
     * @return 系统消息处理线程池
     */
    public ExecutorService getSystemMessageExecutor() {
        return systemMessageExecutor;
    }
    
    /**
     * 使用玩家消息线程池异步发送单播消息
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     */
    public static void sendPlayerMsgAsync(ChannelHandlerContext ctx, int msgId, int sequence,
                                       com.google.protobuf.ByteString data) {
        sendUnicastAsync(ctx, msgId, sequence, data, playerExecutor);
    }
    
    /**
     * 使用指定线程池异步发送单播消息
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     * @param executor 执行线程池
     */
    private static void sendUnicastAsync(ChannelHandlerContext ctx, int msgId, int sequence,
                                       com.google.protobuf.ByteString data, ExecutorService executor) {
        executor.submit(() -> {
            GameMessage response = GameMessage.newBuilder()
                    .setMsgId(msgId)
                    .setSequence(sequence)
                    .setData(data)
                    .build();
            ctx.writeAndFlush(response);
        });
    }
    
    /**
     * 使用系统消息线程池异步发送单播消息
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     */
    public static void sendSystemMsgAsync(ChannelHandlerContext ctx, int msgId, int sequence,
                                             com.google.protobuf.ByteString data) {
        sendUnicastAsync(ctx, msgId, sequence, data, systemExecutor);
    }
}