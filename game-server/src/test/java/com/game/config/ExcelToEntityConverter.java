package com.game.config;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// 添加导入ExcelUtils和ConfigUtils
import com.game.common.util.ExcelUtils;
import com.game.common.util.ConfigUtils;

/**
 * Excel到Java实体类转换器
 * 支持将Excel配置文件转换为Java实体类
 * 遵循项目规范，支持Excel配置规则：
 * 1. 第四行标记为's'（服务端）的字段才需要生成Java字段
 * 2. 第五行的类型需转换为对应Java类型
 * 3. List类型的值使用{}来配置
 */
@Component
public class ExcelToEntityConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelToEntityConverter.class);
    
    /**
     * 将Excel文件转换为Java实体类
     * 
     * @param excelFile Excel文件
     * @param packageName 包名
     * @param outputPath 输出路径
     * @throws IOException IO异常
     */
    public static void convertExcelToEntity(File excelFile, String packageName, String outputPath) throws IOException {
        String fileName = excelFile.getName();
        String entityName = fileName.substring(0, fileName.lastIndexOf('.'));
        // 首字母大写
        entityName = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);
        
        logger.info("Converting Excel file {} to entity class {}", fileName, entityName);
        
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0); // 只处理第一个工作表
            if (sheet == null) {
                logger.warn("Excel file is empty: {}", fileName);
                return;
            }
            
            // 获取字段配置信息
            Map<String, ExcelUtils.FieldConfig> fieldConfigs = parseFieldConfig(sheet); // 使用公共的FieldConfig
            
            // 生成实体类代码
            String entityCode = generateEntityClass(entityName, packageName, fieldConfigs);
            
            // 写入文件
            writeToFile(outputPath + "/" + entityName + ".java", entityCode);
            
            logger.info("Successfully generated entity class: {}", entityName);
        }
    }
    
    /**
     * 直接调用方法，将Excel文件转换为Java实体类
     * 
     * @param excelFilePath Excel文件路径
     * @param packageName 包名
     * @param outputPath 输出路径
     * @throws IOException IO异常
     */
    public void convertExcelToEntity(String excelFilePath, String packageName, String outputPath) throws IOException {
        File excelFile = new File(excelFilePath);
        convertExcelToEntity(excelFile, packageName, outputPath);
    }
    
    /**
     * 解析字段配置信息
     * 
     * @param sheet Excel工作表
     * @return 字段配置映射
     */
    private static Map<String, ExcelUtils.FieldConfig> parseFieldConfig(Sheet sheet) { // 使用公共的FieldConfig
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
            
            // 保持原始字段名，除非需要转换为驼峰命名
            // fieldName = toCamelCase(fieldName);
            
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
     * 生成实体类代码
     * 
     * @param entityName 实体类名
     * @param packageName 包名
     * @param fieldConfigs 字段配置映射
     * @return 实体类代码
     */
    private static String generateEntityClass(String entityName, String packageName, Map<String, ExcelUtils.FieldConfig> fieldConfigs) { // 使用公共的FieldConfig
        StringBuilder sb = new StringBuilder();
        
        // 包声明
        sb.append("package ").append(packageName).append(";\n\n");
        
        // 导入
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.util.*;\n");
        sb.append("import java.math.BigDecimal;\n\n");
        
        // 类注释
        sb.append("/**\n");
        sb.append(" * ").append(entityName).append("配置实体类\n");
        sb.append(" * 自动生成的配置实体类，请勿手动修改\n");
        sb.append(" */\n");
        
        // 类声明
        sb.append("public class ").append(entityName).append(" implements Serializable {\n\n");
        
        // 序列化ID
        sb.append("    private static final long serialVersionUID = 1L;\n\n");
        
        // 字段声明
        for (ExcelUtils.FieldConfig fieldConfig : fieldConfigs.values()) { // 使用公共的FieldConfig
            String javaType = mapToJavaType(fieldConfig.getType());
            sb.append("    /** ").append(fieldConfig.getType()).append(" */\n");
            sb.append("    private ").append(javaType).append(" ").append(fieldConfig.getName()).append(";\n\n");
        }
        
        // 无参构造函数
        sb.append("    public ").append(entityName).append("() {}\n\n");
        
        // Getter和Setter方法
        for (ExcelUtils.FieldConfig fieldConfig : fieldConfigs.values()) { // 使用公共的FieldConfig
            String javaType = mapToJavaType(fieldConfig.getType());
            String capitalFieldName = capitalize(fieldConfig.getName());
            
            // Getter
            sb.append("    public ").append(javaType).append(" get").append(capitalFieldName).append("() {\n");
            sb.append("        return ").append(fieldConfig.getName()).append(";\n");
            sb.append("    }\n\n");
            
            // Setter
            sb.append("    public void set").append(capitalFieldName).append("(").append(javaType).append(" ").append(fieldConfig.getName()).append(") {\n");
            sb.append("        this.").append(fieldConfig.getName()).append(" = ").append(fieldConfig.getName()).append(";\n");
            sb.append("    }\n\n");
        }
        
        // toString方法
        sb.append("    @Override\n");
        sb.append("    public String toString() {\n");
        sb.append("        return \"").append(entityName).append("{\" +\n");
        
        boolean first = true;
        for (ExcelUtils.FieldConfig fieldConfig : fieldConfigs.values()) { // 使用公共的FieldConfig
            if (!first) {
                sb.append("                \", ");
            } else {
                sb.append("                \"");
            }
            
            sb.append(fieldConfig.getName()).append("=\" + ");
            sb.append(fieldConfig.getName()).append(" +\n");
            first = false;
        }
        
        sb.append("                '}';\n");
        sb.append("    }\n\n");
        
        // equals和hashCode方法
        sb.append("    @Override\n");
        sb.append("    public boolean equals(Object o) {\n");
        sb.append("        if (this == o) return true;\n");
        sb.append("        if (o == null || getClass() != o.getClass()) return false;\n");
        sb.append("        ").append(entityName).append(" that = (").append(entityName).append(") o;\n");
        sb.append("        return ");
        
        first = true;
        for (ExcelUtils.FieldConfig fieldConfig : fieldConfigs.values()) { // 使用公共的FieldConfig
            if (!first) {
                sb.append(" &&\n                ");
            }
            
            if (isPrimitiveType(fieldConfig.getType())) {
                sb.append("Objects.equals(").append(fieldConfig.getName()).append(", that.").append(fieldConfig.getName()).append(")");
            } else {
                sb.append(fieldConfig.getName()).append(" == that.").append(fieldConfig.getName());
            }
            
            first = false;
        }
        
        if (first) {
            sb.append("true");
        }
        
        sb.append(";\n");
        sb.append("    }\n\n");
        
        sb.append("    @Override\n");
        sb.append("    public int hashCode() {\n");
        sb.append("        return Objects.hash(");
        
        first = true;
        for (ExcelUtils.FieldConfig fieldConfig : fieldConfigs.values()) { // 使用公共的FieldConfig
            if (!first) {
                sb.append(", ");
            }
            sb.append(fieldConfig.getName());
            first = false;
        }
        
        sb.append(");\n");
        sb.append("    }\n");
        
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * 映射Excel类型到Java类型
     * 
     * @param excelType Excel类型
     * @return Java类型
     */
    private static String mapToJavaType(String excelType) {
        // 处理Map类型
        if (excelType.contains("Map")) {
            if (excelType.contains("intIntMap")) {
                return "Map<Integer, Integer>";
            } else if (excelType.contains("intStringMap")) {
                return "Map<Integer, String>";
            } else if (excelType.contains("stringIntMap")) {
                return "Map<String, Integer>";
            } else if (excelType.contains("longLongMap")) {
                return "Map<Long, Long>";
            } else if (excelType.contains("stringStringMap")) {
                return "Map<String, String>";
            } else {
                return "Map<String, String>";
            }
        }
        
        // 处理List类型
        if (excelType.contains("List")) {
            // 二维List类型
            if (excelType.contains("intList2") || excelType.contains("integerList2")) {
                return "List<List<Integer>>";
            } else if (excelType.contains("longList2")) {
                return "List<List<Long>>";
            } else if (excelType.contains("floatList2")) {
                return "List<List<Float>>";
            } else if (excelType.contains("doubleList2")) {
                return "List<List<Double>>";
            } else if (excelType.contains("stringList2")) {
                return "List<List<String>>";
            } else if (excelType.contains("booleanList2")) {
                return "List<List<Boolean>>";
            }
            // 一维List类型
            else if (excelType.contains("intList") || excelType.contains("integerList")) {
                return "List<Integer>";
            } else if (excelType.contains("longList")) {
                return "List<Long>";
            } else if (excelType.contains("floatList")) {
                return "List<Float>";
            } else if (excelType.contains("doubleList")) {
                return "List<Double>";
            } else if (excelType.contains("stringList")) {
                return "List<String>";
            } else if (excelType.contains("booleanList")) {
                return "List<Boolean>";
            } else {
                return "List<Object>";
            }
        }
        
        // 处理基本类型
        switch (excelType.toLowerCase()) {
            case "int":
            case "integer":
                return "Integer";
            case "long":
                return "Long";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "boolean":
                return "Boolean";
            case "string":
                return "String";
            case "bigdecimal":
                return "BigDecimal";
            case "date":
                return "java.util.Date";
            default:
                return "String";
        }
    }
    
    /**
     * 判断是否为基本类型
     * 
     * @param excelType Excel类型
     * @return 是否为基本类型
     */
    private static boolean isPrimitiveType(String excelType) {
        String lowerType = excelType.toLowerCase();
        return "int".equals(lowerType) || "integer".equals(lowerType) || 
               "long".equals(lowerType) || "float".equals(lowerType) || 
               "double".equals(lowerType) || "boolean".equals(lowerType) || 
               "string".equals(lowerType) || "bigdecimal".equals(lowerType) ||
               "date".equals(lowerType);
    }
    
    /**
     * 转换为驼峰命名
     * 
     * @param str 字符串
     * @return 驼峰命名
     */
    private static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        
        for (char c : str.toCharArray()) {
            if (c == '_' || c == ' ') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    
    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写字符串
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    /**
     * 写入文件
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     * @throws IOException IO异常
     */
    private static void writeToFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        
        logger.debug("Generated file: {}", filePath);
    }

    public static void generateExcel(String excelFileName) {
        // 默认参数
        String packageName = "com.game.config.data";
        
        // 从配置文件中读取game-server目录路径
        String gameServerPath = ConfigUtils.getGameServerPath();
        String outputPath = gameServerPath + "src/main/java/com/game/config/data";
        String excelFilePath = gameServerPath + "config/" + excelFileName;

        try {
            System.out.println("Excel Entity Generator");
            System.out.println("=====================");
            System.out.println("Excel文件: " + excelFilePath);
            System.out.println("包名: " + packageName);
            System.out.println("输出路径: " + outputPath);
            System.out.println();

            // 检查Excel文件是否存在
            File excelFile = new File(excelFilePath);
            if (!excelFile.exists()) {
                System.err.println("错误: Excel文件不存在: " + excelFilePath);
                System.exit(1);
                return;
            }

            // 检查是否为Excel文件
            if (!excelFileName.toLowerCase().endsWith(".xlsx")) {
                System.err.println("错误: 文件不是Excel文件(.xlsx): " + excelFileName);
                System.exit(1);
                return;
            }

            // 创建输出目录
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    System.err.println("错误: 无法创建输出目录: " + outputPath);
                    System.exit(1);
                    return;
                }
            }

            // 生成实体类
            convertExcelToEntity(excelFile, packageName, outputPath);

            System.out.println("实体类生成成功!");

            // 提示生成的文件名
            String fileName = excelFile.getName();
            String entityName = fileName.substring(0, fileName.lastIndexOf('.'));
            // 首字母大写
            entityName = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);

            System.out.println("生成的实体类: " + outputPath + "/" + entityName + ".java");
        } catch (Exception e) {
            logger.error("生成实体类失败", e);
            System.err.println("错误: " + e.getMessage());
            System.exit(1);
        }
    }
}