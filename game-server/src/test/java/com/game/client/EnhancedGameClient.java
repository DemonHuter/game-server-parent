package com.game.client;

import com.game.proto.ErrorMessage;
import com.game.proto.GameMessage;
import com.game.proto.HeartbeatMessage;
import com.game.proto.login.*;
import com.game.proto.msgid.MsgIdEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * 增强版游戏客户端
 * 用于连接游戏服务器并进行各种消息测试
 */
public class EnhancedGameClient {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedGameClient.class);
    
    private final String host;
    private final int port;
    private Channel channel;
    private EventLoopGroup workerGroup;
    
    // 消息序列号
    private int sequence = 0;
    
    // 当前用户ID
    private long playerId = 0;
    private int currentServerIndex = 0;
    private ChannelHandlerContext currentCtx;
    
    public EnhancedGameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * 启动客户端
     */
    public void start() throws InterruptedException {
        workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // 长度字段解码器 - 解决粘包/拆包问题
                            // 根据协议格式：4字节(总长度)+2字节(msgId)+4字节(序列号)+data
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
                                    1024 * 1024, 0, 4, 0, 0));
                            
                            // 自定义解码器处理msgId和序列号
                            pipeline.addLast("protobufDecoder", new ClientProtobufDecoder());
                            
                            // Protobuf编码器
                            pipeline.addLast("protobufEncoder", new ClientProtobufEncoder());
                            
                            // 业务处理器
                            pipeline.addLast("clientHandler", new EnhancedClientHandler());
                        }
                    });
            
            // 连接到服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            
            logger.info("Connected to game server at {}:{}", host, port);
            
            // 启动用户输入线程
            startUserInput();
            
            // 等待连接关闭
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * 启动用户输入线程
     */
    private void startUserInput() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enhanced Game Client started. Enter commands:");
            System.out.println("=== 基础命令 ===");
            System.out.println("login <userId> <serverIndex> - 登录");
            System.out.println("create <userId> <serverIndex> <name> - 创建角色");
            System.out.println("info [userId] - 获取用户信息");
            System.out.println("update <level> <gold> <exp> - 更新用户信息");
            System.out.println("chat <type> <message> [targetUserId] [guildId] - 发送聊天消息");
            System.out.println("logout - 登出");
            System.out.println("heartbeat - 发送心跳");
            System.out.println("=== 测试命令 ===");
            System.out.println("test_login - 测试登录流程");
            System.out.println("test_chat - 测试聊天功能");
            System.out.println("stress <count> - 压力测试（发送指定数量的心跳消息）");
            System.out.println("=== 系统命令 ===");
            System.out.println("help - 显示帮助信息");
            System.out.println("exit - 退出客户端");
            
            while (true) {
                try {
                    System.out.print("> ");
                    String input = scanner.nextLine();
                    if (input == null || input.trim().isEmpty()) {
                        continue;
                    }
                    
                    String[] parts = input.trim().split(" ", 5);
                    String command = parts[0].toLowerCase();
                    
                    switch (command) {
                        case "login":
                            if (parts.length >= 3) {
                                sendLoginRequest(parts[1], Integer.parseInt(parts[2]));
                            } else {
                                System.out.println("Usage: login <userId> <serverIndex>");
                            }
                            break;
                        case "create":
                            if (parts.length >= 4) {
                                sendCreateRequest(parts[1], Integer.parseInt(parts[2]), parts[3]);
                            } else {
                                System.out.println("Usage: create <userId> <serverIndex> <name>");
                            }
                            break;
                        case "info":
                            if (parts.length >= 2) {
                                sendUserInfoRequest(Long.parseLong(parts[1]));
                            } else {
                                sendUserInfoRequest(0); // 使用当前玩家ID
                            }
                            break;
                        case "update":
                            if (parts.length >= 4) {
                                sendUserUpdateRequest(
                                    Integer.parseInt(parts[1]), 
                                    Long.parseLong(parts[2]), 
                                    Long.parseLong(parts[3])
                                );
                            } else {
                                System.out.println("Usage: update <level> <gold> <exp>");
                            }
                            break;
                        case "chat":
                            if (parts.length >= 3) {
                                int chatType = Integer.parseInt(parts[1]);
                                String message = parts[2];
                                long targetUserId = parts.length >= 4 ? Long.parseLong(parts[3]) : 0;
                                long guildId = parts.length >= 5 ? Long.parseLong(parts[4]) : 0;
                                sendChatRequest(chatType, message, targetUserId, guildId);
                            } else {
                                System.out.println("Usage: chat <type> <message> [targetUserId] [guildId]");
                            }
                            break;
                        case "logout":
                            sendLogoutRequest();
                            break;
                        case "heartbeat":
                            sendHeartbeat();
                            break;
                        case "test_login":
                            testLoginFlow();
                            break;
                        case "test_chat":
                            testChatFlow();
                            break;
                        case "stress":
                            if (parts.length >= 2) {
                                stressTest(Integer.parseInt(parts[1]));
                            } else {
                                System.out.println("Usage: stress <count>");
                            }
                            break;
                        case "help":
                            showHelp();
                            break;
                        case "exit":
                            channel.close();
                            return;
                        default:
                            System.out.println("Unknown command: " + command);
                            System.out.println("Type 'help' for available commands");
                    }
                } catch (Exception e) {
                    logger.error("Error processing user input", e);
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelp() {
        System.out.println("=== 基础命令 ===");
        System.out.println("login <userId> <serverIndex> - 登录");
        System.out.println("create <userId> <serverIndex> <name> - 创建角色");
        System.out.println("info [userId] - 获取用户信息");
        System.out.println("update <level> <gold> <exp> - 更新用户信息");
        System.out.println("chat <type> <message> [targetUserId] [guildId] - 发送聊天消息");
        System.out.println("logout - 登出");
        System.out.println("heartbeat - 发送心跳");
        System.out.println("=== 测试命令 ===");
        System.out.println("test_login - 测试登录流程");
        System.out.println("test_chat - 测试聊天功能");
        System.out.println("stress <count> - 压力测试（发送指定数量的心跳消息）");
        System.out.println("=== 系统命令 ===");
        System.out.println("help - 显示帮助信息");
        System.out.println("exit - 退出客户端");
    }
    
    /**
     * 发送登录请求
     */
    private void sendLoginRequest(String userId, int serverIndex) {
        CS_player_login loginRequest = CS_player_login.newBuilder()
                .setUserId(userId)
                .setServerIndex(serverIndex)
                .setName("") // 登录时不需要名字
                .setClientVersion("1.0.0")
                .setDeviceId("test-device")
                .build();
        
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.CS_player_login_VALUE)
                .setSequence(++sequence)
                .setData(loginRequest.toByteString())
                .build();
        
        channel.writeAndFlush(gameMessage);
        logger.info("Sent login request for user: {}, serverIndex: {}", userId, serverIndex);
    }
    
    /**
     * 发送创建请求
     */
    private void sendCreateRequest(String userId, int serverIndex, String name) {
        CS_player_login createRequest = CS_player_login.newBuilder()
                .setUserId(userId)
                .setServerIndex(serverIndex)
                .setName(name)
                .setClientVersion("1.0.0")
                .setDeviceId("test-device")
                .build();

        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.CS_create_role_VALUE)
                .setSequence(++sequence)
                .setData(createRequest.toByteString())
                .build();

        channel.writeAndFlush(gameMessage);
        logger.info("Sent create role request for user: {}, serverIndex: {}, name: {}", userId, serverIndex, name);
    }
    
    /**
     * 发送用户信息请求
     */
    private void sendUserInfoRequest(long playerId) {
        UserInfoRequest userInfoRequest = UserInfoRequest.newBuilder()
                .setUserId(playerId) // 这里应该使用playerId而不是userId
                .build();
        
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.MSG_PLAYER_INFO_REQUEST_VALUE)
                .setSequence(++sequence)
                .setData(userInfoRequest.toByteString())
                .build();
        
        channel.writeAndFlush(gameMessage);
        logger.info("Sent user info request for playerId: {}", playerId);
    }
    
    /**
     * 发送用户更新请求
     */
    private void sendUserUpdateRequest(int level, long gold, long exp) {
        UserUpdateRequest updateRequest = UserUpdateRequest.newBuilder()
                .setUserId(0) // 这里应该使用playerId
                .setLevel(level)
                .setGold(gold)
                .setExp(exp)
                .build();
        
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.MSG_PLAYER_UPDATE_REQUEST_VALUE)
                .setSequence(++sequence)
                .setData(updateRequest.toByteString())
                .build();
        
        channel.writeAndFlush(gameMessage);
        logger.info("Sent user update request: level={}, gold={}, exp={}", level, gold, exp);
    }
    
    /**
     * 发送聊天请求
     */
    private void sendChatRequest(int chatType, String message, long targetUserId, long guildId) {
        ChatRequest chatRequest = ChatRequest.newBuilder()
                .setChatType(chatType)
                .setContent(message)
                .setTargetUserId(targetUserId)
                .setGuildId(guildId)
                .build();
        
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.MSG_CHAT_MESSAGE_REQUEST_VALUE)
                .setSequence(++sequence)
                .setData(chatRequest.toByteString())
                .build();
        
        channel.writeAndFlush(gameMessage);
        logger.info("Sent chat request: type={}, message={}", chatType, message);
    }
    
    /**
     * 发送登出请求
     */
    private void sendLogoutRequest() {
        LogoutRequest logoutRequest = LogoutRequest.newBuilder()
                .setUserId(0L) // 这里应该使用playerId
                .setReason("User logout")
                .build();
        
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.CS_player_logout_VALUE)
                .setSequence(++sequence)
                .setData(logoutRequest.toByteString())
                .build();
        
        channel.writeAndFlush(gameMessage);
        logger.info("Sent logout request for userId: {}", playerId);
    }
    
    /**
     * 发送心跳消息
     */
    private void sendHeartbeat() {
        HeartbeatMessage heartbeat = HeartbeatMessage.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setMessage("Client heartbeat")
                .build();
        
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgId(MsgIdEnum.MSG_SYSTEM_HEARTBEAT_REQ_VALUE)
                .setSequence(++sequence)
                .setData(heartbeat.toByteString())
                .build();
        
        channel.writeAndFlush(gameMessage);
        logger.info("Sent heartbeat message");
    }
    
    /**
     * 测试登录流程
     */
    private void testLoginFlow() {
        System.out.println("Testing login flow...");
        sendLoginRequest("testuser", 0);
    }
    
    /**
     * 测试聊天功能
     */
    private void testChatFlow() {
        System.out.println("Testing chat flow...");
        sendChatRequest(1, "Hello, world!", 0, 0); // 世界聊天
        sendChatRequest(2, "Private message", 123, 0); // 私聊
        sendChatRequest(3, "Guild message", 0, 456); // 公会聊天
    }
    
    /**
     * 压力测试
     */
    private void stressTest(int count) {
        System.out.println("Starting stress test with " + count + " heartbeat messages...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            sendHeartbeat();
            try {
                Thread.sleep(10); // 简短延迟避免过于频繁
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Stress test completed. Sent " + count + " messages in " + (endTime - startTime) + "ms");
    }
    
    /**
     * 客户端主函数
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9998;
        
        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        try {
            new EnhancedGameClient(host, port).start();
        } catch (Exception e) {
            logger.error("Enhanced game client error", e);
        }
    }
    
    /**
     * 增强版客户端处理器
     */
    private class EnhancedClientHandler extends SimpleChannelInboundHandler<GameMessage> {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("Channel active: {}", ctx.channel());
            System.out.println("Connected to server. Type 'help' for available commands.");
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, GameMessage msg) throws Exception {
            logger.info("Received message: msgId={}, sequence={}", msg.getMsgId(), msg.getSequence());
            currentCtx = ctx;
            // 使用新的枚举值进行比较
            short msgId = (short)msg.getMsgId();
            MsgIdEnum msgType = MsgIdEnum.forNumber(msgId);
            
            // 如果无法识别消息类型，尝试使用UNKNOWN类型处理
            if (msgType == null) {
                logger.warn("Unknown message msgId: {}, treating as UNKNOWN", msgId);
                System.out.println("Received unknown msgId: " + msgId + ", treating as UNKNOWN");
                msgType = MsgIdEnum.MSG_UNKNOWN;
            }
            
            switch (msgType) {
                case SC_player_login:
                    handleLoginResponse(msg);
                    break;
                case SC_create_role:
                    handleCreateRoleResponse(msg);
                    break;
                case MSG_PLAYER_INFO_RESPONSE:
                    handleUserInfoResponse(msg);
                    break;
                case MSG_PLAYER_UPDATE_RESPONSE:
                    handleUserUpdateResponse(msg);
                    break;
                case MSG_CHAT_MESSAGE_RESPONSE:
                    handleChatResponse(msg);
                    break;
                case MSG_CHAT_BROADCAST:
                    handleChatBroadcast(msg);
                    break;
                case SC_player_logout:
                    handleLogoutResponse(msg);
                    break;
                case MSG_SYSTEM_HEARTBEAT_RES:
                    handleHeartbeat(ctx, msg);
                    break;
                case MSG_SYSTEM_ERROR:
                    handleSystemError(msg);
                    break;
                case MSG_UNKNOWN:
                default:
                    logger.warn("Unknown or unhandled msgId: {}", msg.getMsgId());
                    System.out.println("Received unknown msgId: " + msg.getMsgId());
                    // 尝试打印原始数据以便调试
                    try {
                        System.out.println("Unknown message received: msgId=" + msg.getMsgId() +
                            ", sequence=" + msg.getSequence() + 
                            ", dataLength=" + msg.getData().size());
                    } catch (Exception e) {
                        logger.error("Error processing unknown message", e);
                    }
                    break;
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error("Client handler exception", cause);
            System.out.println("Client error: " + cause.getMessage());
            ctx.close();
        }
        
        /**
         * 处理登录响应
         */
        private void handleLoginResponse(GameMessage msg) {
            try {
                SC_player_login response = SC_player_login.parseFrom(msg.getData());
                System.out.println("=== Login Response ===");
                System.out.println("playerId: " + response.getPlayerId());
                System.out.println("name: " + response.getName());
                System.out.println("createnum: " + response.getCreatenum());
                System.out.println("serverIndex: " + response.getServerIndex());
                System.out.println("=====================");
                
                // 保存当前用户信息
                // 注意：这里需要根据实际情况调整，可能需要保存playerId而不是userId
                playerId = response.getPlayerId();
                currentServerIndex = response.getServerIndex();
                
                // 登录成功后自动发送心跳
                sendHeartbeat(currentCtx);
            } catch (Exception e) {
                logger.error("Failed to parse login response", e);
                System.out.println("Error parsing login response: " + e.getMessage());
            }
        }
        
        /**
         * 处理创角响应
         */
        private void handleCreateRoleResponse(GameMessage msg) {
            try {
                SC_create_role response = SC_create_role.parseFrom(msg.getData());
                System.out.println("=== Create Role Response ===");
                System.out.println("result: " + response.getResult());
                System.out.println("============================");
                
                if (response.getResult() == 1) {
                    System.out.println("Need to create role. Please use 'create' command.");
                } else {
                    System.out.println("Role created successfully.");
                }
            } catch (Exception e) {
                logger.error("Failed to parse create role response", e);
                System.out.println("Error parsing create role response: " + e.getMessage());
            }
        }
        
        /**
         * 处理用户信息响应
         */
        private void handleUserInfoResponse(GameMessage msg) {
            try {
                UserInfoResponse response = UserInfoResponse.parseFrom(msg.getData());
                System.out.println("=== User Info Response ===");
                System.out.println("Status: " + response.getStatus());
                System.out.println("Message: " + response.getMessage());
                if (response.hasUserInfo()) {
                    System.out.println("User Info: " + response.getUserInfo());
                }
                System.out.println("=========================");
            } catch (Exception e) {
                logger.error("Failed to parse user info response", e);
                System.out.println("Error parsing user info response: " + e.getMessage());
            }
        }
        
        /**
         * 处理用户更新响应
         */
        private void handleUserUpdateResponse(GameMessage msg) {
            try {
                UserUpdateResponse response = UserUpdateResponse.parseFrom(msg.getData());
                System.out.println("=== User Update Response ===");
                System.out.println("Status: " + response.getStatus());
                System.out.println("Message: " + response.getMessage());
                if (response.hasUserInfo()) {
                    System.out.println("Updated User Info: " + response.getUserInfo());
                }
                System.out.println("===========================");
            } catch (Exception e) {
                logger.error("Failed to parse user update response", e);
                System.out.println("Error parsing user update response: " + e.getMessage());
            }
        }
        
        /**
         * 处理聊天响应
         */
        private void handleChatResponse(GameMessage msg) {
            try {
                ChatResponse response = ChatResponse.parseFrom(msg.getData());
                System.out.println("=== Chat Response ===");
                System.out.println("Status: " + response.getStatus());
                System.out.println("Message: " + response.getMessage());
                System.out.println("====================");
            } catch (Exception e) {
                logger.error("Failed to parse chat response", e);
                System.out.println("Error parsing chat response: " + e.getMessage());
            }
        }
        
        /**
         * 处理聊天广播
         */
        private void handleChatBroadcast(GameMessage msg) {
            try {
                ChatBroadcast broadcast = ChatBroadcast.parseFrom(msg.getData());
                System.out.println("=== Chat Broadcast ===");
                System.out.println("From: " + broadcast.getFromUsername());
                System.out.println("Message: " + broadcast.getContent());
                System.out.println("Time: " + broadcast.getTimestamp());
                System.out.println("======================");
            } catch (Exception e) {
                logger.error("Failed to parse chat broadcast", e);
                System.out.println("Error parsing chat broadcast: " + e.getMessage());
            }
        }
        
        /**
         * 处理登出响应
         */
        private void handleLogoutResponse(GameMessage msg) {
            try {
                LogoutResponse response = LogoutResponse.parseFrom(msg.getData());
                System.out.println("=== Logout Response ===");
                System.out.println("Status: " + response.getStatus());
                System.out.println("Message: " + response.getMessage());
                System.out.println("=======================");
                playerId = 0L; // 清除当前用户ID
                currentServerIndex = 0; // 清除当前服务器索引
            } catch (Exception e) {
                logger.error("Failed to parse logout response", e);
                System.out.println("Error parsing logout response: " + e.getMessage());
            }
        }
        
        /**
         * 处理心跳消息
         */
        private void handleHeartbeat(ChannelHandlerContext ctx, GameMessage msg) {
            try {
                HeartbeatMessage heartbeat = HeartbeatMessage.parseFrom(msg.getData());
                System.out.println("=== Heartbeat ===");
                System.out.println("Server time: " + heartbeat.getTimestamp());
                System.out.println("Message: " + heartbeat.getMessage());
                System.out.println("=================");
            } catch (Exception e) {
                logger.error("Failed to parse heartbeat", e);
                System.out.println("Error parsing heartbeat: " + e.getMessage());
            }
        }
        
        /**
         * 处理系统错误
         */
        private void handleSystemError(GameMessage msg) {
            try {
                ErrorMessage error = ErrorMessage.parseFrom(msg.getData());
                System.out.println("=== System Error ===");
                System.out.println("Error Code: " + error.getErrorCode());
                System.out.println("Error Message: " + error.getErrorMessage());
                System.out.println("Details: " + error.getDetails());
                System.out.println("====================");
            } catch (Exception e) {
                logger.error("Failed to parse system error", e);
                System.out.println("Error parsing system error: " + e.getMessage());
            }
        }
        
        /**
         * 发送心跳消息
         */
        private void sendHeartbeat(ChannelHandlerContext ctx) {
            HeartbeatMessage heartbeat = HeartbeatMessage.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setMessage("Client heartbeat")
                    .build();
            
            GameMessage gameMessage = GameMessage.newBuilder()
                    .setMsgId(MsgIdEnum.MSG_SYSTEM_HEARTBEAT_REQ_VALUE)
                    .setSequence(++sequence)
                    .setData(heartbeat.toByteString())
                    .build();
            
            ctx.writeAndFlush(gameMessage);
            
            // 定时发送心跳
            ctx.executor().schedule(() -> sendHeartbeat(ctx), 30, TimeUnit.SECONDS);
        }
    }
}