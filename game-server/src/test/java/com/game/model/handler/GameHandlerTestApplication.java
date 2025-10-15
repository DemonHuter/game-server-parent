package com.game.model.handler;

import com.game.core.GameHandlerManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class GameHandlerTestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(GameHandlerTestApplication.class, args);
        
        // 获取GameHandlerManager bean
        GameHandlerManager handlerManager = context.getBean(GameHandlerManager.class);
        
        System.out.println("Total registered handlers: " + handlerManager.getHandlerCount());
        
        // 测试获取处理器
        System.out.println("Handler for msgId 1000: " + handlerManager.getHandler(1000));
        System.out.println("Handler for msgId 2000: " + handlerManager.getHandler(2000));
        System.out.println("Handler for msgId 3000: " + handlerManager.getHandler(3000));
    }
}