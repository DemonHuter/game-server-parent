package com.game.init;

import com.game.cache.CacheManager;
import com.game.common.constant.GameConstants;
import com.game.common.util.ScheduleUtil;
import com.game.constant.SystemInitializeOrder;
import com.game.model.heartbeat.HeartBeatManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 任务初始化器
 * 在系统启动时注册所有定时任务
 */
@Component
public class TaskInitializer implements SystemInitializer {

    private static final Logger logger = LoggerFactory.getLogger(TaskInitializer.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private HeartBeatManager heartBeatManager;

    @Override
    public int getOrder() {
        return SystemInitializeOrder.TASK;
    }

    @Override
    public void initialize() throws Exception {
        ScheduleUtil.scheduleAtFixedRate(() -> cacheManager.syncAllDataToDatabase(),
                GameConstants.Cache.PERSISTENCE_INTERVAL,
                GameConstants.Cache.PERSISTENCE_INTERVAL,
                TimeUnit.MILLISECONDS
        );

        ScheduleUtil.scheduleAtFixedRate(() -> heartBeatManager.checkHeartbeatTimeouts(),
                GameConstants.Heartbeat.HEARTBEAT_CHECK_INTERVAL,
                GameConstants.Heartbeat.HEARTBEAT_CHECK_INTERVAL,
                TimeUnit.MILLISECONDS
        );

        logger.info("Starting scheduled cache data sync");
    }
}