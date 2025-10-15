package com.game;

import com.game.core.GracefulShutdownManager;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.game")
@MapperScan("com.game.dao.mapper")
public class GameServerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(GameServerApplication.class);
    
    public static void main(String[] args) {
        try {
            logger.info("Starting Game Server Application...");

            // 运行Spring应用并保存应用上下文
            ConfigurableApplicationContext context = SpringApplication.run(GameServerApplication.class, args);
            
            // 注册关闭钩子
            setupShutdownHook(context);
            
            logger.info("Game Server Application started successfully!");
        } catch (Exception e) {
            logger.error("Failed to start Game Server Application", e);
            System.exit(1);
        }
    }
    
    /**
     * 设置关闭钩子以实现优雅关闭
     * 
     * @param context Spring应用上下文
     */
    private static void setupShutdownHook(ConfigurableApplicationContext context) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered, starting graceful shutdown...");
            
            try {
                // 获取优雅关闭管理器并执行关闭
                GracefulShutdownManager shutdownManager = context.getBean(GracefulShutdownManager.class);
                shutdownManager.stop();
                
                // 关闭Spring应用上下文
                context.close();
                
                logger.info("Application shutdown gracefully");
            } catch (Exception e) {
                logger.error("Error during graceful shutdown", e);
            }
        }));
    }
}