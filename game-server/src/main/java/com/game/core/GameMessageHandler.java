package com.game.core;

import com.game.proto.GameMessage;
import com.game.proto.msgid.MsgIdEnum;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;
import io.netty.channel.ChannelHandlerContext;

/**
 * 游戏消息处理器抽象类
 * @param <T> 处理的消息类型
 */
public abstract class GameMessageHandler<T extends GeneratedMessageV3> {
    
    // 存储ChannelHandlerContext的属性
    protected ChannelHandlerContext ctx;
    
    // 存储原始游戏消息
    private GameMessage rawMessage;
    
    /**
     * 构造函数，初始化消息解析器
     */
    public GameMessageHandler() {
        // 构造函数中不进行初始化，避免子类还未完全初始化时就调用
    }
    
    /**
     * 处理游戏消息
     * 
     * @param msg 游戏消息
     * @throws Exception 处理异常
     */
    public abstract void handleMessage(T msg) throws Exception;
    
    /**
     * 处理原始游戏消息
     * 
     * @param msg 原始游戏消息
     * @throws Exception 处理异常
     */
    public void handleRawMessage(GameMessage msg) throws Exception {
        // 保存原始消息引用
        this.rawMessage = msg;
        
        // 确保解析器已初始化
        Parser<T> invoke = (Parser<T>) GameHandlerManager.getParser(Short.valueOf(msg.getMsgId() + ""));
        if(invoke == null) {
            return;
        }
        T parsedMsg = invoke.parseFrom(msg.getData());
        handleMessage(parsedMsg);
    }
    
    /**
     * 获取当前消息的序列号
     * 
     * @return 消息序列号
     */
    protected int getSequence() {
        if (rawMessage != null) {
            return rawMessage.getSequence();
        }
        return 0; // 默认返回0
    }
    
    /**
     * 获取当前通道的上下文
     * 
     * @return ChannelHandlerContext
     */
    protected ChannelHandlerContext getCtx() {
        return ctx;
    }
    
    /**
     * 设置ChannelHandlerContext
     * 
     * @param ctx ChannelHandlerContext
     */
    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
    
    /**
     * 发送响应消息
     * 
     * @param msgId 消息类型
     * @param sequence 序列号
     * @param data 消息数据
     */
    protected void sendResponse(int msgId, int sequence, com.google.protobuf.ByteString data) {
        MessageUtils.sendResponse(ctx, msgId, sequence, data);
    }
    
    /**
     * 发送错误响应
     * 
     * @param sequence 序列号
     * @param errorMessage 错误信息
     */
    protected void sendErrorResponse(int sequence, String errorMessage) {
        MessageUtils.sendErrorResponse(ctx, sequence, MsgIdEnum.MSG_SYSTEM_ERROR_VALUE, errorMessage);
    }
}