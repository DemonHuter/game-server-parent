-- ========================================
-- 数据库清理脚本
-- 用于开发和测试环境重置数据库
-- ========================================

USE `game_db`;

-- 清理数据（保留表结构）
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `game_log`;
TRUNCATE TABLE `guild_member`;
TRUNCATE TABLE `guild`;
TRUNCATE TABLE `chat_log`;
TRUNCATE TABLE `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- 重置自增ID
ALTER TABLE `user` AUTO_INCREMENT = 1;
ALTER TABLE `chat_log` AUTO_INCREMENT = 1;
ALTER TABLE `guild` AUTO_INCREMENT = 1;
ALTER TABLE `guild_member` AUTO_INCREMENT = 1;
ALTER TABLE `game_log` AUTO_INCREMENT = 1;

SELECT '数据库清理完成！' as message;