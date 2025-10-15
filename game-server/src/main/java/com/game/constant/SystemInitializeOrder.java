package com.game.constant;

/**
 * @description: 系统初始化顺序，顺序越小越靠前
 * @author: guosheng
 * @date: 2025/10/14 17:03
 */
public class SystemInitializeOrder {

    /**
     * 配置
     */
    public final static int CONFIG = -100;

    /**
     * 缓存
     */
    public final static int CACHE = -90;

    /**
     * 任务
     */
    public final static int TASK = -80;

    /**
     * 线程
     */
    public final static int EXECUTOR = -70;

    /**
     * 接口
     */
    public final static int INTERFACE = -60;

    /**
     * ID生成器
     */
    public final static int IDGENERATOR = -50;
}
