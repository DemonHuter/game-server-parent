-- ========================================
-- 数据库维护脚本
-- 用于定期清理过期数据和优化性能
-- ========================================

USE `game_db`;

-- 清理7天前的聊天记录
DELETE FROM `chat_log` 
WHERE `create_time` < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- 清理30天前的游戏日志
DELETE FROM `game_log` 
WHERE `create_time` < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 优化表
OPTIMIZE TABLE `user`;
OPTIMIZE TABLE `chat_log`;
OPTIMIZE TABLE `guild`;
OPTIMIZE TABLE `guild_member`;
OPTIMIZE TABLE `game_log`;

-- 更新表统计信息
ANALYZE TABLE `user`;
ANALYZE TABLE `chat_log`;
ANALYZE TABLE `guild`;
ANALYZE TABLE `guild_member`;
ANALYZE TABLE `game_log`;

SELECT 
    '数据库维护完成！' as message,
    (SELECT COUNT(*) FROM `chat_log`) as remaining_chat_logs,
    (SELECT COUNT(*) FROM `game_log`) as remaining_game_logs;