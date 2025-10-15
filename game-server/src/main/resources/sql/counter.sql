-- 计数器表
CREATE TABLE IF NOT EXISTS `counter` (
    `playerid` BIGINT NOT NULL COMMENT '玩家ID（-1表示全局计数器）',
    `counterdata` JSON NOT NULL COMMENT '计数器数据（JSON格式）',
    `createtime` BIGINT(20) COMMENT '创建时间',
    `updatetime` BIGINT(20) COMMENT '更新时间',
    PRIMARY KEY (`playerid`),
    KEY `idx_createtime` (`createtime`),
    KEY `idx_updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计数器表';