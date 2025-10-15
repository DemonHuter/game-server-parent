package com.game.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 游戏处理器注解
 * 用于标识处理特定消息类型的处理器
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameHandler {
    
    /**
     * 处理的消息类型ID
     */
    int msgId() default 0;
}