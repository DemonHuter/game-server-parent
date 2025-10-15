package com.game.core;

import com.game.proto.ErrorMessage;
import com.game.proto.GameMessage;
import com.game.proto.msgid.MsgIdEnum;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息工具类
 * 提供便捷的消息发送方法，支持单播、广播、响应等多种场景
 */
public class MessageUtils {
    
    /**
     * 发送单播消息（同步）
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     */
    public static void sendUnicast(ChannelHandlerContext ctx, int msgId, int sequence, com.google.protobuf.ByteString data) {
        GameMessage response = GameMessage.newBuilder()
                .setMsgId(msgId)
                .setSequence(sequence)
                .setData(data)
                .build();
        
        ctx.writeAndFlush(response);
    }
    
    /**
     * 发送广播消息
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     */
    public static void sendBroadcast(ChannelHandlerContext ctx, int msgId, int sequence, com.google.protobuf.ByteString data) {
        GameMessage response = GameMessage.newBuilder()
                .setMsgId(msgId)
                .setSequence(sequence)
                .setData(data)
                .build();
        
        // TODO: 实际应用中需要实现真正的广播机制，将消息发送给所有在线用户
        ctx.writeAndFlush(response);
    }
    
    /**
     * 发送响应消息
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     */
    public static void sendResponse(ChannelHandlerContext ctx, int msgId, int sequence, com.google.protobuf.ByteString data) {
        sendUnicast(ctx, msgId, sequence, data);
    }
    
    /**
     * 发送错误响应
     * 
     * @param ctx ChannelHandlerContext
     * @param sequence 序列号
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    public static void sendErrorResponse(ChannelHandlerContext ctx, int sequence, int errorCode, String errorMessage) {
        ErrorMessage error = ErrorMessage.newBuilder()
                .setErrorCode(errorCode)
                .setErrorMessage(errorMessage)
                .setDetails("")
                .build();
        
        sendResponse(ctx, MsgIdEnum.MSG_SYSTEM_ERROR_VALUE, sequence, error.toByteString());
    }
    
    /**
     * 发送成功响应
     * 
     * @param ctx ChannelHandlerContext
     * @param msgId 消息类型
     * @param sequence 序列号
     */
    public static void sendSuccessResponse(ChannelHandlerContext ctx, int msgId, int sequence) {
        ErrorMessage success = ErrorMessage.newBuilder()
                .setErrorCode(0)
                .setErrorMessage("Success")
                .setDetails("")
                .build();
        
        sendResponse(ctx, msgId, sequence, success.toByteString());
    }
}