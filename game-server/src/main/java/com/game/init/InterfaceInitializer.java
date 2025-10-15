package com.game.init;

import com.game.constant.SystemInitializeOrder;
import com.game.model.player.login.PlayerLoginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接口初始化器
 * 在系统启动时初始化所有接口
 */
@Component
public class InterfaceInitializer implements SystemInitializer {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceInitializer.class);

    @Autowired
    private PlayerLoginManager playerLoginManager;

    @Override
    public int getOrder() {
        return SystemInitializeOrder.INTERFACE;
    }

    @Override
    public void initialize() throws Exception {
        //玩家登录接口
        playerLoginManager.init();

        logger.info("init interface success");
    }
}