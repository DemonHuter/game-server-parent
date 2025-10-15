//package com.game.model.chat;
//
//import com.game.core.MessageSender;
//import com.game.proto.GameProto;
//import com.game.proto.MessageIdProto;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// * 聊天服务类
// * 提供聊天相关的业务功能
// */
//@Service
//public class ChatService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
//
//    @Autowired
//    private MessageSender messageSender;
//
//    /**
//     * 向指定玩家发送私聊消息
//     *
//     * @param fromUserId 发送者ID
//     * @param toUserId 接收者ID
//     * @param content 消息内容
//     */
//    public void sendPrivateMessage(long fromUserId, long toUserId, String content) {
//        // 检查接收者是否在线
//        if (!messageSender.isPlayerOnline(toUserId)) {
//            logger.warn("Player is not online, cannot send private message: playerId={}", toUserId);
//            return;
//        }
//
//        // 创建私聊消息
//        GameProto.PrivateChatMessage privateMessage = GameProto.PrivateChatMessage.newBuilder()
//                .setFromUserId(fromUserId)
//                .setContent(content)
//                .setTimestamp(System.currentTimeMillis())
//                .build();
//
//        // 发送私聊消息
//        boolean success = messageSender.sendMessageToPlayer(
//                toUserId,
//                MessageIdProto.MsgIdEnum.MSG_CHAT_PRIVATE_MESSAGE_VALUE,
//                privateMessage.toByteString());
//
//        if (success) {
//            logger.info("Private message sent successfully: fromUserId={}, toUserId={}", fromUserId, toUserId);
//        } else {
//            logger.error("Failed to send private message: fromUserId={}, toUserId={}", fromUserId, toUserId);
//        }
//    }
//
//    /**
//     * 向所有在线玩家广播消息
//     *
//     * @param fromUserId 发送者ID
//     * @param content 消息内容
//     */
//    public void broadcastMessage(long fromUserId, String content) {
//        // TODO: 实现广播消息逻辑
//        // 这需要一个在线玩家列表的管理机制
//        logger.info("Broadcasting message: fromUserId={}, content={}", fromUserId, content);
//    }
//}