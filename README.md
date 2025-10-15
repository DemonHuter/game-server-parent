# 游戏服务器框架 (Game Server Framework)

一个基于Spring Boot + Netty + MyBatis + MySQL的高性能游戏服务器框架，采用多模块架构设计，支持高并发、高可用的游戏后端服务。

## 🎯 项目特性

- **高性能网络通信**: 基于Netty实现TCP长连接，支持数千并发用户
- **协议化通信**: 使用Protobuf进行高效的消息序列化
- **多层缓存设计**: 内存缓存 + 数据库持久化，提升数据访问性能
- **模块化架构**: 清晰的分层设计，便于维护和扩展
- **异步处理**: 支持异步任务处理和数据持久化
- **高可用性**: 包含健康检查、监控和管理接口
- **配置管理**: 支持Excel配置文件自动转换为内存数据和Java实体类

## 🏗️ 架构设计

### 模块结构

```
qoder/
├── game-protobuf/          # Protobuf协议模块
├── game-server/            # 主服务模块
├── game-generator/         # 代码生成器模块
└── game-protobuf/          # Protobuf定义模块
```

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 8+ | 开发语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Netty | 4.1.97 | 网络通信 |
| MyBatis | 2.3.1 | ORM框架 |
| Protobuf | 3.24.4 | 序列化协议 |
| MySQL | 8.0.33 | 数据库 |
| HikariCP | - | 连接池 |
| Apache POI | 5.2.3 | Excel处理 |

## 🚀 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+

### 1. 克隆项目

```bash
git clone <repository-url>
cd qoder
```

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source game-server/src/main/resources/sql/init.sql
```

### 3. 配置数据库

修改 `game-server/src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
    username: game_user
    password: game_pass
```

### 4. 编译项目

```bash
# 编译所有模块
mvn clean compile

# 或者分步编译（推荐首次构建）
cd game-protobuf
mvn clean compile install
cd ../game-server
mvn clean compile
```

### 5. 启动服务

```bash
cd game-server
mvn spring-boot:run
```

服务启动后：
- TCP游戏服务端口：9999
- HTTP管理端口：8080

## 📁 项目结构详解

### game-protobuf 模块
定义游戏通信协议，包含：
- 消息类型枚举
- 请求/响应消息定义
- 用户信息、聊天消息等数据结构

### game-server 模块
主服务模块，包含：
- Spring Boot启动类
- 业务服务层
- HTTP管理接口
- 配置文件
- Excel配置转换工具
- 网络通信层（Netty）
- 数据访问层（MyBatis）
- 缓存层

### game-generator 模块
代码生成器模块，包含：
- 数据库表到Java实体类的代码生成工具
- 支持通过配置文件或命令行参数生成代码
- 提供批处理脚本简化代码生成过程

## 🎮 功能特性

### 核心功能
- ✅ 用户登录/登出
- ✅ 用户信息管理
- ✅ 聊天系统（世界聊天、私聊、公会聊天）
- ✅ 心跳机制
- ✅ 连接管理
- ✅ Excel配置文件自动转换
- ✅ 计数器系统
- ✅ 异步消息处理

### 管理功能
- ✅ 服务器状态监控
- ✅ 缓存管理（手动持久化、清理、重载）
- ✅ 健康检查
- ✅ JVM监控
- ✅ 配置数据管理

## 🔧 API接口

### 管理接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/server/health` | GET | 健康检查 |
| `/api/server/status` | GET | 服务器状态 |
| `/api/server/cache/stats` | GET | 缓存统计 |
| `/api/server/cache/persist` | POST | 手动持久化缓存 |
| `/api/server/cache/cleanup` | POST | 清理缓存 |
| `/api/server/cache/reload` | POST | 重载缓存 |
| `/api/config/names` | GET | 获取所有配置名称 |
| `/api/config/{configName}` | GET | 获取指定配置的所有数据 |
| `/api/config/{configName}/size` | GET | 获取指定配置的数据大小 |
| `/api/config/{configName}/{id}` | GET | 根据ID获取配置数据 |
| `/api/config/reload/{configFileName}` | POST | 重新加载指定配置文件 |
| `/api/config/reload-all` | POST | 重新加载所有配置文件 |

### TCP消息类型

- `SYSTEM_HEARTBEAT` - 心跳消息
- `USER_LOGIN_REQUEST/RESPONSE` - 用户登录
- `USER_LOGOUT_REQUEST/RESPONSE` - 用户登出
- `USER_INFO_REQUEST/RESPONSE` - 用户信息查询
- `USER_UPDATE_REQUEST/RESPONSE` - 用户信息更新
- `CHAT_MESSAGE_REQUEST/RESPONSE` - 聊天消息
- `CHAT_BROADCAST` - 聊天广播

## 💾 数据库设计

### 主要表结构

- `user` - 用户表
- `chat_log` - 聊天记录表
- `counter` - 计数器表
- `guild` - 公会表（预留）
- `guild_member` - 公会成员表（预留）
- `game_log` - 游戏日志表

## 📊 监控和运维

### 健康检查
```bash
curl http://localhost:8080/api/server/health
```

