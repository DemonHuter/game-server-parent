package com.game.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 * 配置专用线程池用于处理不同类型的消息
 */
@Configuration
public class ThreadPoolConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    /**
     * 玩家消息处理线程池
     * 用于处理玩家相关的消息，如移动、战斗、聊天等
     */
    private ExecutorService playerMessageExecutorService;
    
    /**
     * 系统消息处理线程池
     * 用于处理系统相关的消息，如心跳、定时任务、系统通知等
     */
    private ExecutorService systemMessageExecutorService;

    /**
     * 玩家消息处理线程池
     * 用于处理玩家相关的消息，如移动、战斗、聊天等
     * 
     * @return ExecutorService线程池实例
     */
    @Bean(name = "playerMessageExecutor")
    public ExecutorService playerMessageExecutor() {
        playerMessageExecutorService = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new CustomizableThreadFactory("player-msg-pool-%d"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        return playerMessageExecutorService;
    }

    /**
     * 系统消息处理线程池
     * 用于处理系统相关的消息，如心跳、定时任务、系统通知等
     * 
     * @return ExecutorService线程池实例
     */
    @Bean(name = "systemMessageExecutor")
    public ExecutorService systemMessageExecutor() {
        systemMessageExecutorService = new ThreadPoolExecutor(
                2,
                4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new CustomizableThreadFactory("system-msg-pool-%d"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        return systemMessageExecutorService;
    }
    
    /**
     * 优雅关闭所有线程池
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down thread pools...");
        
        // 关闭玩家消息处理线程池
        if (playerMessageExecutorService != null) {
            playerMessageExecutorService.shutdown();
            try {
                if (!playerMessageExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Player message executor did not terminate in 30 seconds, forcing shutdown");
                    playerMessageExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                playerMessageExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 关闭系统消息处理线程池
        if (systemMessageExecutorService != null) {
            systemMessageExecutorService.shutdown();
            try {
                if (!systemMessageExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("System message executor did not terminate in 30 seconds, forcing shutdown");
                    systemMessageExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                systemMessageExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Thread pools shut down completed");
    }
}