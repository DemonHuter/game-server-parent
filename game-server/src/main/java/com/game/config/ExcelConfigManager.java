package com.game.config;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 添加导入ExcelUtils
import com.game.common.util.ExcelUtils;

/**
 * Excel配置数据管理器
 * 负责将Excel配置文件转换为内存数据对象
 * 支持热加载和动态更新
 */
@Component
public class ExcelConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ExcelConfigManager.class);

    // 单例实例
    private static volatile ExcelConfigManager instance;
    // 存储所有配置数据的映射表
    private final Map<String, List<Object>> configDataMap = new ConcurrentHashMap<>();
    // 存储配置类的映射表
    private final Map<String, Class<?>> configClassMap = new ConcurrentHashMap<>();
    // 配置文件路径
    @Value("${game.config.path:config}")
    private String configPath;

    /**
     * 加载单个配置文件
     *
     * @param excelFile Excel文件
     */
    public void loadConfig(File excelFile) {
        try {
            loadConfigDataFromFile(excelFile);
        } catch (Exception e) {
            logger.error("Failed to load config data from file: {}", excelFile.getName(), e);
        }
    }

    /**
     * 从Excel文件加载配置数据
     *
     * @param excelFile Excel文件
     * @throws IOException            IO异常
     * @throws IllegalAccessException 非法访问异常
     * @throws InstantiationException 实例化异常
     */
    private void loadConfigDataFromFile(File excelFile)
            throws IOException, IllegalAccessException, InstantiationException {
        String fileName = excelFile.getName();
        String configName = fileName.substring(0, fileName.lastIndexOf('.'));

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 只处理第一个工作表
            if (sheet == null) {
                logger.warn("Excel file is empty: {}", fileName);
                return;
            }

            // 获取字段配置信息
            Map<String, ExcelUtils.FieldConfig> fieldConfigs = parseFieldConfig(sheet); // 使用公共的FieldConfig

            // 动态创建配置类
            Class<?> configClass = createConfigClass(configName, fieldConfigs);
            configClassMap.put(configName, configClass);

            // 获取数据行并转换为对象
            List<Object> configData = parseDataRows(sheet, configClass, fieldConfigs);
            configDataMap.put(configName, configData);

            logger.info("Successfully loaded {} records from {}", configData.size(), fileName);
        }
    }

    /**
     * 解析字段配置信息
     *
     * @param sheet Excel工作表
     * @return 字段配置映射
     */
    private Map<String, ExcelUtils.FieldConfig> parseFieldConfig(Sheet sheet) { // 使用公共的FieldConfig
        Map<String, ExcelUtils.FieldConfig> fieldConfigs = new HashMap<>(); // 使用公共的FieldConfig

        // 获取标题行（第3行，索引2）
        Row headerRow = sheet.getRow(2);
        if (headerRow == null) {
            return fieldConfigs;
        }

        // 获取服务端标记行（第4行，索引3）
        Row serverFlagRow = sheet.getRow(3);
        if (serverFlagRow == null) {
            return fieldConfigs;
        }

        // 获取主键标记行（第4行，索引3，查找包含k的字段）
        // 获取类型行（第5行，索引4）
        Row typeRow = sheet.getRow(4);
        if (typeRow == null) {
            return fieldConfigs;
        }

        // 遍历所有列
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell headerCell = headerRow.getCell(i);
            Cell serverFlagCell = serverFlagRow.getCell(i);
            Cell typeCell = typeRow.getCell(i);

            if (headerCell == null) {
                continue;
            }

            String fieldName = ExcelUtils.getCellValueAsString(headerCell); // 使用工具类方法
            if (fieldName == null || fieldName.trim().isEmpty()) {
                continue;
            }

            // 检查是否为服务端字段（标记为's'）
            String serverFlag = ExcelUtils.getCellValueAsString(serverFlagCell); // 使用工具类方法
            if (serverFlag == null || !serverFlag.toLowerCase().contains("s")) {
                continue;
            }

            // 获取字段类型
            String fieldType = ExcelUtils.getCellValueAsString(typeCell); // 使用工具类方法
            if (fieldType == null || fieldType.trim().isEmpty()) {
                continue;
            }

            // 检查是否为主键字段（标记为'k'）
            boolean isPrimaryKey = serverFlag.toLowerCase().contains("k");

            fieldConfigs.put(fieldName, new ExcelUtils.FieldConfig(fieldName, fieldType, isPrimaryKey)); // 使用公共的FieldConfig
        }

        return fieldConfigs;
    }

    /**
     * 动态创建配置类
     *
     * @param configName   配置名称
     * @param fieldConfigs 字段配置映射
     * @return 配置类
     */
    private Class<?> createConfigClass(String configName, Map<String, ExcelUtils.FieldConfig> fieldConfigs) { // 使用公共的FieldConfig
        // 在实际项目中，我们可以通过字节码技术动态生成类
        // 这里简化处理，假设已经存在对应的类
        try {
            return Class.forName("com.game.config.data." + configName);
        } catch (ClassNotFoundException e) {
            logger.warn("Config class not found: {}, using generic map-based approach", configName);
            return null;
        }
    }

    /**
     * 解析数据行并转换为Java对象
     *
     * @param sheet        Excel工作表
     * @param configClass  配置类
     * @param fieldConfigs 字段配置映射
     * @return Java对象列表
     * @throws IllegalAccessException 非法访问异常
     * @throws InstantiationException 实例化异常
     */
    private List<Object> parseDataRows(Sheet sheet, Class<?> configClass, Map<String, ExcelUtils.FieldConfig> fieldConfigs) // 使用公共的FieldConfig
            throws IllegalAccessException, InstantiationException {
        List<Object> result = new ArrayList<>();

        // 从第6行开始是数据行（索引5）
        for (int i = 5; i <= sheet.getLastRowNum(); i++) {
            Row dataRow = sheet.getRow(i);
            if (dataRow == null) {
                continue;
            }

            // 检查是否为空行
            boolean isEmptyRow = true;
            for (int j = 0; j < dataRow.getLastCellNum(); j++) {
                Cell cell = dataRow.getCell(j);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    isEmptyRow = false;
                    break;
                }
            }

            if (isEmptyRow) {
                continue;
            }

            // 如果没有配置类，使用Map存储数据
            if (configClass == null) {
                Map<String, Object> dataMap = new HashMap<>();

                // 获取标题行用于字段映射
                Row headerRow = sheet.getRow(2);
                if (headerRow == null) {
                    continue;
                }

                // 遍历所有列并设置字段值
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    Cell headerCell = headerRow.getCell(j);
                    if (headerCell == null) {
                        continue;
                    }

                    String fieldName = ExcelUtils.getCellValueAsString(headerCell); // 使用工具类方法
                    if (fieldName == null || fieldName.trim().isEmpty()) {
                        continue;
                    }

                    // 检查是否为服务端字段
                    if (!fieldConfigs.containsKey(fieldName)) {
                        continue;
                    }

                    // 获取数据单元格
                    Cell dataCell = dataRow.getCell(j);
                    if (dataCell == null) {
                        continue;
                    }

                    // 获取字段配置
                    ExcelUtils.FieldConfig fieldConfig = fieldConfigs.get(fieldName); // 使用公共的FieldConfig

                    // 设置字段值
                    Object value = ExcelUtils.getCellValueByType(dataCell, fieldConfig.getType()); // 使用工具类方法
                    dataMap.put(fieldName, value);
                }

                result.add(dataMap);
            } else {
                // 创建对象实例
                Object obj = configClass.newInstance();

                // 获取标题行用于字段映射
                Row headerRow = sheet.getRow(2);
                if (headerRow == null) {
                    continue;
                }

                // 遍历所有列并设置字段值
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    Cell headerCell = headerRow.getCell(j);
                    if (headerCell == null) {
                        continue;
                    }

                    String fieldName = ExcelUtils.getCellValueAsString(headerCell); // 使用工具类方法
                    if (fieldName == null || fieldName.trim().isEmpty()) {
                        continue;
                    }

                    // 检查是否为服务端字段
                    if (!fieldConfigs.containsKey(fieldName)) {
                        continue;
                    }

                    // 获取数据单元格
                    Cell dataCell = dataRow.getCell(j);
                    if (dataCell == null) {
                        continue;
                    }

                    // 获取字段配置
                    ExcelUtils.FieldConfig fieldConfig = fieldConfigs.get(fieldName); // 使用公共的FieldConfig

                    // 设置字段值
                    setFieldValue(obj, fieldName, dataCell, fieldConfig);
                }

                result.add(obj);
            }
        }

        return result;
    }

    /**
     * 设置对象字段值
     *
     * @param obj         对象实例
     * @param fieldName   字段名
     * @param cell        Excel单元格
     * @param fieldConfig 字段配置
     * @throws IllegalAccessException 非法访问异常
     */
    private void setFieldValue(Object obj, String fieldName, Cell cell, ExcelUtils.FieldConfig fieldConfig) // 使用公共的FieldConfig
            throws IllegalAccessException {
        try {
            // 获取对象的字段
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            // 根据字段类型设置值
            String fieldType = fieldConfig.getType();
            Object value = ExcelUtils.getCellValueByType(cell, fieldType); // 使用工具类方法

            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            logger.warn("Field not found in class: {} -> {}", obj.getClass().getSimpleName(), fieldName);
        }
    }

    /**
     * 获取指定配置的所有数据
     *
     * @param configName 配置名称
     * @return 配置数据列表
     */
    public List<Object> getConfigData(String configName) {
        return configDataMap.getOrDefault(configName, new ArrayList<>());
    }

    /**
     * 根据实体类型返回这个配置所有List数据
     *
     * @param entityType 实体类型（对应Excel文件名，如"AdditionConfig"）
     * @param <T>        实体类型
     * @return 配置数据列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getAllConfigData(String entityType) {
        List<Object> dataList = configDataMap.getOrDefault(entityType, new ArrayList<>());
        return (List<T>) dataList;
    }

    /**
     * 获取指定配置的指定ID数据
     *
     * @param configName 配置名称
     * @param id         数据ID
     * @return 配置数据对象
     */
    public Object getConfigDataById(String configName, Object id) {
        List<Object> dataList = getConfigData(configName);
        for (Object data : dataList) {
            if (data instanceof Map) {
                Map<?, ?> dataMap = (Map<?, ?>) data;
                if (dataMap.containsKey("id") && id.equals(dataMap.get("id"))) {
                    return data;
                }
            } else {
                // 尝试通过反射获取ID字段
                try {
                    Method getIdMethod = data.getClass().getMethod("getId");
                    if (id.equals(getIdMethod.invoke(data))) {
                        return data;
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
        return null;
    }

    /**
     * 根据条件过滤配置数据
     *
     * @param configName 配置名称
     * @param filter     过滤条件
     * @return 过滤后的配置数据列表
     */
    public List<Object> getConfigDataByFilter(String configName, java.util.function.Predicate<Object> filter) {
        List<Object> dataList = getConfigData(configName);
        List<Object> result = new ArrayList<>();
        for (Object data : dataList) {
            if (filter.test(data)) {
                result.add(data);
            }
        }
        return result;
    }

    /**
     * 重新加载指定配置文件
     *
     * @param configFileName 配置文件名
     */
    public void reloadConfig(String configFileName) {
        // 使用配置路径而不是写死的game-server路径
        File configFile = new File(configPath, configFileName);
        if (!configFile.exists()) {
            logger.warn("Config file not found: {}", configFile.getAbsolutePath());
            return;
        }

        try {
            // 移除旧的配置数据
            String configName = configFileName.substring(0, configFileName.lastIndexOf('.'));
            configDataMap.remove(configName);
            configClassMap.remove(configName);

            // 重新加载配置数据
            loadConfigDataFromFile(configFile);
            logger.info("Reloaded config: {}", configFileName);
        } catch (Exception e) {
            logger.error("Failed to reload config: {}", configFileName, e);
        }
    }

    /**
     * 获取所有配置名称
     *
     * @return 配置名称列表
     */
    public Set<String> getAllConfigNames() {
        return configDataMap.keySet();
    }

    /**
     * 获取配置数据大小
     *
     * @param configName 配置名称
     * @return 配置数据大小
     */
    public int getConfigDataSize(String configName) {
        List<Object> dataList = getConfigData(configName);
        return dataList.size();
    }
}