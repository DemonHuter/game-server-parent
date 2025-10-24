package com.game.netty.codec;

/**
 * @description: <TODO description class purpose>
 * @author: guosheng
 * @date: 2025/10/23 17:00
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

/**
 * 简化的WebSocket帧转换器
 * 直接处理二进制帧，无需格式转换
 */
public class SimpleWebSocketFrameConverter extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 将ByteBuf包装成BinaryWebSocketFrame
        out.add(new BinaryWebSocketFrame(msg.retain()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof BinaryWebSocketFrame) {
            // 将BinaryWebSocketFrame中的内容转换为ByteBuf
            out.add(frame.content().retain());
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
        } else {
            // 如果不是二进制帧，关闭连接
            throw new UnsupportedOperationException("不支持帧类型: " + frame.getClass().getName());
        }
    }
}
