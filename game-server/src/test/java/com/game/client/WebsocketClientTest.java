package com.game.client;

import com.game.proto.GameMessage;
import com.game.proto.HeartbeatMessage;
import com.game.proto.Test_Req;
import com.game.proto.Test_Res;
import com.game.proto.msgid.MsgIdEnum;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * WebSocket客户端测试
 */
public class WebsocketClientTest {
    
    @Test
    public void testWebSocketConnection() throws URISyntaxException, InterruptedException {
        // 创建WebSocket客户端
        WebsocketGameClient client = new WebsocketGameClient("ws://localhost:9998/ws");
        
        // 连接到服务器
        client.connect();
        
        // 等待连接建立
        Thread.sleep(2000);
        
        // 发送心跳消息
        client.sendHeartbeat();

        
        // 等待响应
        Thread.sleep(5000);
        
        // 关闭连接
        client.close();
        
        System.out.println("WebSocket client test completed");
    }

    /**
     * 参考EnhancedGameClient编写的增强版WebSocket测试方法
     */
    @Test
    public void testEnhancedWebSocketConnection() throws URISyntaxException, InterruptedException {
        // 创建WebSocket客户端
        WebsocketGameClient client = new WebsocketGameClient("ws://localhost:9998/ws");
        
        // 连接到服务器
        client.connect();
        
        // 等待连接建立
        Thread.sleep(2000);
        
        // 发送测试请求消息
        client.sendTestRequest();
        
        // 等待响应
        Thread.sleep(3000);
        
        // 发送心跳消息
        client.sendHeartbeat();
        
        // 等待响应
        Thread.sleep(3000);
        
        // 发送登录请求测试
        client.sendLoginRequest("testuser", 0);
        
        // 等待响应
        Thread.sleep(3000);
        
        // 发送聊天请求测试
        client.sendChatRequest(1, "Hello from WebSocket client!", 0, 0);
        
        // 等待响应
        Thread.sleep(3000);
        
        // 关闭连接
        client.close();
        
        System.out.println("Enhanced WebSocket client test completed");
    }

    /**
     * 综合测试方法，包含多种消息类型的测试
     */
    @Test
    public void testComprehensiveWebSocketConnection() throws URISyntaxException, InterruptedException {
        // 创建WebSocket客户端
        WebsocketGameClient client = new WebsocketGameClient("ws://localhost:9998/ws");
        
        // 连接到服务器
        client.connect();
        
        // 等待连接建立
        Thread.sleep(2000);
        
        System.out.println("=== 开始综合WebSocket测试 ===");
        
        // 1. 发送多个测试请求
        for (int i = 1; i <= 3; i++) {
            client.sendTestRequest("Test message #" + i);
            Thread.sleep(1000);
        }
        
        // 2. 发送心跳消息
        client.sendHeartbeat();
        Thread.sleep(2000);
        
        // 3. 发送不同类型的聊天消息
        client.sendChatRequest(1, "World chat message", 0, 0);  // 世界聊天
        Thread.sleep(1000);
        client.sendChatRequest(2, "Private chat message", 123, 0);  // 私聊
        Thread.sleep(1000);
        client.sendChatRequest(3, "Guild chat message", 0, 456);  // 公会聊天
        Thread.sleep(2000);
        
        // 4. 发送登录请求
        client.sendLoginRequest("comprehensive_test_user", 1);
        Thread.sleep(2000);
        
        // 5. 发送多个心跳消息测试连接稳定性
        for (int i = 1; i <= 5; i++) {
            client.sendHeartbeat("Heartbeat #" + i);
            Thread.sleep(500);
        }
        
        // 等待一段时间接收所有响应
        Thread.sleep(3000);
        
        // 关闭连接
        client.close();
        
        System.out.println("=== 综合WebSocket测试完成 ===");
    }

    /**
     * 运行所有WebSocket测试
     */
    @Test
    public void testAllWebSocketConnections() throws URISyntaxException, InterruptedException {
        System.out.println("=== 开始运行所有WebSocket测试 ===");
        
        // 运行基础测试
        System.out.println("--- 运行基础测试 ---");
        testWebSocketConnection();
        Thread.sleep(2000); // 等待连接完全关闭
        
        // 运行增强测试
        System.out.println("--- 运行增强测试 ---");
        testEnhancedWebSocketConnection();
        Thread.sleep(2000); // 等待连接完全关闭
        
        // 运行综合测试
        System.out.println("--- 运行综合测试 ---");
        testComprehensiveWebSocketConnection();
        
        System.out.println("=== 所有WebSocket测试完成 ===");
    }

