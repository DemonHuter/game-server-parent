package com.game.model.player.login;

import com.game.cache.PlayerCache;
import com.game.cache.enhance.PlayerCacheEnhancer;
import com.game.common.util.IdGenerator;
import com.game.core.MessageUtils;
import com.game.dao.entity.Player;
import com.game.model.CommonManager;
import com.game.proto.ResponseStatus;
import com.game.proto.login.*;
import com.game.proto.msgid.MsgIdEnum;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户业务逻辑管理器
 * 处理与用户相关的业务逻辑
 */
@Service
public class PlayerLoginManager extends CommonManager {

    private static final Logger logger = LoggerFactory.getLogger(PlayerLoginManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PlayerCache playerCache;

    @Autowired
    private PlayerCacheEnhancer playerCacheEnhancer;

    @Autowired
    private PlayerSessionManager playerSessionManager;

    private List<IPlayerLogin> playerLoginHandlers = new ArrayList<>();

    @Value("${game.serverIndex:0}")
    private int serverIndex;

    /**
     * 初始化，获取所有实现了IPlayerLogin接口的bean
     */
    public void init() {
        Map<String, IPlayerLogin> handlers = applicationContext.getBeansOfType(IPlayerLogin.class);
        playerLoginHandlers.addAll(handlers.values());
        logger.info("已加载 {} 个玩家登录后处理器: {}", playerLoginHandlers.size(),
                playerLoginHandlers.stream().map(h -> h.getClass().getSimpleName()).toArray());
    }

    /**
     * 处理用户登录请求
     *
     * @param ctx      网络连接上下文
     * @param request  登录请求消息
     * @param sequence 消息序列号
     */
    public void handleLogin(ChannelHandlerContext ctx, CS_player_login request, int sequence) {
        logger.info("Processing login request for userId: {}", request.getUserId());

        if (request.getServerIndex() != serverIndex) {
            return;
        }

        // TODO: 实现实际的登录逻辑，如验证用户名和密码、查询数据库等
        Player player = playerCacheEnhancer.getPlayer(request.getUserId(), request.getServerIndex());
        if (player == null) {
            // 如果玩家不存在，创建新玩家
            player = createNewPlayer(request);
        }

        // 如果玩家已存在，更新登录时间
        player.setLogintime(System.currentTimeMillis());
        // 更新玩家信息到缓存和数据库
        playerCache.add(player);

        // 注意：心跳注册现在通过PlayerSessionManager的bindPlayerSession方法自动完成
        // 当调用bindPlayerSession时，会触发GameEventManager中的handlePlayerRegistered事件
        // 该事件会调用HeartBeatManager.registerPlayerHeartbeat方法
        playerSessionManager.bindPlayerSession(player.getPlayerid(), ctx);

        // 执行所有玩家登录后处理逻辑
        executeLoginHandlers(player);

        SC_player_login.Builder login = SC_player_login.newBuilder();
        login.setPlayerId(player.getPlayerid());
        login.setCreatenum(player.getCreatenum());
        login.setServerIndex(player.getServerindex());
        login.setName(player.getName());

        messageSender.sendMessageToPlayer(player.getPlayerid(), MsgIdEnum.SC_player_login_VALUE, login);
    }

    /**
     * 创建新玩家
     *
     * @param request 登录请求
     * @return 新创建的玩家对象
     */
    private Player createNewPlayer(CS_player_login request) {
        Player player = new Player();
        player.setUserid(request.getUserId());
        player.setServerindex(request.getServerIndex());
        player.setName(request.getName());
        player.setChannel(null);
        player.setPlatform(null);
        player.setDevicetype(null);
        player.setNetworktype(null);
        player.setSourceid(null);
        player.setDeviceversion(request.getClientVersion());
        player.setMacaddr(request.getDeviceId());
        player.setLanguage("zh_CN"); // 默认语言
        player.setCreatetime(System.currentTimeMillis());
        player.setLogintime(System.currentTimeMillis());
        player.setLogouttime(0L);
        player.setGm(0);
        player.setTotalcharge(0L);
        player.setTotalprice(java.math.BigDecimal.ZERO);
        player.setPaygroupids("");
        player.setPaylimit("");
        player.setFirstbuy(0);
        player.setSecondbuy(0);
        player.setThirdbuy(0);
        player.setBuytime(0L);
        player.setPushset("");
        player.setState("online");
        player.setCreatenum(0);
        
        // 生成唯一的playerid（使用雪花算法或类似方法）
        player.setPlayerid(IdGenerator.getInstance().generateId());
        
        return player;
    }

    /**
     * 处理用户登出请求
     *
     * @param request 登出请求消息
     * @return 登出响应消息
     */
    public LogoutResponse handleLogout(LogoutRequest request) {
        logger.info("Processing logout request for userId: {}", request.getUserId());

        // 注意：心跳注销现在通过PlayerSessionManager的unbindPlayerSession方法自动完成
        // 当调用unbindPlayerSession时，会触发GameEventManager中的handlePlayerUnregistered事件
        // 该事件会调用HeartBeatManager.unregisterPlayerHeartbeat方法

        // TODO: 实现实际的登出逻辑，如更新玩家状态、记录登出时间等

        LogoutResponse response = LogoutResponse.newBuilder()
                .setStatus(ResponseStatus.SUCCESS)
                .setMessage("Logout successful")
                .build();

        logger.info("Logout processed successfully for userId: {}", request.getUserId());
        return response;
    }

    /**
     * 执行所有玩家登录后处理逻辑
     *
     * @param player 登录的玩家对象
     */
    public void executeLoginHandlers(Player player) {
        if (playerLoginHandlers.isEmpty()) {
            logger.debug("没有找到玩家登录后处理器");
            return;
        }

        logger.info("开始执行 {} 个玩家登录后处理器", playerLoginHandlers.size());

        for (IPlayerLogin handler : playerLoginHandlers) {
            try {
                long startTime = System.currentTimeMillis();
                handler.login(player);
                long endTime = System.currentTimeMillis();
                logger.debug("执行玩家登录后处理器 {} 完成，耗时 {} ms",
                        handler.getClass().getSimpleName(), (endTime - startTime));
            } catch (Exception e) {
                logger.error("执行玩家登录后处理器 {} 时发生错误", handler.getClass().getSimpleName(), e);
            }
        }

        logger.info("玩家登录后处理器执行完成");
    }
}