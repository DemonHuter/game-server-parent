package com.game.client;

import com.game.proto.GameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端Protobuf编码器
 * 协议格式：4字节(总长度)+2字节(msgId)+4字节(序列号)+data
 */
public class ClientProtobufEncoder extends MessageToByteEncoder<GameMessage> {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientProtobufEncoder.class);
    
    @Override
    protected void encode(ChannelHandlerContext ctx, GameMessage msg, ByteBuf out) throws Exception {
        try {
            // 获取protobuf数据
            byte[] data = msg.getData().toByteArray();
            
            // 计算总长度（不包括长度字段本身）
            // 2字节msgId + 4字节序列号 + data长度
            int totalLength = 2 + 4 + data.length;
            
            // 写入总长度（4字节）
            out.writeInt(totalLength);
            
            // 写入msgId（2字节）
            out.writeShort((short) msg.getMsgId());
            
            // 写入序列号（4字节）
            out.writeInt(msg.getSequence());
            
            // 写入protobuf数据
            out.writeBytes(data);
            
            logger.debug("Encoded message: msgId={}, sequence={}, totalSize={}",
                    msg.getMsgId(), msg.getSequence(), totalLength + 4);
                    
        } catch (Exception e) {
            logger.error("Failed to encode protobuf message to channel: {}", ctx.channel(), e);
            throw e;
        }
    }
}