    public static class WebsocketGameClient extends WebSocketClient {

        private static final Logger logger = LoggerFactory.getLogger(WebsocketGameClient.class);

        // 消息序列号
        private int sequence = 0;

        public WebsocketGameClient(String serverUri) throws URISyntaxException {
            super(new URI(serverUri));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            logger.info("Connected to WebSocket server");
        }

        @Override
        public void onMessage(String message) {
            logger.info("Received text message: {}", message);
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            try {
                // 先解码字节流为GameMessage对象
                GameMessage gameMessage = decodeMessage(bytes);
                logger.info("Received GameMessage: msgId={}, sequence={}",
                        gameMessage.getMsgId(), gameMessage.getSequence());

                // 根据消息类型处理不同的响应
                handleGameMessage(gameMessage);
            } catch (Exception e) {
                logger.error("Failed to parse GameMessage", e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            logger.info("Connection closed: code={}, reason={}, remote={}", code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            logger.error("WebSocket error", ex);
        }

        /**
         * 处理游戏消息
         */
        private void handleGameMessage(GameMessage msg) {
            // 打印消息基本信息
            logger.info("Received message with msgId={}, sequence={}, dataLength={}",
                    msg.getMsgId(), msg.getSequence(), msg.getData().size());

            // 尝试解析不同类型的消息内容
            try {
                if (msg.getMsgId() == MsgIdEnum.MSG_SYSTEM_HEARTBEAT_RES_VALUE) {
                    // 心跳响应消息
                    HeartbeatMessage heartbeat = HeartbeatMessage.parseFrom(msg.getData());
                    logger.info("Heartbeat response: timestamp={}, message={}",
                            heartbeat.getTimestamp(), heartbeat.getMessage());
                } else if (msg.getMsgId() == MsgIdEnum.SC_test_VALUE) {
                    // Test_Req响应消息
                    Test_Res testRes = Test_Res.parseFrom(msg.getData());
                    logger.info("Test_Req response: {}", testRes.getRes());
                } else if (msg.getMsgId() == MsgIdEnum.MSG_SYSTEM_ERROR_VALUE) {
                    // 错误消息
                    logger.info("Error message received");
                } else {
                    // 其他消息类型，以十六进制格式打印数据
                    byte[] data = msg.getData().toByteArray();
                    StringBuilder hexString = new StringBuilder();
                    for (int i = 0; i < Math.min(data.length, 100); i++) {
                        hexString.append(String.format("%02X ", data[i]));
                    }
                    if (data.length > 100) {
                        hexString.append("...");
                    }
                    logger.info("Message data (hex): {}", hexString.toString().trim());
                }
            } catch (Exception e) {
                logger.warn("Failed to parse message data: {}", e.getMessage());
                // 以十六进制格式打印数据
                byte[] data = msg.getData().toByteArray();
                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < Math.min(data.length, 100); i++) {
                    hexString.append(String.format("%02X ", data[i]));
                }
                if (data.length > 100) {
                    hexString.append("...");
                }
                logger.info("Message data (hex): {}", hexString.toString().trim());
            }
        }

        /**
         * 发送Test_Req消息
         */
        public void sendHeartbeat() {
            sendHeartbeat("Hello, this is a test request!");
        }

        /**
         * 发送带自定义消息的Test_Req消息
         */
        public void sendHeartbeat(String message) {
            try {
                Test_Req testReq = Test_Req.newBuilder()
                        .setReq(message)
                        .build();

                GameMessage gameMessage = GameMessage.newBuilder()
                        .setMsgId(MsgIdEnum.CS_test_VALUE)
                        .setSequence(++sequence)
                        .setData(testReq.toByteString())
                        .build();

                send(formatMessage(gameMessage));
                logger.info("Sent Test_Req message: {}", testReq.getReq());
            } catch (Exception e) {
                logger.error("Failed to send Test_Req message", e);
            }
        }

        /**
         * 发送GameMessage
         */
        public void sendGameMessage(GameMessage message) {
            try {
                send(formatMessage(message));
                logger.info("Sent GameMessage: msgId={}, sequence={}",
                        message.getMsgId(), message.getSequence());
            } catch (Exception e) {
                logger.error("Failed to send GameMessage", e);
            }
        }

        /**
         * 发送测试请求消息（参考EnhancedGameClient）
         */
        public void sendTestRequest() {
            sendTestRequest("Enhanced test request from WebSocket client");
        }

        /**
         * 发送带自定义消息的测试请求
         */
        public void sendTestRequest(String message) {
            try {
                Test_Req testReq = Test_Req.newBuilder()
                        .setReq(message)
                        .build();

                GameMessage gameMessage = GameMessage.newBuilder()
                        .setMsgId(MsgIdEnum.CS_test_VALUE)
                        .setSequence(++sequence)
                        .setData(testReq.toByteString())
                        .build();

                send(formatMessage(gameMessage));
                logger.info("Sent enhanced Test_Req message: {}", testReq.getReq());
            } catch (Exception e) {
                logger.error("Failed to send enhanced Test_Req message", e);
            }
        }

        /**
         * 发送登录请求（参考EnhancedGameClient）
         */
        public void sendLoginRequest(String userId, int serverIndex) {
            try {
                // 注意：这里需要导入相应的protobuf类
                // 由于这是一个测试类，我们简化实现
                Test_Req loginRequest = Test_Req.newBuilder()
                        .setReq("Login request for user: " + userId + ", server: " + serverIndex)
                        .build();

                GameMessage gameMessage = GameMessage.newBuilder()
                        .setMsgId(MsgIdEnum.CS_player_login_VALUE)
                        .setSequence(++sequence)
                        .setData(loginRequest.toByteString())
                        .build();

                send(formatMessage(gameMessage));
                logger.info("Sent login request for user: {}, serverIndex: {}", userId, serverIndex);
            } catch (Exception e) {
                logger.error("Failed to send login request", e);
            }
        }

        /**
         * 发送聊天请求（参考EnhancedGameClient）
         */
        public void sendChatRequest(int chatType, String message, long targetUserId, long guildId) {
            try {
                Test_Req chatRequest = Test_Req.newBuilder()
                        .setReq("Chat request - type: " + chatType + ", message: " + message + 
                               ", targetUserId: " + targetUserId + ", guildId: " + guildId)
                        .build();

                GameMessage gameMessage = GameMessage.newBuilder()
                        .setMsgId(MsgIdEnum.MSG_CHAT_MESSAGE_REQUEST_VALUE)
                        .setSequence(++sequence)
                        .setData(chatRequest.toByteString())
                        .build();

                send(formatMessage(gameMessage));
                logger.info("Sent chat request: type={}, message={}, targetUserId={}, guildId={}", 
                           chatType, message, targetUserId, guildId);
            } catch (Exception e) {
                logger.error("Failed to send chat request", e);
            }
        }

        /**
         * 格式化消息以匹配服务端协议
         * 协议格式：4字节(总长度)+2字节(msgId)+4字节(序列号)+data
         */
        private byte[] formatMessage(GameMessage message) throws IOException {
            // 获取protobuf数据
            byte[] data = message.getData().toByteArray();

            // 计算总长度（不包括长度字段本身）
            // 2字节msgId + 4字节序列号 + data长度
            int totalLength = 2 + 4 + data.length;

            // 构造符合服务端协议的消息格式
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 写入总长度（4字节）
            baos.write((totalLength >> 24) & 0xFF);
            baos.write((totalLength >> 16) & 0xFF);
            baos.write((totalLength >> 8) & 0xFF);
            baos.write(totalLength & 0xFF);

            // 写入msgId（2字节）
            short msgId = (short) message.getMsgId();
            baos.write((msgId >> 8) & 0xFF);
            baos.write(msgId & 0xFF);

            // 写入序列号（4字节）
            int sequence = message.getSequence();
            baos.write((sequence >> 24) & 0xFF);
            baos.write((sequence >> 16) & 0xFF);
            baos.write((sequence >> 8) & 0xFF);
            baos.write(sequence & 0xFF);

            // 写入protobuf数据
            baos.write(data);

            return baos.toByteArray();
        }

        /**
         * 解码服务端发送的消息
         * 协议格式：4字节(总长度)+2字节(msgId)+4字节(序列号)+data
         */
        public GameMessage decodeMessage(ByteBuffer bytes) throws IOException {
            // 读取总长度（4字节）
            int totalLength = bytes.getInt();

            // 读取msgId（2字节）
            short msgId = bytes.getShort();

            // 读取序列号（4字节）
            int sequence = bytes.getInt();

            // 读取剩余的数据作为protobuf数据
            byte[] data = new byte[bytes.remaining()];
            bytes.get(data);

            // 构造GameMessage对象
            return GameMessage.newBuilder()
                    .setMsgId(msgId)
                    .setSequence(sequence)
                    .setData(com.google.protobuf.ByteString.copyFrom(data))
                    .build();
        }
    }
}