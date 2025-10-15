package com.game.model.config;

import com.game.config.ExcelConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配置管理器
 * 提供查询Excel配置数据的方法
 */
@Component
public class ConfigManager {
    @Autowired
    private ExcelConfigManager excelConfigManager;
    
    /**
     * 根据实体类型和主键查询某一个配置中的某一条数据
     * 
     * @param entityClass 实体类（对应Excel文件名，如AdditionConfig.class）
     * @param id 主键值
     * @param <T> 实体类型
     * @return 配置数据实体
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigDataById(Class<T> entityClass, Object id) {
        String entityType = entityClass.getSimpleName();
        return (T) excelConfigManager.getConfigDataById(entityType, id);
    }
    
    /**
     * 根据实体类型返回这个配置所有List数据
     * 
     * @param entityClass 实体类（对应Excel文件名，如AdditionConfig.class）
     * @param <T> 实体类型
     * @return 配置数据列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getAllConfigData(Class<T> entityClass) {
        String entityType = entityClass.getSimpleName();
        return (List<T>) excelConfigManager.getAllConfigData(entityType);
    }
    
    /**
     * 获取配置数据的大小
     * 
     * @param entityClass 实体类（对应Excel文件名，如AdditionConfig.class）
     * @return 配置数据数量
     */
    public int getConfigDataSize(Class<?> entityClass) {
        String entityType = entityClass.getSimpleName();
        return excelConfigManager.getConfigDataSize(entityType);
    }
}