# Excel实体类生成器使用说明

## 概述

Excel实体类生成器是一个独立的工具，可以将单个Excel配置文件转换为对应的Java实体类。该工具可以单独运行，无需启动整个游戏服务器。

## 构建项目

在使用生成器之前，需要先构建项目：

```bash
# 进入game-server目录
cd game-server

# 编译项目
mvn clean compile

# 或者构建包含依赖的包
mvn clean package
```

## 使用方法

### 方法1：直接在Java代码中调用简化方法（推荐）

可以直接在Java代码中调用简化的方法，只需要传入Excel文件名：

```java
// 直接调用简化方法，只需要传入Excel文件名
ExcelEntityGenerator.generateEntity("AdditionConfig.xlsx");
```

### 方法2：直接在Java代码中调用完整方法

可以直接在Java代码中调用转换器的方法：

```java
// 创建转换器
ExcelToEntityConverter converter = new ExcelToEntityConverter();

// 直接调用转换方法
converter.convertExcelToEntity(
    "config/AdditionConfig.xlsx",           // Excel文件路径
    "com.game.config.data",                 // 包名
    "src/main/java/com/game/config/data"    // 输出路径
);
```

### 方法3：使用Java命令行运行

```bash
# 在game-server目录下运行，只需要传入Excel文件名
java -cp "target/classes;target/dependency/*" com.game.config.ExcelEntityGenerator AdditionConfig.xlsx

# 或者指定包名和输出路径
java -cp "target/classes;target/dependency/*" com.game.config.ExcelEntityGenerator AdditionConfig.xlsx com.game.model.config src/main/java/com/game/model/config
```

### 方法4：使用Maven exec插件

```bash
# 在game-server目录下运行
mvn exec:java -Dexec.mainClass="com.game.config.ExcelEntityGenerator" -Dexec.args="AdditionConfig.xlsx"

# 或者指定包名和输出路径
mvn exec:java -Dexec.mainClass="com.game.config.ExcelEntityGenerator" -Dexec.args="AdditionConfig.xlsx com.game.model.config src/main/java/com/game/model/config"
```

## 参数说明

1. **Excel文件名**（必需）
   - Excel配置文件的名称（会自动在config目录下查找）
   - 必须是.xlsx格式
   - 文件需要遵循特定格式（见下文）

2. **包名**（可选）
   - 生成实体类的包名
   - 默认值：com.game.config.data

3. **输出路径**（可选）
   - 生成实体类的输出目录
   - 默认值：src/main/java/com/game/config/data

## Excel文件格式要求

Excel文件需要遵循以下格式：

| 行号 | 用途 | 说明 |
|------|------|------|
| 第1行 | 字段名称 | 每列的字段名称 |
| 第2行 | 说明 | 字段的中文说明（可选） |
| 第3行 | 说明 | 字段的详细说明（可选） |
| 第4行 | 字段标记 | 's'表示服务端字段，'k'表示主键字段 |
| 第5行 | 字段类型 | 字段的数据类型（如int, string, intList等） |
| 第6行开始 | 数据 | 实际的数据内容 |

### 支持的数据类型

1. **基本类型**：
   - int / integer
   - long
   - float
   - double
   - boolean
   - string
   - bigdecimal
   - date

2. **集合类型**：
   - intList → List<Integer>
   - stringList → List<String>
   - longList → List<Long>
   - floatList → List<Float>
   - doubleList → List<Double>
   - booleanList → List<Boolean>

3. **二维集合类型**：
   - intList2 → List<List<Integer>>
   - stringList2 → List<List<String>>
   - longList2 → List<List<Long>>
   - floatList2 → List<List<Float>>
   - doubleList2 → List<List<Double>>
   - booleanList2 → List<List<Boolean>>

4. **Map类型**：
   - intIntMap → Map<Integer, Integer>
   - intStringMap → Map<Integer, String>
   - stringIntMap → Map<String, Integer>
   - stringStringMap → Map<String, String>

### 数据格式示例

1. **List类型**：{1,2,3,4,5}
2. **Map类型**：{1#value1,2#value2,3#value3}
3. **二维List类型**：{1,2|3,4|5,6}

## 示例

假设有一个Excel文件config/AdditionConfig.xlsx：

| id | name | values | valueMap |
|----|------|--------|----------|
| 编号 | 名称 | 数值列表 | 数值映射 |
|    |      |        |          |
| s  | s    | s      | s        |
| k  |      | intList| intIntMap|
| 1  | 测试1| {1,2,3}| {1#10,2#20}|
| 2  | 测试2| {4,5,6}| {3#30,4#40}|

运行生成器后会生成AdditionConfig.java实体类：

```java
public class AdditionConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** int */
    private Integer id;
    
    /** string */
    private String name;
    
    /** intList */
    private List<Integer> values;
    
    /** intIntMap */
    private Map<Integer, Integer> valueMap;
    
    // getter和setter方法...
}
```

## 常见问题

### 1. 错误：Excel文件不存在
确保Excel文件路径正确，并且文件确实存在。

### 2. 错误：无法创建输出目录
确保有权限在指定路径创建目录和文件。

### 3. 生成的实体类不正确
检查Excel文件格式是否符合要求，特别是第4行和第5行的标记和类型。

### 4. 类路径错误
确保先运行mvn compile命令编译项目，再运行生成器。

## 注意事项

1. Excel文件必须是.xlsx格式（不支持.xls）
2. 确保Excel文件格式正确，特别是第4行和第5行
3. 生成的实体类会覆盖同名文件，请注意备份
4. 建议在版本控制中提交生成的实体类