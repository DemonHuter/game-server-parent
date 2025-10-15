package com.game.generator.util;

import com.game.generator.config.CodeGeneratorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 代码生成工具类
 * 自动读取数据库表结构，生成实体类和缓存类
 * 支持通过配置对象进行灵活配置
 */
public class CodeGeneratorUtils {

    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratorUtils.class);

    /**
     * 配置对象
     */
    private final CodeGeneratorConfig config;

    /**
     * 构造函数 - 使用配置对象
     */
    public CodeGeneratorUtils(CodeGeneratorConfig config) {
        this.config = config;
    }

    /**
     * 构造函数 - 兼容旧版本
     *
     * @deprecated 建议使用 CodeGeneratorUtils(CodeGeneratorConfig) 构造函数
     */
    @Deprecated
    public CodeGeneratorUtils(String jdbcUrl, String username, String password) {
        this.config = new CodeGeneratorConfig();
        this.config.getDatabase().setUrl(jdbcUrl);
        this.config.getDatabase().setUsername(username);
        this.config.getDatabase().setPassword(password);
    }

    /**
     * 生成所有表的代码
     */
    public void generateAll() {
        try {
            List<TableInfo> tables = loadTableStructures();

            for (TableInfo table : tables) {
                generateEntityClass(table);
                generateCacheClass(table);
                generateDaoInterface(table);
                logger.info("Generated code for table: {}", table.tableName);
            }

            logger.info("Code generation completed! Generated {} tables", tables.size());
        } catch (Exception e) {
            logger.error("Code generation failed", e);
            throw new RuntimeException("Code generation failed", e);
        }
    }

    /**
     * 加载数据库表结构
     */
    private List<TableInfo> loadTableStructures() throws SQLException {
        List<TableInfo> tables = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                config.getDatabase().getUrl(),
                config.getDatabase().getUsername(),
                config.getDatabase().getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            // 获取所有表
            try (ResultSet tableRs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tableRs.next()) {
                    String tableName = tableRs.getString("TABLE_NAME");

                    // 检查表是否应该被处理
                    if (shouldSkipTable(tableName)) {
                        continue;
                    }

                    TableInfo table = new TableInfo();
                    table.tableName = tableName;
                    table.className = toPascalCase(tableName);

                    // 获取主键信息
                    Set<String> primaryKeys = new HashSet<>();
                    try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, null, tableName)) {
                        while (pkRs.next()) {
                            primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                        }
                    }

                    // 获取列信息
                    try (ResultSet columnRs = metaData.getColumns(catalog, null, tableName, "%")) {
                        while (columnRs.next()) {
                            ColumnInfo column = new ColumnInfo();
                            column.columnName = columnRs.getString("COLUMN_NAME");
                            column.fieldName = toCamelCase(column.columnName);
                            column.jdbcType = columnRs.getString("TYPE_NAME");
                            column.javaType = mapJavaType(column.jdbcType);
                            column.isPrimaryKey = primaryKeys.contains(column.columnName);
                            column.isNullable = columnRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                            column.comment = columnRs.getString("REMARKS");

                            table.columns.add(column);

                            if (column.isPrimaryKey) {
                                table.primaryKey = column;
                            }
                        }
                    }

                    tables.add(table);
                }
            }
        }

        return tables;
    }

    /**
     * 检查表是否应该跳过
     */
    private boolean shouldSkipTable(String tableName) {
        // 跳过系统表
        if (isSystemTable(tableName)) {
            return true;
        }

        // 检查排除列表
        String excludeTables = config.getOptions().getExcludeTables();
        if (!excludeTables.isEmpty()) {
            String[] excludeArray = excludeTables.split(",");
            for (String exclude : excludeArray) {
                if (tableName.equalsIgnoreCase(exclude.trim())) {
                    return true;
                }
            }
        }

        // 检查包含列表
        String includeTables = config.getOptions().getIncludeTables();
        if (!includeTables.isEmpty()) {
            String[] includeArray = includeTables.split(",");
            boolean found = false;
            for (String include : includeArray) {
                if (tableName.equalsIgnoreCase(include.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }

        return false;
    }

    /**
     * 生成实体类
     */
    private void generateEntityClass(TableInfo table) throws IOException {
        StringBuilder sb = new StringBuilder();

        // 包声明
        sb.append("package ").append(config.getPackages().getEntity()).append(";\n\n");

        // 导入
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.util.Date;\n\n");

        // 类注释
        sb.append("/**\n");
        sb.append(" * ").append(table.className).append("实体类\n");
        sb.append(" * 表名: ").append(table.tableName).append("\n");
        sb.append(" * @author ").append(config.getOptions().getAuthor()).append("\n");
        sb.append(" * 自动生成，请勿手动修改\n");
        sb.append(" */\n");

        // 类声明
        sb.append("public class ").append(table.className).append(" extends BaseEntity {\n\n");

        // 字段声明（包括所有字段，不再跳过主键字段）
        for (ColumnInfo column : table.columns) {
            if (column.comment != null && !column.comment.trim().isEmpty()) {
                sb.append("    /** ").append(column.comment).append(" */\n");
            }
            sb.append("    private ").append(column.javaType).append(" ").append(column.fieldName).append(";\n\n");
        }

        // Getter和Setter方法（包括主键字段）
        for (ColumnInfo column : table.columns) {
            String capitalFieldName = capitalize(column.fieldName);

            // Getter
            sb.append("    public ").append(column.javaType).append(" get").append(capitalFieldName).append("() {\n");
            sb.append("        return ").append(column.fieldName).append(";\n");
            sb.append("    }\n\n");

            // Setter
            sb.append("    public void set").append(capitalFieldName).append("(").append(column.javaType).append(" ").append(column.fieldName).append(") {\n");
            sb.append("        this.").append(column.fieldName).append(" = ").append(column.fieldName).append(";\n");
            sb.append("    }\n\n");
        }

        // 实现getIdx方法
        sb.append("    @Override\n");
        sb.append("    public String getIdx() {\n");
        if (table.primaryKey != null) {
            // 如果主键是字符串类型，直接返回；否则转换为字符串
            if ("String".equals(table.primaryKey.javaType)) {
                sb.append("        return get").append(capitalize(table.primaryKey.fieldName)).append("();\n");
            } else {
                sb.append("        return String.valueOf(get").append(capitalize(table.primaryKey.fieldName)).append("());\n");
            }
        } else {
            // 如果没有主键字段，返回空字符串
            sb.append("        return \"\";\n");
        }
        sb.append("    }\n\n");

        // toString方法
        sb.append("    @Override\n");
        sb.append("    public String toString() {\n");
        sb.append("        return String.format(\"").append(table.className).append("{");
        boolean first = true;
        for (ColumnInfo column : table.columns) {
            if (!first) {
                sb.append(", ");
            }
            if (column.javaType.equals("String")) {
                sb.append(column.fieldName).append("='%s'");
            } else {
                sb.append(column.fieldName).append("=%s");
            }
            first = false;
        }
        sb.append("}\", \n                ");
        first = true;
        for (ColumnInfo column : table.columns) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("get").append(capitalize(column.fieldName)).append("()");
            first = false;
        }
        sb.append(");\n");
        sb.append("    }\n");

        sb.append("}\n");

        // 写入文件
        writeToFile(config.getPaths().getEntity() + table.className + ".java", sb.toString());
    }

    /**
     * 生成缓存类
     */
    private void generateCacheClass(TableInfo table) throws IOException {
        StringBuilder sb = new StringBuilder();

        String cacheClassName = table.className + "Cache";
        String daoClassName = table.className + "Dao";
        String entityClassName = table.className;

        // 包声明
        sb.append("package ").append(config.getPackages().getCache()).append(";\n\n");

        // 导入
        sb.append("import ").append(config.getPackages().getEntity()).append(".").append(entityClassName).append(";\n");
        sb.append("import ").append(config.getPackages().getDao()).append(".").append(daoClassName).append(";\n");
        sb.append("import org.springframework.stereotype.Component;\n\n");

        // 类注释
        sb.append("/**\n");
        sb.append(" * ").append(entityClassName).append("缓存类\n");
        sb.append(" * @author ").append(config.getOptions().getAuthor()).append("\n");
        sb.append(" * 自动生成，请勿手动修改\n");
        sb.append(" */\n");

        // 类声明
        sb.append("@Component\n");
        sb.append("public class ").append(cacheClassName).append(" extends BaseCache<").append(entityClassName).append("> {\n\n");

        // 构造函数
        sb.append("    private final ").append(daoClassName).append(" ").append(toCamelCase(daoClassName)).append(";\n\n");
        sb.append("    public ").append(cacheClassName).append("(").append(daoClassName).append(" ").append(toCamelCase(daoClassName)).append(") {\n");
        sb.append("        super(").append(toCamelCase(daoClassName)).append(");\n");
        sb.append("        this.").append(toCamelCase(daoClassName)).append(" = ").append(toCamelCase(daoClassName)).append(";\n");
        sb.append("    }\n");

        sb.append("}\n");

        // 写入文件
        writeToFile(config.getPaths().getCache() + cacheClassName + ".java", sb.toString());
    }

    /**
     * 生成DAO接口
     */
    private void generateDaoInterface(TableInfo table) throws IOException {
        StringBuilder sb = new StringBuilder();

        String daoClassName = table.className + "Dao";
        String entityClassName = table.className;

        // 包声明
        sb.append("package ").append(config.getPackages().getDao()).append(";\n\n");

        // 导入
        sb.append("import ").append(config.getPackages().getEntity()).append(".").append(entityClassName).append(";\n");
        sb.append("import org.apache.ibatis.annotations.Mapper;\n");
        sb.append("import org.apache.ibatis.annotations.Select;\n");
        sb.append("import org.apache.ibatis.annotations.Insert;\n");
        sb.append("import org.apache.ibatis.annotations.Update;\n");
        sb.append("import org.apache.ibatis.annotations.Delete;\n");
        sb.append("import org.apache.ibatis.annotations.Param;\n");
        sb.append("import org.apache.ibatis.annotations.Options;\n\n");

        // 接口注释
        sb.append("/**\n");
        sb.append(" * ").append(entityClassName).append("数据访问接口\n");
        sb.append(" * @author ").append(config.getOptions().getAuthor()).append("\n");
        sb.append(" * 自动生成，请勿手动修改\n");
        sb.append(" */\n");

        // 接口声明
        sb.append("@Mapper\n");
        sb.append("public interface ").append(daoClassName).append(" extends BaseDao<" + table.className + "> {\n\n");

        // 生成基于注解的SQL映射
        generateSelectByIdMethod(sb, table);
        generateSelectAllMethod(sb, table);
        generateInsertMethod(sb, table);
        generateUpdateMethod(sb, table);
        generateDeleteByIdMethod(sb, table);
        generateCountMethod(sb, table);

        sb.append("}\n");

        // 写入文件
        writeToFile(config.getPaths().getDao() + daoClassName + ".java", sb.toString());
    }

    /**
     * 生成selectById方法
     */
    private void generateSelectByIdMethod(StringBuilder sb, TableInfo table) {
        if (table.primaryKey != null) {
            sb.append("    @Select(\"SELECT * FROM ").append(table.tableName).append(" WHERE ").append(table.primaryKey.columnName).append(" = #{").append(table.primaryKey.fieldName).append("}\")\n");
            sb.append("    ").append(table.className).append(" selectById(@Param(\"").append(table.primaryKey.fieldName).append("\") ").append(table.primaryKey.javaType).append(" ").append(table.primaryKey.fieldName).append(");\n\n");
        } else {
            // 如果没有主键，使用id作为默认主键
            sb.append("    @Select(\"SELECT * FROM ").append(table.tableName).append(" WHERE id = #{id}\")\n");
            sb.append("    ").append(table.className).append(" selectById(@Param(\"id\") Long id);\n\n");
        }
    }

    /**
     * 生成selectAll方法
     */
    private void generateSelectAllMethod(StringBuilder sb, TableInfo table) {
        sb.append("    @Select(\"SELECT * FROM ").append(table.tableName).append("\")\n");
        sb.append("    java.util.List<").append(table.className).append("> selectAll();\n\n");
    }

    /**
     * 生成insert方法
     */
    private void generateInsertMethod(StringBuilder sb, TableInfo table) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder keyProperty = new StringBuilder();

        // 添加所有字段（严格按照数据库表字段）
        for (ColumnInfo column : table.columns) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(column.columnName);
            values.append("#{").append(column.fieldName).append("}");

            // 记录主键字段用于生成keyProperty
            if (column.isPrimaryKey) {
                keyProperty.append(column.fieldName);
            }
        }

        if (keyProperty.length() > 0) {
            sb.append("    @Insert(\"INSERT INTO ").append(table.tableName).append(" (").append(columns).append(") VALUES (").append(values).append(")\")\n");
            sb.append("    @Options(useGeneratedKeys = true, keyProperty = \"").append(keyProperty).append("\")\n");
            sb.append("    int insert(").append(table.className).append(" entity);\n\n");
        } else {
            sb.append("    @Insert(\"INSERT INTO ").append(table.tableName).append(" (").append(columns).append(") VALUES (").append(values).append(")\")\n");
            sb.append("    int insert(").append(table.className).append(" entity);\n\n");
        }
    }

    /**
     * 生成update方法
     */
    private void generateUpdateMethod(StringBuilder sb, TableInfo table) {
        StringBuilder updates = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();

        // 添加所有字段（严格按照数据库表字段）
        for (ColumnInfo column : table.columns) {
            if (column.isPrimaryKey) {
                whereClause.append(column.columnName).append(" = #{").append(column.fieldName).append("}");
            } else {
                if (updates.length() > 0) {
                    updates.append(", ");
                }
                updates.append(column.columnName).append(" = #{").append(column.fieldName).append("}");
            }
        }

        sb.append("    @Update(\"UPDATE ").append(table.tableName).append(" SET ").append(updates).append(" WHERE ").append(whereClause).append("\")\n");
        sb.append("    int update(").append(table.className).append(" entity);\n\n");
    }

    /**
     * 生成deleteById方法
     */
    private void generateDeleteByIdMethod(StringBuilder sb, TableInfo table) {
        if (table.primaryKey != null) {
            sb.append("    @Delete(\"DELETE FROM ").append(table.tableName).append(" WHERE ").append(table.primaryKey.columnName).append(" = #{").append(table.primaryKey.fieldName).append("}\")\n");
            sb.append("    int deleteById(@Param(\"").append(table.primaryKey.fieldName).append("\") ").append(table.primaryKey.javaType).append(" ").append(table.primaryKey.fieldName).append(");\n\n");
        } else {
            // 如果没有主键，使用id作为默认主键
            sb.append("    @Delete(\"DELETE FROM ").append(table.tableName).append(" WHERE id = #{id}\")\n");
            sb.append("    int deleteById(@Param(\"id\") Long id);\n\n");
        }
    }

    /**
     * 生成count方法
     */
    private void generateCountMethod(StringBuilder sb, TableInfo table) {
        sb.append("    @Select(\"SELECT COUNT(*) FROM ").append(table.tableName).append("\")\n");
        sb.append("    long count();\n\n");
    }

    /**
     * 工具方法
     */
    private boolean isSystemTable(String tableName) {
        String lowerName = tableName.toLowerCase();
        return lowerName.startsWith("sys_") ||
                lowerName.startsWith("information_schema") ||
                lowerName.startsWith("performance_schema") ||
                lowerName.startsWith("mysql");
    }

    private boolean isBaseEntityField(String fieldName) {
        return "id".equals(fieldName) ||
                "createTime".equals(fieldName) ||
                "updateTime".equals(fieldName);
    }

    private String toPascalCase(String str) {
        // 移除表前缀
        String tablePrefix = config.getOptions().getTablePrefix();
        if (!tablePrefix.isEmpty() && str.toLowerCase().startsWith(tablePrefix.toLowerCase())) {
            str = str.substring(tablePrefix.length());
        }

        return capitalize(toCamelCase(str));
    }

    private String toCamelCase(String str) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (char c : str.toCharArray()) {
            if (c == '_') {
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String mapJavaType(String jdbcType) {
        String upperType = jdbcType.toUpperCase();

        if (upperType.contains("INT")) {
            if (upperType.contains("BIGINT")) {
                return "Long";
            } else if (upperType.contains("TINYINT")) {
                return "Boolean";  // 通常用于布尔值
            } else {
                return "Integer";
            }
        } else if (upperType.contains("VARCHAR") || upperType.contains("TEXT") || upperType.contains("CHAR")) {
            return "String";
        } else if (upperType.contains("DECIMAL") || upperType.contains("NUMERIC")) {
            return "java.math.BigDecimal";
        } else if (upperType.contains("FLOAT")) {
            return "Float";
        } else if (upperType.contains("DOUBLE")) {
            return "Double";
        } else if (upperType.contains("DATE") || upperType.contains("TIME")) {
            return "java.util.Date";
        } else if (upperType.contains("BLOB")) {
            return "byte[]";
        } else {
            return "String"; // 默认类型
        }
    }

    private void writeToFile(String filePath, String content) throws IOException {
        File file = new File(filePath);

        // 检查是否覆盖已存在的文件
        if (file.exists() && !config.getOptions().isOverwriteExisting()) {
            logger.warn("File already exists and overwrite is disabled: {}", filePath);
            return;
        }

        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }

        logger.debug("Generated file: {}", filePath);
    }

    /**
     * 获取当前配置
     */
    public CodeGeneratorConfig getConfig() {
        return config;
    }

    /**
     * 表结构信息
     */
    private static class TableInfo {
        String tableName;
        String className;
        List<ColumnInfo> columns = new ArrayList<>();
        ColumnInfo primaryKey;
    }

    /**
     * 列信息
     */
    private static class ColumnInfo {
        String columnName;
        String fieldName;
        String jdbcType;
        String javaType;
        boolean isPrimaryKey;
        boolean isNullable;
        String comment;
    }
}