package com.game.core;

import javax.annotation.PreDestroy;

/**
 * @description: 系统停止时执行
 * @author: guosheng
 * @date: 2025/10/13 13:37
 */
public abstract class AbstractSystemShutdown {
    public abstract void shutdown();

    @PreDestroy
    public void preDestroy() {
        shutdown();
    }
}
