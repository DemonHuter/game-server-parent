package com.game.netty;

import com.game.netty.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Netty服务器启动器
 */
@Component
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private ServerBootstrap serverBootstrap;

    @Autowired
    private EventLoopGroup bossGroup;

    @Autowired
    private EventLoopGroup workerGroup;

    private ChannelFuture channelFuture;

    @Autowired
    private NettyConfig nettyConfig;

    @PostConstruct
    public void start() {
        try {
            logger.info("Starting Netty server on port {}", nettyConfig.getPort());

            channelFuture = serverBootstrap.bind(nettyConfig.getPort()).sync();

            logger.info("Netty server started successfully on port {}", nettyConfig.getPort());

            // 注册关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        } catch (Exception e) {
            logger.error("Failed to start Netty server", e);
            throw new RuntimeException("Failed to start Netty server", e);
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping Netty server...");

        try {
            // 先停止接收新的连接
            if (channelFuture != null) {
                channelFuture.channel().close().sync();
            }
            
            logger.info("Netty server stopped accepting new connections");
        } catch (Exception e) {
            logger.error("Error closing server channel", e);
        }

        // 优雅关闭EventLoopGroup
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }

        logger.info("Netty server stopped");
    }

    /**
     * 获取服务器端口
     */
    public int getPort() {
        return nettyConfig.getPort();
    }

    /**
     * 检查服务器是否运行中
     */
    public boolean isRunning() {
        return channelFuture != null && channelFuture.channel().isActive();
    }
}