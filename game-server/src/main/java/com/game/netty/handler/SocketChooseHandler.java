package com.game.netty.handler;

import com.game.common.util.SpringUtils;
import com.game.netty.codec.GameProtobufDecoder;
import com.game.netty.codec.GameProtobufEncoder;
import com.game.netty.codec.SimpleWebSocketFrameConverter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Spring WebSocket游戏处理器
 * 用于处理WebSocket连接和消息，并与现有的TCP消息处理逻辑集成
 */
@Component
public class SocketChooseHandler extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(SocketChooseHandler.class);
    private static final String WEBSOCKET_PREFIX = "GET /";
    private static final int MIN_LENGTH_FOR_DETECTION = WEBSOCKET_PREFIX.length(); // 应该是5
    private static final int MAX_LENGTH_FOR_CHECK = 23;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果可读字节数不足，等待更多数据
        if (in.readableBytes() < MIN_LENGTH_FOR_DETECTION) {
            return;
        }

        String beginning = getBufStart(in);

        if (beginning.startsWith(WEBSOCKET_PREFIX)) {
            // WebSocket连接
            configureWebSocketPipeline(ctx);
        } else {
            // TCP连接
            configureTcpPipeline(ctx);
        }

        // 移除协议选择器自身
        ctx.pipeline().remove(this);

        // 将当前读取的数据传递给下一个处理器
        if (in.readableBytes() > 0) {
            out.add(in.readBytes(in.readableBytes()));
        }
    }

    private String getBufStart(ByteBuf in) {
        int length = Math.min(in.readableBytes(), MAX_LENGTH_FOR_CHECK);
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        in.resetReaderIndex();
        return new String(content, StandardCharsets.US_ASCII);
    }

    private void configureTcpPipeline(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();

        addCommonPipeline(pipeline);

        System.out.println("TCP协议连接建立");
    }

    private void configureWebSocketPipeline(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();

        // 添加WebSocket协议相关的处理器
        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(256 * 1024));
        pipeline.addLast("websocket-handler", new WebSocketServerProtocolHandler("/ws", null, true));

        // 由于消息格式相同，可以使用简化的WebSocket帧转换器
        pipeline.addLast("websocket-frame-converter", new SimpleWebSocketFrameConverter());

        // 添加共享的业务处理器
        addCommonPipeline(pipeline);

        System.out.println("WebSocket协议连接建立11");
    }

    private void addCommonPipeline(ChannelPipeline pipeline) {
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
        pipeline.addLast("gameMessageHandler", SpringUtils.getBean(GameMessageHandler.class));
    }
}