package com.game.netty.handler;

import com.game.netty.codec.GameProtobufDecoder;
import com.game.netty.codec.GameProtobufEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 游戏通道初始化器
 */
@Component
public class GameChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    @Autowired
    private GameMessageHandler gameMessageHandler;
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 空闲状态检测 - 60秒读空闲，120秒写空闲，180秒总空闲
        pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 120, 180, TimeUnit.SECONDS));
        
        // 长度字段解码器 - 解决粘包/拆包问题
        // 根据协议格式：4字节(总长度)+2字节(msgId)+4字节(序列号)+data
        // 最大帧长度1MB，长度字段偏移0，长度字段4字节，长度调整0，初始跳过字节数0
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
                1024 * 1024, 0, 4, 0, 0));
        
        // 自定义解码器处理msgId和序列号
        pipeline.addLast("protobufDecoder", new GameProtobufDecoder());
        
        // Protobuf编码器
        pipeline.addLast("protobufEncoder", new GameProtobufEncoder());
        
        // 游戏消息处理器
        pipeline.addLast("gameMessageHandler", gameMessageHandler);
    }
}