# Game Generator 代码生成器模块

## 概述

Game Generator 是一个独立的代码生成工具模块，可以自动读取数据库表结构并生成对应的实体类（Entity）、缓存类（Cache）和DAO接口。该模块完全独立于Spring框架，可以在任何环境中运行。

## 特性

- **独立运行**: 不依赖Spring Boot或其他框架
- **YAML配置**: 支持YAML配置文件和命令行参数
- **配置灵活**: 支持多种配置方式（YAML文件、命令行参数、编程配置）
- **智能生成**: 自动映射数据库类型到Java类型
- **表过滤**: 支持包含/排除特定表
- **前缀处理**: 自动去除表前缀
- **可执行JAR**: 支持打包为独立可执行文件

## 目录结构

```
game-generator/
├── src/main/java/com/game/generator/
│   ├── config/
│   │   └── CodeGeneratorConfig.java      # 配置类
│   ├── util/
│   │   └── CodeGeneratorUtils.java       # 核心工具类
│   └── CodeGenerator.java                # 主程序入口
├── generate-code.bat                      # Windows批处理脚本
├── config-example.yml                     # 配置示例文件
├── README.md                              # 说明文档
└── pom.xml                                # Maven配置
```

## 快速开始

### 1. 配置数据库连接

**方式一：创建自定义配置文件（推荐）**
```bash
cp config-example.yml config.yml
```

然后编辑 `config.yml` 修改数据库配置。

**方式二：直接使用示例配置**
如果不创建 `config.yml`，系统会自动使用 `config-example.yml` 作为配置文件。

### 2. 编译项目

```bash
cd game-generator
mvn clean compile
```

### 3. 打包可执行JAR（可选）

```bash
mvn clean package
```

这将生成 `target/game-generator-1.0.0-executable.jar` 文件。

### 4. 运行代码生成器

#### 方式一：使用YAML配置文件（推荐）

```bash
# 复制配置模板并修改
cp config-example.yml config.yml
# 编辑 config.yml 修改数据库配置
# 运行代码生成器
java -jar game-generator.jar

# 或者直接指定配置文件
java -jar game-generator.jar --config config-example.yml
```

#### 方式二：使用批处理脚本

```bash
# Windows
generate-code.bat

# 带参数运行
generate-code.bat --author "Your Name" --table-prefix "t_"
```

#### 方式三：直接运行Java类

```bash
# 使用编译后的class文件
java -cp "target/classes;target/dependency/*" com.game.generator.CodeGenerator

# 使用可执行JAR
java -jar target/game-generator-1.0.0-executable.jar
```

## 命令行参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `--config` | 配置文件路径 | `--config my-config.yml` |
| `--db-url` | 数据库URL | `--db-url "jdbc:mysql://localhost:3306/game_db"` |
| `--db-username` | 数据库用户名 | `--db-username "root"` |
| `--db-password` | 数据库密码 | `--db-password "password"` |
| `--author` | 作者名称 | `--author "Developer"` |
| `--table-prefix` | 表前缀（生成时去除） | `--table-prefix "t_"` |
| `--exclude-tables` | 排除的表名（逗号分隔） | `--exclude-tables "sys_log,temp_data"` |
| `--include-tables` | 包含的表名（逗号分隔） | `--include-tables "user,player,item"` |
| `--no-overwrite` | 不覆盖已存在的文件 | `--no-overwrite` |
| `--help, -h` | 显示帮助信息 | `--help` |

## 使用示例

### 使用YAML配置文件

```bash
# 复制配置模板
cp config-example.yml config.yml

# 编辑 config.yml 修改配置
# 运行生成器
java -jar game-generator.jar
```

### 基本使用

```bash
java -jar game-generator.jar
```

使用默认配置文件（config.yml）连接数据库并生成所有表的代码。如果不存在 config.yml，请先从 config-example.yml 复制一份。

### 指定数据库连接

```bash
java -jar game-generator.jar \
  --db-url "jdbc:mysql://localhost:3306/my_db" \
  --db-username "user" \
  --db-password "pass"
```

### 指定作者和表前缀

```bash
java -jar game-generator.jar \
  --author "My Name" \
  --table-prefix "t_"
```

### 排除特定表

```bash
java -jar game-generator.jar \
  --exclude-tables "sys_log,temp_data,test_table"
```

### 只生成特定表

```bash
java -jar game-generator.jar \
  --include-tables "user,player,item"
```

## 生成的代码

### 实体类（Entity）

```java
package com.game.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * User实体类
 * 表名: user
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
public class User extends BaseEntity {

    /** 用户名 */
    private String username;
    
    /** 邮箱 */
    private String email;
    
    // getter/setter方法...
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s'}", 
                getId(), username, email);
    }
}
```

### 缓存类（Cache）

