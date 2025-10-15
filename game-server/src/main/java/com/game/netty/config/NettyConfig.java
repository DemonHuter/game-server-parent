package com.game.netty.config;

import com.game.netty.handler.GameChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Netty服务器配置
 */
@Configuration
public class NettyConfig {

    @Value("${netty.server.port:9999}")
    private int port;

    @Value("${netty.server.bossThreads:1}")
    private int bossThreads;

    @Value("${netty.server.workerThreads:0}")
    private int workerThreads;

    @Value("${netty.server.backlog:128}")
    private int backlog;

    @Value("${netty.server.keepAlive:true}")
    private boolean keepAlive;

    @Value("${netty.server.tcpNoDelay:true}")
    private boolean tcpNoDelay;

    /**
     * Boss线程组
     */
    @Bean
    public EventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossThreads);
    }

    /**
     * Worker线程组
     */
    @Bean
    public EventLoopGroup workerGroup() {
        // Worker线程数建议设置为CPU核心数的2倍，但不要超过16
        int workers = workerThreads == 0 ?
                Math.min(Runtime.getRuntime().availableProcessors() * 2, 16) :
                workerThreads;
        return new NioEventLoopGroup(workers);
    }

    /**
     * 服务器启动配置
     */
    @Bean
    public ServerBootstrap serverBootstrap(EventLoopGroup bossGroup,
                                           EventLoopGroup workerGroup,
                                           GameChannelInitializer channelInitializer) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, backlog)
                .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
                .childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 1024 * 1024) // 1MB接收缓冲区
                .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024) // 1MB发送缓冲区
                // 添加以下优化参数
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(32 * 1024, 64 * 1024)) // 写缓冲区水位
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT); // 使用池化内存

        return bootstrap;
    }

    /**
     * 获取服务器端口
     */
    public int getPort() {
        return port;
    }

    /**
     * 获取Boss线程数
     */
    public int getBossThreads() {
        return bossThreads;
    }

    /**
     * 获取Worker线程数
     */
    public int getWorkerThreads() {
        return workerThreads == 0 ? Runtime.getRuntime().availableProcessors() * 2 : workerThreads;
    }
}