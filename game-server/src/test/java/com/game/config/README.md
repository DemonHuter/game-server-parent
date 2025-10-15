# Excel配置转换工具

## 概述

该工具用于将game-server/config目录下的Excel配置文件转换为内存数据和Java实体类。

## 转换规则

1. **字段筛选规则**：
   - Excel第4行标记为's'的字段才会转换为实体类属性
   - Excel第4行标记为'k'的字段为主键字段

2. **类型映射规则**：
   - intIntMap → Map<Integer,Integer>
   - intList → List<Integer>
   - stringList → List<String>
   - stringList2 → List<List<String>>
   - intList2 → List<List<Integer>>
   - 其他类型以此类推

3. **数据格式规则**：
   - Map类型数据格式：{1#0,2#0,3#0}
   - List类型数据格式：{1001,1002,1003}
   - 二维List类型数据格式：{1001,10}|{1001,10}|{1001,10}

## 使用方法

### 自动加载
系统启动时会自动扫描config目录下的所有.xlsx文件，并：
1. 自动生成对应的Java实体类到`com.game.config.data`包下
2. 将Excel数据加载到内存中

### 手动使用
可以通过以下方式手动使用配置管理器：

```java
@Autowired
private ExcelConfigManager excelConfigManager;

// 获取所有配置数据
List<Object> configData = excelConfigManager.getConfigData("配置文件名");

// 根据ID获取配置数据
Object configItem = excelConfigManager.getConfigDataById("配置文件名", "ID值");

// 根据条件过滤配置数据
List<Object> filteredData = excelConfigManager.getConfigDataByFilter("配置文件名", 
    item -> {
        // 过滤条件
        if (item instanceof Map) {
            Map map = (Map) item;
            return map.get("level") != null && (Integer)map.get("level") > 10;
        }
        return false;
    });

// 重新加载指定配置文件
excelConfigManager.reloadConfig("配置文件名.xlsx");

// 获取所有配置名称
Set<String> configNames = excelConfigManager.getAllConfigNames();
```

### 独立运行工具

提供了可以单独运行的工具类，用于生成单个Excel文件对应的实体类：

#### 方法1：使用Java命令直接运行
```bash
# 编译项目
mvn clean compile

# 运行实体类生成器
java -cp "target/classes;target/dependency/*" com.game.config.ExcelEntityGenerator config/AdditionConfig.xlsx com.game.config.data src/main/java/com/game/config/data
```

#### 方法2：使用批处理脚本
```bash
# 在game-server目录下运行
generate-config-entity.bat config/AdditionConfig.xlsx com.game.config.data src/main/java/com/game/config/data
```

#### 方法3：使用Maven exec插件
```bash
# 在game-server目录下运行
mvn exec:java -Dexec.mainClass="com.game.config.ExcelEntityGenerator" -Dexec.args="config/AdditionConfig.xlsx com.game.config.data src/main/java/com/game/config/data"
```

参数说明：
1. 第一个参数：Excel文件路径（必需）
2. 第二个参数：生成实体类的包名（可选，默认为com.game.config.data）
3. 第三个参数：生成实体类的输出目录（可选，默认为src/main/java/com/game/config/data）

## API接口

提供以下REST API用于测试配置数据：

- `GET /config/all` - 获取所有配置名称
- `GET /config/{configName}` - 获取指定配置的所有数据
- `GET /config/{configName}/{id}` - 获取指定配置的指定ID数据

## 实现细节

### 核心类说明

1. **ExcelConfigManager** - 核心配置管理器，负责加载和管理Excel配置数据
2. **ExcelToEntityConverter** - Excel到Java实体类转换器，自动生成实体类代码
3. **ConfigLoaderService** - 配置加载服务，应用启动时自动加载配置
4. **ConfigTestController** - 配置测试控制器，提供REST API接口
5. **ExcelEntityGenerator** - 独立运行的实体类生成器

### 数据处理流程

1. 系统启动时，ConfigLoaderService自动调用ExcelToEntityConverter生成实体类
2. ExcelConfigManager扫描config目录下的所有.xlsx文件
3. 解析Excel文件的字段配置（第1、4、5行）
4. 将数据行（第6行开始）转换为Java对象
5. 将转换后的数据存储在内存中，供运行时访问

## 扩展说明

如需支持新的数据类型，可以修改以下方法：
- ExcelConfigManager.getCellValueByType() - 添加新的类型处理逻辑
- ExcelToEntityConverter.mapToJavaType() - 添加新的类型映射规则

## 配置说明

在application.yml中可以配置Excel文件路径：

```yaml
game:
  config:
    path: config  # Excel配置文件路径
```

## 测试说明

提供了单元测试类ExcelConfigManagerTest，可以测试配置加载功能：

```bash
# 运行测试
mvn test -Dtest=ExcelConfigManagerTest
```