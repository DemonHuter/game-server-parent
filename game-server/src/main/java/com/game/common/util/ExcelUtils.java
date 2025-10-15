package com.game.common.util;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Excel工具类
 * 提供Excel处理相关的通用方法
 */
public class ExcelUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
    
    /**
     * 获取单元格值为字符串
     * 
     * @param cell Excel单元格
     * @return 字符串值
     */
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    return new BigDecimal(cell.getNumericCellValue()).toPlainString();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 获取单元格值为整数
     * 
     * @param cell Excel单元格
     * @return 整数值
     */
    public static Integer getCellAsInteger(Cell cell) {
        if (cell == null) {
            return 0;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }
    
    /**
     * 获取单元格值为长整数
     * 
     * @param cell Excel单元格
     * @return 长整数值
     */
    public static Long getCellAsLong(Cell cell) {
        if (cell == null) {
            return 0L;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    return Long.parseLong(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0L;
                }
            default:
                return 0L;
        }
    }
    
    /**
     * 获取单元格值为浮点数
     * 
     * @param cell Excel单元格
     * @return 浮点数值
     */
    public static Float getCellAsFloat(Cell cell) {
        if (cell == null) {
            return 0.0f;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (float) cell.getNumericCellValue();
            case STRING:
                try {
                    return Float.parseFloat(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0f;
                }
            default:
                return 0.0f;
        }
    }
    
    /**
     * 获取单元格值为双精度浮点数
     * 
     * @param cell Excel单元格
     * @return 双精度浮点数值
     */
    public static Double getCellAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
    
    /**
     * 获取单元格值为布尔值
     * 
     * @param cell Excel单元格
     * @return 布尔值
     */
    public static Boolean getCellAsBoolean(Cell cell) {
        if (cell == null) {
            return false;
        }
        
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                return "true".equalsIgnoreCase(cell.getStringCellValue());
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            default:
                return false;
        }
    }
    
    /**
     * 获取单元格值为BigDecimal
     * 
     * @param cell Excel单元格
     * @return BigDecimal值
     */
    public static BigDecimal getCellAsBigDecimal(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return new BigDecimal(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取单元格值为日期
     * 
     * @param cell Excel单元格
     * @return 日期值
     */
    public static java.util.Date getCellAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    // 尝试将数字转换为日期
                    return new java.util.Date((long) cell.getNumericCellValue());
                }
            case STRING:
                try {
                    // 尝试解析字符串为日期
                    return new SimpleDateFormat("yyyy-MM-dd").parse(cell.getStringCellValue());
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    /**
     * 根据字段类型获取单元格值
     * 
     * @param cell Excel单元格
     * @param fieldType 字段类型
     * @return 转换后的值
     */
    public static Object getCellValueByType(Cell cell, String fieldType) {
        // 处理Map类型
        if (fieldType.contains("Map")) {
            return parseMapValue(cell, fieldType);
        }
        
        // 处理List类型
        if (fieldType.contains("List")) {
            return parseListValue(cell, fieldType);
        }
        
        // 处理基本类型
        switch (fieldType.toLowerCase()) {
            case "int":
            case "integer":
                return getCellAsInteger(cell);
            case "long":
                return getCellAsLong(cell);
            case "float":
                return getCellAsFloat(cell);
            case "double":
                return getCellAsDouble(cell);
            case "boolean":
                return getCellAsBoolean(cell);
            case "string":
                return getCellValueAsString(cell);
            case "bigdecimal":
                return getCellAsBigDecimal(cell);
            case "date":
                return getCellAsDate(cell);
            default:
                return getCellValueAsString(cell);
        }
    }
    
    /**
     * 解析Map类型的值
     * 支持格式：{key1#value1,key2#value2,key3#value3}
     * 
     * @param cell Excel单元格
     * @param fieldType 字段类型
     * @return Map对象
     */
    public static Object parseMapValue(Cell cell, String fieldType) {
        String cellValue = getCellValueAsString(cell);
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        // 移除大括号
        cellValue = cellValue.trim().replace("{", "").replace("}", "");
        
        Map<Object, Object> result = new HashMap<>();
        
        // 分割键值对
        String[] pairs = cellValue.split(",");
        
        // 根据Map键值类型转换值
        if (fieldType.contains("intIntMap")) {
            for (String pair : pairs) {
                String[] keyValue = pair.split("#");
                if (keyValue.length == 2) {
                    try {
                        Object key = Integer.parseInt(keyValue[0].trim());
                        Object value = Integer.parseInt(keyValue[1].trim());
                        result.put(key, value);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid intIntMap format: {}", pair);
                    }
                }
            }
        } else if (fieldType.contains("intStringMap")) {
            for (String pair : pairs) {
                String[] keyValue = pair.split("#");
                if (keyValue.length == 2) {
                    try {
                        Object key = Integer.parseInt(keyValue[0].trim());
                        Object value = keyValue[1].trim();
                        result.put(key, value);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid intStringMap format: {}", pair);
                    }
                }
            }
        } else if (fieldType.contains("stringIntMap")) {
            for (String pair : pairs) {
                String[] keyValue = pair.split("#");
                if (keyValue.length == 2) {
                    try {
                        Object key = keyValue[0].trim();
                        Object value = Integer.parseInt(keyValue[1].trim());
                        result.put(key, value);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid stringIntMap format: {}", pair);
                    }
                }
            }
        } else {
            // 默认作为字符串到字符串的Map处理
            for (String pair : pairs) {
                String[] keyValue = pair.split("#");
                if (keyValue.length == 2) {
                    Object key = keyValue[0].trim();
                    Object value = keyValue[1].trim();
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 解析List类型的值
     * 支持格式：{value1,value2,value3}
     * 
     * @param cell Excel单元格
     * @param fieldType 字段类型
     * @return List对象
     */
    public static Object parseListValue(Cell cell, String fieldType) {
        String cellValue = getCellValueAsString(cell);
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 处理二维List类型（如stringList2）
        if (fieldType.contains("List2")) {
            return parseList2Value(cell, fieldType);
        }
        
        // 移除大括号
        cellValue = cellValue.trim().replace("{", "").replace("}", "");
        
        // 分割值
        String[] values = cellValue.split(",");
        List<Object> result = new ArrayList<>();
        
        // 根据List元素类型转换值
        if (fieldType.contains("intList") || fieldType.contains("integerList")) {
            for (String value : values) {
                try {
                    result.add(Integer.parseInt(value.trim()));
                } catch (NumberFormatException e) {
                    result.add(0);
                }
            }
        } else if (fieldType.contains("longList")) {
            for (String value : values) {
                try {
                    result.add(Long.parseLong(value.trim()));
                } catch (NumberFormatException e) {
                    result.add(0L);
                }
            }
        } else if (fieldType.contains("floatList")) {
            for (String value : values) {
                try {
                    result.add(Float.parseFloat(value.trim()));
                } catch (NumberFormatException e) {
                    result.add(0.0f);
                }
            }
        } else if (fieldType.contains("doubleList")) {
            for (String value : values) {
                try {
                    result.add(Double.parseDouble(value.trim()));
                } catch (NumberFormatException e) {
                    result.add(0.0);
                }
            }
        } else if (fieldType.contains("stringList")) {
            for (String value : values) {
                result.add(value.trim());
            }
        } else {
            // 默认作为字符串List处理
            for (String value : values) {
                result.add(value.trim());
            }
        }
        
        return result;
    }
    
    /**
     * 解析二维List类型的值
     * 支持格式：value1,value2}|{value3,value4}|{value5,value6
     * 
     * @param cell Excel单元格
     * @param fieldType 字段类型
     * @return 二维List对象
     */
    public static Object parseList2Value(Cell cell, String fieldType) {
        String cellValue = getCellValueAsString(cell);
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 分割二维数组
        String[] array2D = cellValue.split("\\|\\{");
        List<List<Object>> result = new ArrayList<>();
        
        for (int i = 0; i < array2D.length; i++) {
            String array1D = array2D[i];
            
            // 移除首尾的括号
            if (i == 0 && array1D.startsWith("{")) {
                array1D = array1D.substring(1);
            }
            if (i == array2D.length - 1 && array1D.endsWith("}")) {
                array1D = array1D.substring(0, array1D.length() - 1);
            }
            
            // 分割一维数组
            String[] values = array1D.split(",");
            List<Object> innerList = new ArrayList<>();
            
            // 根据List元素类型转换值
            if (fieldType.contains("intList2") || fieldType.contains("integerList2")) {
                for (String value : values) {
                    try {
                        innerList.add(Integer.parseInt(value.trim()));
                    } catch (NumberFormatException e) {
                        innerList.add(0);
                    }
                }
            } else if (fieldType.contains("longList2")) {
                for (String value : values) {
                    try {
                        innerList.add(Long.parseLong(value.trim()));
                    } catch (NumberFormatException e) {
                        innerList.add(0L);
                    }
                }
            } else if (fieldType.contains("floatList2")) {
                for (String value : values) {
                    try {
                        innerList.add(Float.parseFloat(value.trim()));
                    } catch (NumberFormatException e) {
                        innerList.add(0.0f);
                    }
                }
            } else if (fieldType.contains("doubleList2")) {
                for (String value : values) {
                    try {
                        innerList.add(Double.parseDouble(value.trim()));
                    } catch (NumberFormatException e) {
                        innerList.add(0.0);
                    }
                }
            } else if (fieldType.contains("stringList2")) {
                for (String value : values) {
                    innerList.add(value.trim());
                }
            } else {
                // 默认作为字符串List处理
                for (String value : values) {
                    innerList.add(value.trim());
                }
            }
            
            result.add(innerList);
        }
        
        return result;
    }
    
    /**
     * 字段配置类
     */
    public static class FieldConfig {
        private String name;
        private String type;
        private boolean isPrimaryKey;
        
        public FieldConfig(String name, String type, boolean isPrimaryKey) {
            this.name = name;
            this.type = type;
            this.isPrimaryKey = isPrimaryKey;
        }
        
        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
        
        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }
    }
}