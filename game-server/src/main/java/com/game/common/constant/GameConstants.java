package com.game.common.constant;

/**
 * 游戏常量定义
 */
public class GameConstants {
    
    /**
     * 消息相关常量
     */
    public static class Message {
        /** 消息最大长度 */
        public static final int MAX_MESSAGE_LENGTH = 1024 * 1024; // 1MB
        
        /** 心跳间隔（毫秒） */
        public static final int HEARTBEAT_INTERVAL = 30000; // 30秒
        
        /** 消息超时时间（毫秒） */
        public static final int MESSAGE_TIMEOUT = 10000; // 10秒
    }
    
    /**
     * 用户相关常量
     */
    public static class User {
        /** 最大用户名长度 */
        public static final int MAX_USERNAME_LENGTH = 20;
        
        /** 最小用户名长度 */
        public static final int MIN_USERNAME_LENGTH = 3;
        
        /** 最大密码长度 */
        public static final int MAX_PASSWORD_LENGTH = 32;
        
        /** 最小密码长度 */
        public static final int MIN_PASSWORD_LENGTH = 6;
        
        /** 初始等级 */
        public static final int INITIAL_LEVEL = 1;
        
        /** 初始金币 */
        public static final long INITIAL_GOLD = 1000;
        
        /** 初始经验值 */
        public static final long INITIAL_EXP = 0;
    }
    
    /**
     * 聊天相关常量
     */
    public static class Chat {
        /** 聊天类型：世界聊天 */
        public static final int TYPE_WORLD = 1;
        
        /** 聊天类型：私聊 */
        public static final int TYPE_PRIVATE = 2;
        
        /** 聊天类型：公会聊天 */
        public static final int TYPE_GUILD = 3;
        
        /** 最大聊天内容长度 */
        public static final int MAX_CONTENT_LENGTH = 500;
        
        /** 聊天频率限制（毫秒） */
        public static final int CHAT_INTERVAL_LIMIT = 1000; // 1秒
    }
    
    /**
     * 线程池相关常量
     */
    public static class ThreadPool {
        /** 系统消息线程池名称 */
        public static final String SYSTEM_POOL = "system-pool";
        
        /** 玩家消息线程池名称 */
        public static final String PLAYER_POOL = "player-pool";
        
        /** 广播消息线程池名称 */
        public static final String BROADCAST_POOL = "broadcast-pool";
        
        /** 定时任务线程池名称 */
        public static final String SCHEDULED_POOL = "scheduled-pool";
    }
    
    /**
     * 缓存相关常量
     */
    public static class Cache {
        /** 缓存持久化间隔（毫秒） */
        public static final long PERSISTENCE_INTERVAL = 60000; // 1分钟
        
        /** 缓存清理间隔（毫秒） */
        public static final long CLEANUP_INTERVAL = 300000; // 5分钟
        
        /** 用户离线后缓存保留时间（毫秒） */
        public static final long USER_CACHE_RETENTION = 1800000; // 30分钟
    }

    /**
     * 心跳相关常量
     */
    public static class Heartbeat {
        /** 心跳监测间隔（毫秒） */
        public static final long HEARTBEAT_CHECK_INTERVAL = 60000; // 1分钟
    }
    
    /**
     * 错误码定义
     */
    public static class ErrorCode {
        /** 成功 */
        public static final int SUCCESS = 0;
        
        /** 系统错误 */
        public static final int SYSTEM_ERROR = 1000;
        
        /** 参数错误 */
        public static final int INVALID_PARAM = 1001;
        
        /** 权限不足 */
        public static final int PERMISSION_DENIED = 1002;
        
        /** 用户不存在 */
        public static final int USER_NOT_FOUND = 2000;
        
        /** 用户已在线 */
        public static final int USER_ALREADY_ONLINE = 2001;
        
        /** 密码错误 */
        public static final int INVALID_PASSWORD = 2002;
        
        /** 用户名已存在 */
        public static final int USERNAME_EXISTS = 2003;
        
        /** 聊天频率过快 */
        public static final int CHAT_TOO_FAST = 3000;
        
        /** 聊天内容过长 */
        public static final int CHAT_CONTENT_TOO_LONG = 3001;
    }
}