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

        // 首先添加协议选择器
        pipeline.addLast("protocolChooser", new SocketChooseHandler());
    }
}