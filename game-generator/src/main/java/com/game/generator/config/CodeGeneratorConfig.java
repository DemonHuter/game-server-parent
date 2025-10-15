package com.game.generator.config;

/**
 * 代码生成器配置类
 * 用于管理数据库连接、生成路径、包名等配置信息
 * 独立于Spring框架，可以在任何环境中使用
 */
public class CodeGeneratorConfig {
    
    /** 基础目录路径 */
    private String baseDir;
    
    /** 数据库配置 */
    private Database database = new Database();
    
    /** 生成路径配置 */
    private Paths paths = new Paths();
    
    /** 包名配置 */
    private Packages packages = new Packages();
    
    /** 生成选项配置 */
    private Options options = new Options();
    
    /**
     * 获取基础目录路径
     */
    public String getBaseDir() {
        return baseDir;
    }
    
    /**
     * 设置基础目录路径
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    /**
     * 数据库配置
     */
    public static class Database {
        /** JDBC连接URL */
        private String url;
        
        /** 数据库用户名 */
        private String username;
        
        
        /** 数据库密码 */
        private String password;
        
        /** 数据库驱动类名 */
        private String driverClassName;
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    }
    
    /**
     * 生成路径配置
     */
    public static class Paths {
        /** 实体类生成路径 */
        private String entity;
        
        /** 缓存类生成路径 */
        private String cache;
        
        /** DAO接口生成路径 */
        private String dao;
        
        /** XML映射文件生成路径 */
        private String xml;
        
        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        
        public String getCache() { return cache; }
        public void setCache(String cache) { this.cache = cache; }
        
        public String getDao() { return dao; }
        public void setDao(String dao) { this.dao = dao; }
        
        public String getXml() { return xml; }
        public void setXml(String xml) { this.xml = xml; }
    }
    
    /**
     * 包名配置
     */
    public static class Packages {
        /** 实体类包名 */
        private String entity;
        
        /** 缓存类包名 */
        private String cache;
        
        /** DAO接口包名 */
        private String dao;
        
        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        
        public String getCache() { return cache; }
        public void setCache(String cache) { this.cache = cache; }
        
        public String getDao() { return dao; }
        public void setDao(String dao) { this.dao = dao; }
    }
    
    /**
     * 生成选项配置
     */
    public static class Options {
        /** 是否覆盖已存在的文件 */
        private boolean overwriteExisting;
        
        /** 是否生成XML映射文件 */
        private boolean generateXml;
        
        /** 是否生成Swagger注解 */
        private boolean generateSwagger;
        
        /** 是否生成验证注解 */
        private boolean generateValidation;
        
        /** 表名前缀（生成时会去除） */
        private String tablePrefix;
        
        /** 排除的表名（逗号分隔） */
        private String excludeTables;
        
        /** 包含的表名（逗号分隔，为空则包含所有） */
        private String includeTables;
        
        /** 作者名称 */
        private String author;
        
        public boolean isOverwriteExisting() { return overwriteExisting; }
        public void setOverwriteExisting(boolean overwriteExisting) { this.overwriteExisting = overwriteExisting; }
        
        public boolean isGenerateXml() { return generateXml; }
        public void setGenerateXml(boolean generateXml) { this.generateXml = generateXml; }
        
        public boolean isGenerateSwagger() { return generateSwagger; }
        public void setGenerateSwagger(boolean generateSwagger) { this.generateSwagger = generateSwagger; }
        
        public boolean isGenerateValidation() { return generateValidation; }
        public void setGenerateValidation(boolean generateValidation) { this.generateValidation = generateValidation; }
        
        public String getTablePrefix() { return tablePrefix; }
        public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }
        
        public String getExcludeTables() { return excludeTables; }
        public void setExcludeTables(String excludeTables) { this.excludeTables = excludeTables; }
        
        public String getIncludeTables() { return includeTables; }
        public void setIncludeTables(String includeTables) { this.includeTables = includeTables; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }
    
    // Getter and Setter for main properties
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }
    
    public Paths getPaths() { return paths; }
    public void setPaths(Paths paths) { this.paths = paths; }
    
    public Packages getPackages() { return packages; }
    public void setPackages(Packages packages) { this.packages = packages; }
    
    public Options getOptions() { return options; }
    public void setOptions(Options options) { this.options = options; }
    
    @Override
    public String toString() {
        return String.format("CodeGeneratorConfig{database.url='%s', paths.entity='%s', packages.entity='%s'}",
                database.url, paths.entity, packages.entity);
    }
}