package com.game.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 优雅关闭管理器
 * 确保在服务关闭前处理完所有已接收的消息
 */
@Component
public class GracefulShutdownManager implements ApplicationContextAware, SmartLifecycle {
    
    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownManager.class);
    
    private ApplicationContext applicationContext;
    
    private volatile boolean running = false;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void start() {
        running = true;
        logger.info("GracefulShutdownManager started");
    }
    
    @Override
    public void stop() {
        logger.info("Starting graceful shutdown...");
        running = false;
        
        try {
            // 等待消息处理线程池中的任务完成
            awaitTerminationOfMessageExecutors();
            
            logger.info("Graceful shutdown completed successfully");
        } catch (Exception e) {
            logger.error("Error during graceful shutdown", e);
        }
    }
    
    /**
     * 等待消息处理线程池中的任务完成
     */
    private void awaitTerminationOfMessageExecutors() {
        try {
            logger.info("Waiting for message processing to complete...");
            
            // 获取玩家消息处理线程池
            ExecutorService playerExecutor = applicationContext.getBean("playerMessageExecutor", ExecutorService.class);
            // 获取系统消息处理线程池
            ExecutorService systemExecutor = applicationContext.getBean("systemMessageExecutor", ExecutorService.class);
            
            // 先关闭线程池，不再接受新任务
            playerExecutor.shutdown();
            systemExecutor.shutdown();
            
            // 等待最多30秒让现有任务完成
            if (!playerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Player message executor did not terminate in 30 seconds, forcing shutdown");
                playerExecutor.shutdownNow();
            }
            
            if (!systemExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("System message executor did not terminate in 30 seconds, forcing shutdown");
                systemExecutor.shutdownNow();
            }
            
            logger.info("Message processing completed");
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for message processing to complete", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error waiting for message processing to complete", e);
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}