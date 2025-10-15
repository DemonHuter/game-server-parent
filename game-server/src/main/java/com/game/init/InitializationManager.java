package com.game.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 系统初始化管理器
 * 负责在系统启动时统一执行所有实现了SystemInitializer接口的初始化逻辑
 */
@Component
public class InitializationManager implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitializationManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 系统启动后自动执行初始化
     */
    public void init() {
        try {
            initializeAll();
        } catch (Exception e) {
            logger.error("系统初始化过程中发生错误", e);
            throw new RuntimeException("系统初始化失败", e);
        }
    }

    /**
     * 执行所有初始化器的初始化逻辑
     */
    private void initializeAll() throws Exception {
        // 获取所有实现了SystemInitializer接口的bean
        Map<String, SystemInitializer> initializerMap = applicationContext.getBeansOfType(SystemInitializer.class);

        // 按照顺序排序
        List<SystemInitializer> sortedInitializers = new ArrayList<>(initializerMap.values());
        sortedInitializers.sort(Comparator.comparingInt(SystemInitializer::getOrder));

        // 依次执行初始化
        for (SystemInitializer initializer : sortedInitializers) {
            try {
                initializer.initialize();
            } catch (Exception e) {
                throw new Exception("初始化器执行失败", e);
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
    }
}