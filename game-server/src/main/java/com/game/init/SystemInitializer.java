package com.game.init;

/**
 * 系统初始化接口
 * 所有需要在系统启动时执行初始化逻辑的类都应该实现此接口
 */
public interface SystemInitializer {
    
    /**
     * 执行初始化逻辑
     * 
     * @throws Exception 初始化过程中可能抛出的异常
     */
    void initialize() throws Exception;
    
    /**
     * 获取初始化顺序
     * 数字越小优先级越高，越早执行
     * 
     * @return 初始化顺序
     */
    default int getOrder() {
        return 0;
    }
}