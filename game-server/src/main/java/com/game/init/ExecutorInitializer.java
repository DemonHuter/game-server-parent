package com.game.init;

import com.game.constant.SystemInitializeOrder;
import com.game.core.AsyncMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 线程初始化器
 * 在系统启动时初始化所有线程
 */
@Component
public class ExecutorInitializer implements SystemInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorInitializer.class);

    @Autowired
    private AsyncMessageUtils asyncMessageUtils;

    @Override
    public int getOrder() {
        return SystemInitializeOrder.CACHE;
    }

    @Override
    public void initialize() throws Exception {
        asyncMessageUtils.init();
        logger.info("AsyncMessageUtils initialized success");
    }
}