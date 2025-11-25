package com.game.common.util;

import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
        logger.warn("未找到game.server.path配置项，使用默认路径: ./");
        return "./";
    }
    
    /**
     * 获取game.config.path配置项，并智能检测config目录位置
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
                        String configPath = (String) configConfig.get("path");
                        
                        // 智能检测config目录位置
                        // 1. 首先检查当前目录下是否存在config目录
                        File currentConfigDir = new File("config");
                        if (currentConfigDir.exists() && currentConfigDir.isDirectory()) {
                            logger.info("在当前目录找到config目录: {}", currentConfigDir.getAbsolutePath());
                            return "config";
                        }
                        
                        // 2. 检查game-server/config目录（开发环境）
                        File devConfigDir = new File("game-server/config");
                        if (devConfigDir.exists() && devConfigDir.isDirectory()) {
                            logger.info("在game-server目录找到config目录: {}", devConfigDir.getAbsolutePath());
                            return "game-server/config";
                        }
                        
                        // 3. 如果当前目录没有config目录，检查上级目录
                        File parentConfigDir = new File("../config");
                        if (parentConfigDir.exists() && parentConfigDir.isDirectory()) {
                            logger.info("在上级目录找到config目录: {}", parentConfigDir.getAbsolutePath());
                            return "../config";
                        }
                        
                        // 4. 返回配置文件中的路径
                        return configPath;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取game.config.path配置项失败", e);
        }
        
        // 返回默认路径
        logger.warn("未找到game.config.path配置项，使用默认路径: config");
        return "config";
    }
}