```java
package com.game.cache;

import com.game.dao.entity.User;
import com.game.dao.mapper.UserDao;
import org.springframework.stereotype.Component;

/**
 * User缓存类
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
@Component
public class UserCache extends BaseCache<User> {

    private final UserDao userDao;

    public UserCache(UserDao userDao) {
        super(userDao);
        this.userDao = userDao;
    }
}
```

### DAO接口

```java
package com.game.dao.mapper;

import com.game.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User数据访问接口
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
@Mapper
public interface UserDao extends BaseDao<User> {

    // 继承BaseDao的基本CRUD方法
    // 可在此添加特定的查询方法

}
```

## 配置说明

### YAML配置文件

代码生成器支持使用YAML文件进行配置。系统会按以下顺序查找配置文件：
1. 命令行指定的配置文件 (`--config` 参数)
2. 当前目录下的 `config.yml` 文件
3. 当前目录下的 `config-example.yml` 文件（如果 `config.yml` 不存在）
4. 内置默认配置（如果所有文件都不存在）

配置文件结构：

```yaml
# 数据库配置
database:
  url: "jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC"
  username: "game_user"
  password: "game_pass"
  driver-class-name: "com.mysql.cj.jdbc.Driver"

# 生成路径配置
paths:
  entity: "game-dao/src/main/java/com/game/dao/entity/"
  cache: "game-cache/src/main/java/com/game/cache/"
  dao: "game-dao/src/main/java/com/game/dao/mapper/"
  xml: "game-dao/src/main/resources/mapper/"

# 包名配置
packages:
  entity: "com.game.dao.entity"
  cache: "com.game.cache"
  dao: "com.game.dao.mapper"

# 生成选项配置
options:
  overwrite-existing: true
  generate-xml: false
  generate-swagger: false
  generate-validation: false
  table-prefix: ""
  exclude-tables: ""
  include-tables: ""
  author: "CodeGenerator"
```

### 配置加载顺序

代码生成器的配置加载顺序为：

1. **命令行指定的配置文件** (`--config` 参数)
2. **默认配置文件** (`config.yml`)
3. **示例配置文件** (`config-example.yml`) - 如果 `config.yml` 不存在
4. **内置默认配置** - 如果所有配置文件都不存在

配置优先级（从高到低）：
1. 命令行参数
2. YAML配置文件
3. 默认配置

### 默认配置

- **数据库URL**: `jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC`
- **用户名**: `game_user`
- **密码**: `game_pass`
- **实体类包名**: `com.game.dao.entity`
- **缓存类包名**: `com.game.cache`
- **DAO包名**: `com.game.dao.mapper`

### 生成路径

- **实体类**: `game-dao/src/main/java/com/game/dao/entity/`
- **缓存类**: `game-cache/src/main/java/com/game/cache/`
- **DAO接口**: `game-dao/src/main/java/com/game/dao/mapper/`

## 数据类型映射

| MySQL类型 | Java类型 | 说明 |
|-----------|----------|------|
| INT | Integer | 整数类型 |
| BIGINT | Long | 长整数类型 |
| TINYINT | Boolean | 布尔类型（0/1） |
| VARCHAR/TEXT/CHAR | String | 字符串类型 |
| DECIMAL/NUMERIC | java.math.BigDecimal | 精确小数 |
| FLOAT | Float | 浮点数 |
| DOUBLE | Double | 双精度浮点数 |
| DATE/DATETIME/TIMESTAMP | java.util.Date | 日期时间 |
| BLOB | byte[] | 二进制数据 |

## 注意事项

1. **数据库连接**: 确保MySQL数据库服务正在运行且可以连接
2. **表结构**: 表必须有主键（通常为id字段）
3. **权限**: 确保数据库用户有读取表结构的权限
4. **文件覆盖**: 默认会覆盖已存在的文件，使用`--no-overwrite`参数可以避免覆盖
5. **依赖**: 需要MySQL JDBC驱动，已包含在项目依赖中

## 故障排除

### 1. 数据库连接失败

- 检查数据库连接参数是否正确
- 确认MySQL服务是否启动
- 检查用户权限

### 2. 编译失败

- 确保JDK 8+已安装
- 确保Maven 3.6+已安装
- 检查网络连接（Maven需要下载依赖）

### 3. 生成失败

- 检查目标目录是否存在写入权限
- 确认表结构是否符合要求（有主键）
- 查看日志输出获取详细错误信息

## 扩展开发

如果需要扩展代码生成器功能，可以：

1. 修改 `CodeGeneratorUtils.java` 中的生成模板
2. 添加新的数据类型映射
3. 扩展配置选项
4. 添加新的命令行参数

## 版本信息

- **版本**: 1.0.0
- **JDK要求**: 8+
- **Maven要求**: 3.6+
- **数据库**: MySQL 5.7+