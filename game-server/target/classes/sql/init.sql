-- ========================================
-- 游戏数据库初始化脚本
-- 数据库：game_db
-- 创建时间：2025-09-23
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `game_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `game_db`;

-- ========================================
-- 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `level` INT(11) NOT NULL DEFAULT 1 COMMENT '等级',
    `gold` BIGINT(20) NOT NULL DEFAULT 1000 COMMENT '金币',
    `exp` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '经验值',
    `last_login_time` DATETIME NULL COMMENT '最后登录时间',
    `is_online` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否在线',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_level` (`level`),
    KEY `idx_online` (`is_online`),
    KEY `idx_last_login` (`last_login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 聊天记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `chat_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '聊天记录ID',
    `chat_type` INT(11) NOT NULL COMMENT '聊天类型：1-世界聊天，2-私聊，3-公会聊天',
    `from_user_id` BIGINT(20) NOT NULL COMMENT '发送者用户ID',
    `from_username` VARCHAR(50) NOT NULL COMMENT '发送者用户名',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `target_user_id` BIGINT(20) NULL COMMENT '目标用户ID（私聊时使用）',
    `guild_id` BIGINT(20) NULL COMMENT '公会ID（公会聊天时使用）',
    `timestamp` BIGINT(20) NOT NULL COMMENT '发送时间戳',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_chat_type` (`chat_type`),
    KEY `idx_from_user` (`from_user_id`),
    KEY `idx_target_user` (`target_user_id`),
    KEY `idx_guild` (`guild_id`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

-- ========================================
-- 公会表（预留）
-- ========================================
CREATE TABLE IF NOT EXISTS `guild` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公会ID',
    `name` VARCHAR(100) NOT NULL COMMENT '公会名称',
    `description` TEXT COMMENT '公会描述',
    `leader_id` BIGINT(20) NOT NULL COMMENT '会长用户ID',
    `member_count` INT(11) NOT NULL DEFAULT 1 COMMENT '成员数量',
    `max_members` INT(11) NOT NULL DEFAULT 50 COMMENT '最大成员数',
    `level` INT(11) NOT NULL DEFAULT 1 COMMENT '公会等级',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_leader` (`leader_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公会表';

-- ========================================
-- 公会成员表（预留）
-- ========================================
CREATE TABLE IF NOT EXISTS `guild_member` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `guild_id` BIGINT(20) NOT NULL COMMENT '公会ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `position` INT(11) NOT NULL DEFAULT 1 COMMENT '职位：1-成员，2-副会长，3-会长',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `contribution` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '贡献值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_guild_user` (`guild_id`, `user_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_position` (`position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公会成员表';

-- ========================================
-- 游戏日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `game_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `log_type` VARCHAR(50) NOT NULL COMMENT '日志类型',
    `user_id` BIGINT(20) NULL COMMENT '相关用户ID',
    `content` TEXT NOT NULL COMMENT '日志内容',
    `extra_data` JSON NULL COMMENT '额外数据',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏日志表';

-- ========================================
-- 任务执行记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `task_execution_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `execution_time` BIGINT NOT NULL COMMENT '执行时间戳',
  `success` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '执行是否成功(1:成功, 0:失败)',
  `error_message` TEXT COMMENT '错误信息',
  `duration` BIGINT NOT NULL DEFAULT 0 COMMENT '执行耗时(毫秒)',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_name` (`task_name`),
  KEY `idx_execution_time` (`execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

-- ========================================
-- 计数器表
-- ========================================
CREATE TABLE IF NOT EXISTS `counter` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `player_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '玩家ID（NULL表示全局计数器）',
  `counter_data` JSON NOT NULL COMMENT '计数器数据（JSON格式）',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计数器表';

-- ========================================
-- 插入测试数据
-- ========================================

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `level`, `gold`, `exp`, `is_online`) VALUES
('admin', 'admin123', 99, 999999, 99000, FALSE),
('player1', 'password1', 5, 5000, 4500, FALSE),
('player2', 'password2', 3, 3000, 2500, FALSE),
('player3', 'password3', 1, 1000, 0, FALSE),
('testuser', 'test123', 10, 10000, 9500, FALSE);

-- 插入测试聊天记录
INSERT INTO `chat_log` (`chat_type`, `from_user_id`, `from_username`, `content`, `timestamp`) VALUES
(1, 1, 'admin', '欢迎来到游戏世界！', UNIX_TIMESTAMP(NOW()) * 1000),
(1, 2, 'player1', '大家好！', UNIX_TIMESTAMP(NOW() - INTERVAL 1 HOUR) * 1000),
(1, 3, 'player2', '今天天气不错', UNIX_TIMESTAMP(NOW() - INTERVAL 2 HOUR) * 1000),
(2, 2, 'player1', '你好，我是新玩家', UNIX_TIMESTAMP(NOW() - INTERVAL 3 HOUR) * 1000),
(1, 4, 'player3', '有人组队吗？', UNIX_TIMESTAMP(NOW() - INTERVAL 4 HOUR) * 1000);

-- 插入测试公会
INSERT INTO `guild` (`name`, `description`, `leader_id`, `member_count`, `level`) VALUES
('龙门客栈', '欢迎所有勇敢的冒险者加入我们的大家庭！', 1, 3, 2),
('星辰公会', '追求卓越，共创辉煌', 2, 2, 1);

-- 插入公会成员
INSERT INTO `guild_member` (`guild_id`, `user_id`, `position`, `contribution`) VALUES
(1, 1, 3, 10000),  -- admin 是会长
(1, 2, 1, 1500),   -- player1 是成员
(1, 3, 1, 800),    -- player2 是成员
(2, 2, 3, 5000),   -- player1 也是星辰公会的会长
(2, 4, 1, 200);    -- player3 是星辰公会成员

-- 插入测试日志
INSERT INTO `game_log` (`log_type`, `user_id`, `content`, `extra_data`) VALUES
('USER_LOGIN', 1, '管理员登录系统', JSON_OBJECT('ip', '127.0.0.1', 'device', 'PC')),
('USER_LOGIN', 2, '玩家1登录游戏', JSON_OBJECT('ip', '192.168.1.100', 'device', 'Mobile')),
('LEVEL_UP', 2, '玩家1升级到5级', JSON_OBJECT('old_level', 4, 'new_level', 5, 'exp_gained', 500)),
('CHAT_SEND', 2, '玩家1发送聊天消息', JSON_OBJECT('chat_type', 1, 'content_length', 4)),
('USER_LOGOUT', 2, '玩家1退出游戏', JSON_OBJECT('online_time', 3600));

-- ========================================
-- 创建索引（如果需要额外的索引）
-- ========================================

-- 用户表的复合索引
CREATE INDEX `idx_user_level_exp` ON `user` (`level`, `exp`);
CREATE INDEX `idx_user_online_login` ON `user` (`is_online`, `last_login_time`);

-- 聊天记录表的复合索引
CREATE INDEX `idx_chat_type_time` ON `chat_log` (`chat_type`, `timestamp`);
CREATE INDEX `idx_private_chat` ON `chat_log` (`chat_type`, `from_user_id`, `target_user_id`);
CREATE INDEX `idx_guild_chat` ON `chat_log` (`chat_type`, `guild_id`, `timestamp`);

-- ========================================
-- 创建视图（可选）
-- ========================================

-- 在线用户视图
CREATE OR REPLACE VIEW `v_online_users` AS
SELECT 
    `id`,
    `username`,
    `level`,
    `gold`,
    `exp`,
    `last_login_time`
FROM `user`
WHERE `is_online` = TRUE;

-- 用户统计视图
CREATE OR REPLACE VIEW `v_user_stats` AS
SELECT 
    COUNT(*) as `total_users`,
    COUNT(CASE WHEN `is_online` = TRUE THEN 1 END) as `online_users`,
    AVG(`level`) as `avg_level`,
    MAX(`level`) as `max_level`,
    SUM(`gold`) as `total_gold`
FROM `user`;

-- 聊天统计视图
CREATE OR REPLACE VIEW `v_chat_stats` AS
SELECT 
    `chat_type`,
    COUNT(*) as `message_count`,
    COUNT(DISTINCT `from_user_id`) as `unique_senders`,
    MIN(`create_time`) as `first_message`,
    MAX(`create_time`) as `last_message`
FROM `chat_log`
GROUP BY `chat_type`;

-- 任务执行统计视图
CREATE OR REPLACE VIEW `v_task_stats` AS
SELECT 
    `task_name`,
    COUNT(*) as `total_executions`,
    COUNT(CASE WHEN `success` = 1 THEN 1 END) as `successful_executions`,
    COUNT(CASE WHEN `success` = 0 THEN 1 END) as `failed_executions`,
    AVG(`duration`) as `avg_duration`,
    MAX(`execution_time`) as `last_execution_time`
FROM `task_execution_record`
GROUP BY `task_name`;

-- ========================================
-- 数据库用户和权限设置
-- ========================================

-- 创建游戏服务专用用户
CREATE USER IF NOT EXISTS 'game_user'@'localhost' IDENTIFIED BY 'game_pass';
CREATE USER IF NOT EXISTS 'game_user'@'%' IDENTIFIED BY 'game_pass';

-- 授权
GRANT SELECT, INSERT, UPDATE, DELETE ON `game_db`.* TO 'game_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON `game_db`.* TO 'game_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库初始化完成！' as message,
       (SELECT COUNT(*) FROM `user`) as user_count,
       (SELECT COUNT(*) FROM `chat_log`) as chat_count,
       (SELECT COUNT(*) FROM `guild`) as guild_count;