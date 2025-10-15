package com.game.generator;

import com.game.generator.config.CodeGeneratorConfig;
import com.game.generator.util.CodeGeneratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 代码生成器主类
 * 使用示例和配置，支持通过配置对象进行灵活配置
 */
public class CodeGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);
    
    public static void main(String[] args) {
        // 检查是否是帮助命令
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg)) {
                printUsage();
                return;
            }
        }
        
        try {
            // 创建配置对象
            CodeGeneratorConfig config = loadConfiguration(args);
            
            // 可以根据命令行参数调整配置
            parseCommandLineArgs(config, args);
            
            // 创建代码生成器
            CodeGeneratorUtils generator = new CodeGeneratorUtils(config);
            
            logger.info("Starting code generation with config: {}", config);
            
            // 生成所有代码
            generator.generateAll();
            
            logger.info("Code generation completed successfully!");
            printGenerationSummary(config);
            
        } catch (Exception e) {
            logger.error("Code generation failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * 加载配置文件
     */
    private static CodeGeneratorConfig loadConfiguration(String[] args) {
        // 默认配置文件路径
        String configFile = "config.yml";
        
        // 检查命令行参数中是否指定了配置文件
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i])) {
                configFile = args[i + 1];
                break;
            }
        }
        
        Path configPath = Paths.get(configFile);
        
        // 如果指定的配置文件存在，尝试加载
        if (Files.exists(configPath)) {
            try {
                logger.info("Loading configuration from: {}", configPath.toAbsolutePath());
                return loadFromYaml(configPath);
            } catch (Exception e) {
                logger.warn("Failed to load configuration from {}: {}, trying fallback options", configFile, e.getMessage());
            }
        }
        
        // 如果默认的 config.yml 不存在，尝试使用 config-example.yml
        if ("config.yml".equals(configFile)) {
            Path exampleConfigPath = Paths.get("config-example.yml");
            if (Files.exists(exampleConfigPath)) {
                try {
                    logger.info("config.yml not found, loading from config-example.yml: {}", exampleConfigPath.toAbsolutePath());
                    logger.warn("Using example configuration. Please copy config-example.yml to config.yml and modify as needed.");
                    return loadFromYaml(exampleConfigPath);
                } catch (Exception e) {
                    logger.warn("Failed to load configuration from config-example.yml: {}, using default configuration", e.getMessage());
                }
            }
        }
        
        logger.info("No configuration file found, using built-in default configuration");
        return createDefaultConfig();
    }
    
    /**
     * 从 YAML 文件加载配置
     */
    private static CodeGeneratorConfig loadFromYaml(Path configPath) throws Exception {
        Yaml yaml = new Yaml(new Constructor(CodeGeneratorConfig.class));
        
        try (InputStream inputStream = new FileInputStream(configPath.toFile())) {
            CodeGeneratorConfig config = yaml.load(inputStream);
            
            // 如果某些字段为 null，设置默认值
            if (config == null) {
                config = new CodeGeneratorConfig();
            }
            
            // 添加调试日志
            logger.info("Loaded config from YAML: database.url={}, database.username={}", 
                    config.getDatabase() != null ? config.getDatabase().getUrl() : "null",
                    config.getDatabase() != null ? config.getDatabase().getUsername() : "null");
            
            setDefaultsIfNull(config);
            
            // 调试日志：显示最终配置
            logger.info("Final config after defaults: database.url={}, database.username={}", 
                    config.getDatabase().getUrl(), config.getDatabase().getUsername());
            
            return config;
        }
    }
    
    /**
     * 为 null 字段设置默认值
     */
    private static void setDefaultsIfNull(CodeGeneratorConfig config) {
        if (config.getDatabase() == null) {
            config.setDatabase(new CodeGeneratorConfig.Database());
        }
        if (config.getPaths() == null) {
            config.setPaths(new CodeGeneratorConfig.Paths());
        }
        if (config.getPackages() == null) {
            config.setPackages(new CodeGeneratorConfig.Packages());
        }
        if (config.getOptions() == null) {
            config.setOptions(new CodeGeneratorConfig.Options());
        }
        
        // 设置默认值
        if (config.getBaseDir() == null) {
            config.setBaseDir("");
        }
        
        CodeGeneratorConfig.Database db = config.getDatabase();
        if (db.getUrl() == null) {
            db.setUrl("jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC");
        }
        if (db.getUsername() == null) {
            db.setUsername("game_user");
        }
        if (db.getPassword() == null) {
            db.setPassword("game_pass");
        }
        if (db.getDriverClassName() == null) {
            db.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        
        CodeGeneratorConfig.Paths paths = config.getPaths();
        if (paths.getEntity() == null) {
            paths.setEntity("game-dao/src/main/java/com/game/dao/entity/");
        }
        if (paths.getCache() == null) {
            paths.setCache("game-cache/src/main/java/com/game/cache/");
        }
        if (paths.getDao() == null) {
            paths.setDao("game-dao/src/main/java/com/game/dao/mapper/");
        }
        if (paths.getXml() == null) {
            paths.setXml("game-dao/src/main/resources/mapper/");
        }
        
        // 如果设置了 baseDir，则在所有路径前添加 baseDir
        if (!config.getBaseDir().isEmpty()) {
            if (!paths.getEntity().startsWith(config.getBaseDir())) {
                paths.setEntity(config.getBaseDir() + paths.getEntity());
            }
            if (!paths.getCache().startsWith(config.getBaseDir())) {
                paths.setCache(config.getBaseDir() + paths.getCache());
            }
            if (!paths.getDao().startsWith(config.getBaseDir())) {
                paths.setDao(config.getBaseDir() + paths.getDao());
            }
            if (!paths.getXml().startsWith(config.getBaseDir())) {
                paths.setXml(config.getBaseDir() + paths.getXml());
            }
        }
        
        CodeGeneratorConfig.Packages packages = config.getPackages();
        if (packages.getEntity() == null) {
            packages.setEntity("com.game.dao.entity");
        }
        if (packages.getCache() == null) {
            packages.setCache("com.game.cache");
        }
        if (packages.getDao() == null) {
            packages.setDao("com.game.dao.mapper");
        }
        
        CodeGeneratorConfig.Options options = config.getOptions();
        if (options.getTablePrefix() == null) {
            options.setTablePrefix("");
        }
        if (options.getExcludeTables() == null) {
            options.setExcludeTables("");
        }
        if (options.getIncludeTables() == null) {
            options.setIncludeTables("");
        }
        if (options.getAuthor() == null) {
            options.setAuthor("CodeGenerator");
        }
    }
    
    /**
     * 创建默认配置
     */
    private static CodeGeneratorConfig createDefaultConfig() {
        CodeGeneratorConfig config = new CodeGeneratorConfig();
        setDefaultsIfNull(config);
        return config;
    }
    
    /**
     * 解析命令行参数
     */
    private static void parseCommandLineArgs(CodeGeneratorConfig config, String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "--config":
                    // config 参数已经在 loadConfiguration 中处理，这里跳过
                    if (i + 1 < args.length) {
                        i++; // 跳过配置文件路径
                    }
                    break;
                case "--db-url":
                    if (i + 1 < args.length) {
                        config.getDatabase().setUrl(args[++i]);
                    }
                    break;
                case "--db-username":
                    if (i + 1 < args.length) {
                        config.getDatabase().setUsername(args[++i]);
                    }
                    break;
                case "--db-password":
                    if (i + 1 < args.length) {
                        config.getDatabase().setPassword(args[++i]);
                    }
                    break;
                case "--author":
                    if (i + 1 < args.length) {
                        config.getOptions().setAuthor(args[++i]);
                    }
                    break;
                case "--table-prefix":
                    if (i + 1 < args.length) {
                        config.getOptions().setTablePrefix(args[++i]);
                    }
                    break;
                case "--exclude-tables":
                    if (i + 1 < args.length) {
                        config.getOptions().setExcludeTables(args[++i]);
                    }
                    break;
                case "--include-tables":
                    if (i + 1 < args.length) {
                        config.getOptions().setIncludeTables(args[++i]);
                    }
                    break;
                case "--no-overwrite":
                    config.getOptions().setOverwriteExisting(false);
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
            }
        }
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("Usage: java CodeGenerator [options]");
        System.out.println("Options:");
        System.out.println("  --config <file>          Configuration file path (default: config.yml)");
        System.out.println("  --db-url <url>           Database URL");
        System.out.println("  --db-username <username> Database username");
        System.out.println("  --db-password <password> Database password");
        System.out.println("  --author <name>          Author name for generated code");
        System.out.println("  --table-prefix <prefix>  Table prefix to remove");
        System.out.println("  --exclude-tables <list>  Comma-separated list of tables to exclude");
        System.out.println("  --include-tables <list>  Comma-separated list of tables to include");
        System.out.println("  --no-overwrite           Don't overwrite existing files");
        System.out.println("  --help, -h               Show this help message");
    }
    
    /**
     * 打印生成结果总结
     */
    private static void printGenerationSummary(CodeGeneratorConfig config) {
        logger.info("===============================================");
        logger.info("          代码生成完成！");
        logger.info("===============================================");
        logger.info("Generated files location:");
        logger.info("- Entity classes: {}", config.getPaths().getEntity());
        logger.info("- Cache classes:  {}", config.getPaths().getCache());
        logger.info("- DAO interfaces: {}", config.getPaths().getDao());
        logger.info("Package configuration:");
        logger.info("- Entity package: {}", config.getPackages().getEntity());
        logger.info("- Cache package:  {}", config.getPackages().getCache());
        logger.info("- DAO package:    {}", config.getPackages().getDao());
        logger.info("Author: {}", config.getOptions().getAuthor());
        logger.info("===============================================");
    }
}