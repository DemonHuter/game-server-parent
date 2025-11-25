package com.game.init;

import com.game.config.ExcelConfigManager;
import com.game.constant.SystemInitializeOrder;
import com.game.model.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 配置初始化类
 * 在应用启动时读取config目录下的所有Excel文件并加载到内存中
 */
@Component
public class ConfigInitializer implements SystemInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigInitializer.class);

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private ExcelConfigManager excelConfigManager;

    @Override
    public int getOrder() {
        return SystemInitializeOrder.CONFIG;
    }

    /**
     * 应用启动时初始化所有配置
     */
    @Override
    public void initialize() throws Exception {
        logger.info("开始初始化Excel配置文件...");
        
        try {
            // 获取配置文件目录，使用ExcelConfigManager中注入的configPath
            // 智能检测config目录位置
            String configPath = excelConfigManager.getConfigPath();
            File configDir = new File(configPath);
            
            // 如果config目录不存在，尝试在当前目录下查找
            if (!configDir.exists() || !configDir.isDirectory()) {
                logger.warn("配置目录不存在: {}, 尝试在当前目录查找", configDir.getAbsolutePath());
                configDir = new File("config");
            }
            
            // 如果当前目录也没有，尝试在上级目录查找
            if ((!configDir.exists() || !configDir.isDirectory()) && !configPath.equals("../config")) {
                logger.warn("配置目录不存在: {}, 尝试在上级目录查找", configDir.getAbsolutePath());
                configDir = new File("../config");
            }
            
            if (!configDir.exists() || !configDir.isDirectory()) {
                logger.warn("配置目录不存在: {}", configDir.getAbsolutePath());
                return;
            }
            
            // 获取所有Excel文件
            File[] excelFiles = configDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".xlsx") || name.toLowerCase().endsWith(".xls"));
            
            if (excelFiles == null || excelFiles.length == 0) {
                logger.warn("配置目录中没有找到Excel文件: {}", configDir.getAbsolutePath());
                return;
            }
            
            // 加载每个Excel文件
            for (File excelFile : excelFiles) {
                excelConfigManager.loadConfig(excelFile);
            }
            
            logger.info("配置文件初始化完成，共加载了 {} 个配置文件", excelFiles.length);
        } catch (Exception e) {
            logger.error("初始化配置文件时发生错误", e);
            throw e;
        }
    }
}