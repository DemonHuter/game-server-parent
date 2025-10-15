package com.game.netty.codec;

import com.game.proto.GameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Protobuf解码器
 * 协议格式：4字节(总长度)+2字节(msgId)+4字节(序列号)+data
 */
public class GameProtobufDecoder extends MessageToMessageDecoder<ByteBuf> {
    
    private static final Logger logger = LoggerFactory.getLogger(GameProtobufDecoder.class);
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        try {
            // 读取msgId(2字节)和序列号(4字节)
            if (msg.readableBytes() < 6) {
                logger.warn("Message too short to contain msgId and sequence");
                return;
            }
            int length = msg.readInt();
            short msgId = msg.readShort();   // 2字节msgId
            int sequence = msg.readInt(); // 4字节序列号
            
            // 读取剩余的数据作为protobuf数据
            byte[] data = new byte[msg.readableBytes()];
            msg.readBytes(data);
            
            // 构造GameMessage对象
            GameMessage gameMessage = GameMessage.newBuilder()
                    .setMsgId(msgId)
                    .setSequence(sequence)
                    .setData(com.google.protobuf.ByteString.copyFrom(data))
                    .build();
            
            out.add(gameMessage);
            
            logger.debug("Decoded message: msgId={}, sequence={}, size={}",
                    gameMessage.getMsgId(), gameMessage.getSequence(), data.length + 6);
                    
        } catch (Exception e) {
            logger.error("Failed to decode message from channel: {}", ctx.channel(), e);
            ctx.fireExceptionCaught(e);
        }
    }
}