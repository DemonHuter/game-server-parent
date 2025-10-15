package com.game.common.util;

import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * 配置文件工具类
 * 用于读取YML配置文件中的配置项
 */
public class ConfigUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    
    private static final String CONFIG_FILE = "application.yml";
    private static Map<String, Object> configMap;
    
    static {
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    @SuppressWarnings("unchecked")
    private static void loadConfig() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = ConfigUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (inputStream != null) {
                configMap = yaml.load(inputStream);
            } else {
                logger.warn("配置文件 {} 未找到，使用默认配置", CONFIG_FILE);
                configMap = new java.util.HashMap<>();
            }
        } catch (Exception e) {
            logger.error("加载配置文件失败", e);
            configMap = new java.util.HashMap<>();
        }
    }
    
    /**
     * 获取game.server.path配置项
     * 
     * @return game-server目录路径
     */
    public static String getGameServerPath() {
        try {
            if (configMap != null && configMap.containsKey("game")) {
                Map<String, Object> gameConfig = (Map<String, Object>) configMap.get("game");
                if (gameConfig.containsKey("server")) {
                    Map<String, Object> serverConfig = (Map<String, Object>) gameConfig.get("server");
                    if (serverConfig.containsKey("path")) {
                        String path = (String) serverConfig.get("path");
                        // 确保路径以"/"结尾
                        if (!path.endsWith("/")) {
                            path += "/";
                        }
                        return path;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取game.server.path配置项失败", e);
        }
        
        // 返回默认路径
        logger.warn("未找到game.server.path配置项，使用默认路径: game-server/");
        return "game-server/";
    }
    
    /**
     * 获取game.config.path配置项
     * 
     * @return 配置文件目录路径
     */
    public static String getGameConfigPath() {
        try {
            if (configMap != null && configMap.containsKey("game")) {
                Map<String, Object> gameConfig = (Map<String, Object>) configMap.get("game");
                if (gameConfig.containsKey("config")) {
                    Map<String, Object> configConfig = (Map<String, Object>) gameConfig.get("config");
                    if (configConfig.containsKey("path")) {
                        return (String) configConfig.get("path");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取game.config.path配置项失败", e);
        }
        
        // 返回默认路径
        logger.warn("未找到game.config.path配置项，使用默认路径: game-server/config");
        return "game-server/config";
    }
}