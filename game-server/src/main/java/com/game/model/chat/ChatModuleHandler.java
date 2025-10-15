//package com.game.model.chat;
//
//import com.game.core.GameHandler;
//import com.game.core.GameMessageHandler;
//import com.game.core.MessageUtils;
//import com.game.model.chat.ChatManager.ChatResult;
//import com.game.proto.GameProto;
//import com.game.proto.MessageIdProto;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * 聊天模块处理器
// * 处理聊天相关的消息
// */
//@Component
//@GameHandler(msgId = MessageIdProto.MsgIdEnum.MSG_CHAT_MESSAGE_REQUEST_VALUE)
//public class ChatModuleHandler extends GameMessageHandler<GameProto.ChatRequest> {
//
//    private static final Logger logger = LoggerFactory.getLogger(ChatModuleHandler.class);
//
//    // 注入聊天业务逻辑管理器
//    @Autowired
//    private ChatManager chatManager;
//
//    @Override
//    public void handleMessage(GameProto.ChatRequest msg) throws Exception {
//        logger.info("Chat request from {}: content={}", getCtx().channel().remoteAddress(), msg.getContent());
//
//        // 调用业务逻辑管理器处理聊天请求
//        ChatResult result = chatManager.handleChat(msg);
//
//        // 发送聊天响应，使用从消息中获取的序列号
//        MessageUtils.sendResponse(getCtx(), MessageIdProto.MsgIdEnum.MSG_CHAT_MESSAGE_RESPONSE_VALUE, getSequence(), result.getResponse().toByteString());
//
//        // 广播聊天消息，使用从消息中获取的序列号
//        MessageUtils.sendResponse(getCtx(), MessageIdProto.MsgIdEnum.MSG_CHAT_BROADCAST_VALUE, getSequence(), result.getBroadcast().toByteString());
//    }
//}