### 缓存统计
```bash
curl http://localhost:8080/api/server/cache/stats
```

### 服务器状态
```bash
curl http://localhost:8080/api/server/status
```

### 配置数据访问
```bash
# 获取所有配置名称
curl http://localhost:8080/api/config/names

# 获取指定配置的所有数据
curl http://localhost:8080/api/config/AdditionConfig

# 根据ID获取配置数据
curl http://localhost:8080/api/config/AdditionConfig/1
```

## 🔧 代码生成器

### 功能特性

- 根据数据库表结构自动生成实体类、DAO接口、缓存类
- 支持通过配置文件或命令行参数指定生成选项
- 提供批处理脚本简化代码生成过程
- 支持生成单个或多个表的代码
- 支持排除特定表不生成代码

### 使用方法

#### 通过批处理脚本生成代码

```bash
# 双击运行，按提示输入表名
cd game-generator
generate-code.bat

# 或者通过命令行指定表名
generate-code.bat player user

# 生成所有表的代码
generate-code.bat
```

#### 通过命令行参数生成代码

```bash
cd game-generator
java -jar target/game-generator-1.0.0-executable.jar --include-tables player,user --author "Your Name"
```

### 配置文件

代码生成器支持通过 `config.yml` 文件进行配置：

```yaml
database:
  url: "jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC"
  username: "game_user"
  password: "game_pass"

paths:
  entity: "game-dao/src/main/java/com/game/dao/entity/"
  cache: "game-cache/src/main/java/com/game/cache/"
  dao: "game-dao/src/main/java/com/game/dao/mapper/"

packages:
  entity: "com.game.dao.entity"
  cache: "com.game.cache"
  dao: "com.game.dao.mapper"

options:
  author: "CodeGenerator"
  overwriteExisting: true
```

## 🏃‍♂️ 开发指南

### 添加新的消息类型

1. 在 `game.proto` 中定义新的消息类型和结构
2. 编译protobuf：`mvn clean compile`
3. 在 `GameMessageHandler` 中添加处理逻辑
4. 创建对应的服务类处理业务逻辑

### 添加新的实体类

1. 在数据库中创建对应的表
2. 使用代码生成器生成实体类、DAO接口和缓存类
3. 或者手动创建实体类、DAO接口和缓存类
4. 更新数据库表结构

### 使用Excel配置数据

1. 在业务代码中注入`ConfigService`
2. 调用相应方法获取配置数据：

```java
@Autowired
private ConfigService configService;

// 获取配置数据
List<Object> configData = configService.getConfigData("配置文件名");

// 根据ID获取配置数据
Object configItem = configService.getConfigDataById("配置文件名", "ID值");
```

## 🧪 测试

### 编译测试
```bash
mvn clean compile
```

### 运行测试
```bash
mvn test
```

### 客户端测试

项目提供了两种Netty客户端实现用于测试服务器功能：

1. **基础版客户端** - 实现基本的登录、聊天、登出功能
2. **增强版客户端** - 提供完整的测试命令集，包括压力测试、流程测试等

#### 启动客户端

```bash
# 启动增强版客户端（推荐）
start-enhanced-client.bat

# 启动基础版客户端
start-client.bat
```

#### 客户端功能

增强版客户端支持以下测试命令：

**基础命令：**
- `login <username> <password>` - 用户登录
- `info [userId]` - 获取用户信息
- `update <level> <gold> <exp>` - 更新用户信息
- `chat <type> <message> [targetUserId] [guildId]` - 发送聊天消息
- `logout` - 用户登出
- `heartbeat` - 发送心跳消息

**测试命令：**
- `test_login` - 测试完整登录流程
- `test_chat` - 测试聊天功能（世界聊天、私聊、公会聊天）
- `stress <count>` - 压力测试（发送指定数量的心跳消息）

**系统命令：**
- `help` - 显示帮助信息
- `exit` - 退出客户端

## 📝 开发环境配置

### 使用开发环境配置
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 使用生产环境配置
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🔍 故障排除

### 常见问题

1. **编译失败**：检查JDK版本是否为8+
2. **数据库连接失败**：检查MySQL服务是否启动，配置是否正确
3. **端口占用**：检查9999和8080端口是否被占用
4. **Protobuf编译失败**：确保网络连接正常，Maven能下载protoc
5. **Excel配置加载失败**：检查Excel文件格式是否正确，字段配置是否符合规则

### 日志配置

日志文件位置：`logs/game-server.log`

调整日志级别：
```yaml
logging:
  level:
    com.game: DEBUG  # 调整为所需级别
    com.game.config: DEBUG  # Excel配置相关日志
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 作者

- 开发团队 - 游戏服务器框架

## 🔗 相关链接

- [Spring Boot文档](https://spring.io/projects/spring-boot)
- [Netty官网](https://netty.io/)
- [MyBatis文档](https://mybatis.org/mybatis-3/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)

---

## 📈 项目状态

- ✅ 基础框架完成
- ✅ 核心功能实现
- ✅ Excel配置转换工具完成
- ✅ 代码生成器完成
- ⏳ 性能优化中
- ⏳ 扩展功能开发中

如有问题或建议，欢迎提交Issue或Pull Request！