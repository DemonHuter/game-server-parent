package com.game.core;

import com.game.proto.GameMessage;
import com.game.proto.msgid.MsgIdEnum;
import com.google.protobuf.Parser;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏处理器管理器
 * 负责管理所有通过注解注册的游戏处理器
 */
@Component
public class GameHandlerManager implements ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(GameHandlerManager.class);
    
    // 存储消息ID到处理器的映射
    private final Map<Integer, GameMessageHandler<?>> handlerMap = new ConcurrentHashMap<>();

    private final static Map<Short, Parser> parserMap = new ConcurrentHashMap<>();

    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 应用上下文设置完成后，扫描并注册所有处理器
        registerHandlers();
    }
    
    /**
     * 扫描并注册所有处理器
     */
    private void registerHandlers() {
        try {
            // 获取所有带有GameHandler注解的Bean
            Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(GameHandler.class);

            for (Map.Entry<String, Object> entry : handlers.entrySet()) {
                Object handler = entry.getValue();
                if (handler instanceof GameMessageHandler) {
                    GameHandler annotation = handler.getClass().getAnnotation(GameHandler.class);

                    // 注册具体的msgId处理器
                    if (annotation.msgId() != 0) {
                        int msgId = annotation.msgId();

                        // 通过反射获取泛型类型
                        Type genericSuperclass = handler.getClass().getGenericSuperclass();
                        if (genericSuperclass instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                            Class messageType = (Class) parameterizedType.getActualTypeArguments()[0];

                            // 使用parser()静态方法获取Parser
                            Method parserMethod = messageType.getMethod("parser");
                            Parser invoke = (Parser) parserMethod.invoke(null);
                            parserMap.put(Short.valueOf(msgId + ""), invoke);
                        }

                        handlerMap.put(msgId, (GameMessageHandler<?>) handler);
                        logger.info("Registered handler for msgId {}: {}", msgId, handler.getClass().getSimpleName());
                    }
                }
            }
        }catch (Exception e) {
            throw new RuntimeException("Failed to register GameMessageHandler", e);
        }

        
        logger.info("Total registered handlers: {}", handlerMap.size());
    }
    
    /**
     * 根据消息ID获取处理器并处理消息
     */
    public void handle(ChannelHandlerContext ctx, GameMessage msg) {
        try {
            int msgId = msg.getMsgId();
            
            // 使用GameHandlerManager根据msgId获取处理器
            GameMessageHandler<?> handler = getHandler(msgId);
            
            if (handler != null) {
                // 设置上下文
                handler.setCtx(ctx);
                
                // 直接调用处理器的handleRawMessage方法，避免使用switch
                handler.handleRawMessage(msg);
            } else {
                logger.warn("No handler found for msgId: {}", msg.getMsgId());
                MessageUtils.sendErrorResponse(ctx, msg.getSequence(), MsgIdEnum.MSG_SYSTEM_ERROR_VALUE,"No handler found for message type");
            }
        } catch (Exception e) {
            logger.error("Error handling message: msgId={}, sequence={}", msg.getMsgId(), msg.getSequence(), e);
            MessageUtils.sendErrorResponse(ctx, msg.getSequence(), MsgIdEnum.MSG_SYSTEM_ERROR_VALUE,"Internal server error");
        }
    }
    
    /**
     * 根据消息ID获取处理器
     */
    public GameMessageHandler<?> getHandler(int msgId) {
        
        return handlerMap.getOrDefault(msgId, null);
    }
    
    /**
     * 获取所有已注册的处理器数量
     */
    public int getHandlerCount() {
        return handlerMap.size();
    }

    public static Parser getParser(short msgId) {
        return parserMap.getOrDefault(msgId, null);
    }
}