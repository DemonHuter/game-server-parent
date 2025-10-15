package com.game.model.handler;

import com.game.core.GameHandlerManager;
import com.game.core.GameMessageHandler;
import com.game.model.heartbeat.HeartBeatHandler;
import com.game.model.player.login.PlayerLoginHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class GameHandlerTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testHandlerRegistration() {
        // 获取GameHandlerManager bean
        GameHandlerManager handlerManager = context.getBean(GameHandlerManager.class);
        assertNotNull(handlerManager, "GameHandlerManager should not be null");
        
        // 检查是否有注册的处理器
        assertTrue(handlerManager.getHandlerCount() > 0, "There should be registered handlers");
        
        System.out.println("Total registered handlers: " + handlerManager.getHandlerCount());
    }
    
    @Test
    public void testHandlerRetrieval() {
        // 获取GameHandlerManager bean
        GameHandlerManager handlerManager = context.getBean(GameHandlerManager.class);
        
        // 测试获取具体的处理器
        GameMessageHandler systemHandler = handlerManager.getHandler(1000);
        GameMessageHandler userHandler = handlerManager.getHandler(2000);
        GameMessageHandler chatHandler = handlerManager.getHandler(3000);
        
        // 验证处理器是否正确注册
        assertNotNull(systemHandler, "System handler should be registered");
        assertNotNull(userHandler, "User handler should be registered");
        assertNotNull(chatHandler, "Chat handler should be registered");
        
        // 验证处理器类型是否正确
        assertTrue(systemHandler instanceof HeartBeatHandler, "System handler should be SystemModuleHandler");
        assertTrue(userHandler instanceof PlayerLoginHandler, "User handler should be UserModuleHandler");
//        assertTrue(chatHandler instanceof ChatModuleHandler, "Chat handler should be ChatModuleHandler");
        
        System.out.println("System handler: " + systemHandler.getClass().getSimpleName());
        System.out.println("User handler: " + userHandler.getClass().getSimpleName());
        System.out.println("Chat handler: " + chatHandler.getClass().getSimpleName());
    }
}