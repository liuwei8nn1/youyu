# YouYu AI Service - 项目结构

## 完整目录树

```
youyu-ai/
│
├── 📄 README.md                          # 项目说明和使用指南
├── 📄 MODULES.md                         # 模块架构详细说明
├── 📄 IMPLEMENTATION_SUMMARY.md          # 实现总结
├── 📄 QUICK_REFERENCE.md                 # 快速参考卡片
├── 📄 STARTUP_GUIDE.md                   # 启动指南
├── 📄 .gitignore                         # Git 忽略配置
├── 📄 pom.xml                            # Maven 父 POM
│
├── 📁 sql/                               # 数据库脚本
│   └── init-ai.sql                       # 数据库初始化脚本
│
├── 📁 youyu-ai-api/                      # 接口定义层
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/youyu/ai/api/
│       ├── 📁 client/
│       │   └── AiFeignClient.java        # Feign 客户端接口
│       └── 📁 dto/
│           ├── AiPolishRequest.java      # 文本润色请求 DTO
│           ├── AiPolishResponse.java     # 文本润色响应 DTO
│           ├── AiGenerateRequest.java    # 文本生成请求 DTO
│           └── AiGenerateResponse.java   # 文本生成响应 DTO
│
├── 📁 youyu-ai-impl/                     # 业务实现层（DDD）
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/youyu/ai/
│       ├── 📁 interfaces/                # 控制器层
│       │   └── 📁 controller/
│       │       └── AiController.java     # REST API 控制器
│       │
│       ├── 📁 application/               # 应用服务层
│       │   └── 📁 service/
│       │       ├── AiApplicationService.java         # 应用服务接口
│       │       └── 📁 impl/
│       │           └── AiApplicationServiceImpl.java # 应用服务实现
│       │
│       ├── 📁 domain/                    # 领域服务层
│       │   └── 📁 service/
│       │       └── AiTextService.java    # AI 文本领域服务接口
│       │
│       └── 📁 infrastructure/            # 基础设施层
│           └── 📁 ai/
│               └── AiTextServiceImpl.java # AI 服务实现（Spring AI）
│
├── 📁 youyu-ai-bootstrap/                # 启动模块
│   ├── 📄 pom.xml
│   └── 📁 src/main/
│       ├── 📁 java/com/youyu/ai/
│       │   └── AiApplication.java        # Spring Boot 启动类
│       │
│       └── 📁 resources/
│           ├── application.yml           # 基础配置
│           ├── application-dev.yml       # 开发环境配置
│           └── application-prod.yml      # 生产环境配置
│
└── 📁 youyu-ai-sdk/                      # 客户端 SDK
    ├── 📄 pom.xml
    └── 📁 src/main/
        ├── 📁 java/com/youyu/ai/sdk/
        │   ├── AiServiceClient.java                      # AI 服务客户端（带缓存）
        │   └── 📁 autoconfigure/
        │       └── AiServiceSdkAutoConfiguration.java    # 自动配置类
        │
        └── 📁 resources/META-INF/
            └── spring.factories          # Spring Factories 配置
```

## 模块依赖关系

```
┌─────────────────────────────────────────┐
│         youyu-ai-bootstrap              │  ← 启动入口
│         (依赖 impl)                     │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│           youyu-ai-impl                 │  ← 业务实现
│   (依赖 api + Spring AI + MyBatis)      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│           youyu-ai-api                  │  ← 接口定义
│         (依赖 common + Feign)           │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│           youyu-ai-sdk                  │  ← 客户端 SDK
│      (依赖 api + Caffeine)              │
└─────────────────────────────────────────┘
```

## 文件统计

| 类型 | 数量 | 说明 |
|------|------|------|
| Java 文件 | 11 | 核心代码 |
| XML 文件 | 5 | Maven 配置 |
| YAML 文件 | 4 | 应用配置 |
| SQL 文件 | 1 | 数据库脚本 |
| Markdown 文件 | 5 | 文档 |
| **总计** | **26** | |

## 代码行数估算

| 模块 | 估算行数 |
|------|----------|
| youyu-ai-api | ~300 行 |
| youyu-ai-impl | ~500 行 |
| youyu-ai-bootstrap | ~100 行 |
| youyu-ai-sdk | ~150 行 |
| **总计** | **~1050 行** |

## 关键技术文件说明

### 1. 核心业务文件

- **AiController.java** - REST API 入口，处理 HTTP 请求
- **AiApplicationService.java** - 应用服务接口，编排业务流程
- **AiTextService.java** - 领域服务接口，定义核心 AI 能力
- **AiTextServiceImpl.java** - AI 服务实现，集成 Spring AI

### 2. 配置文件

- **application.yml** - 基础配置（端口、Nacos、Knife4j）
- **application-dev.yml** - 开发环境（开启日志、模拟模式）
- **application-prod.yml** - 生产环境（关闭日志、真实 API）

### 3. SDK 文件

- **AiServiceClient.java** - 客户端封装，提供缓存和重试
- **AiServiceSdkAutoConfiguration.java** - Spring Boot 自动配置

### 4. 数据文件

- **init-ai.sql** - 数据库表结构（调用记录 + 使用统计）

## DDD 分层说明

### Interfaces Layer（接口层）
- **职责**：接收 HTTP 请求，参数验证，返回响应
- **文件**：`AiController.java`

### Application Layer（应用层）
- **职责**：业务流程编排，事务管理
- **文件**：`AiApplicationService.java`, `AiApplicationServiceImpl.java`

### Domain Layer（领域层）
- **职责**：核心业务逻辑，AI 调用策略
- **文件**：`AiTextService.java`

### Infrastructure Layer（基础设施层）
- **职责**：外部服务调用（AI Provider），数据持久化
- **文件**：`AiTextServiceImpl.java`

## 扩展点

如果需要添加新的 AI 功能，可以在以下位置扩展：

1. **新增 DTO** → `youyu-ai-api/dto/`
2. **新增接口** → `AiFeignClient.java`
3. **新增领域服务** → `youyu-ai-impl/domain/service/`
4. **新增实现** → `youyu-ai-impl/infrastructure/`
5. **新增 Controller** → `youyu-ai-impl/interfaces/controller/`

## 下一步

现在你可以：
1. ✅ 查看 [README.md](README.md) 了解如何使用
2. ✅ 查看 [QUICK_REFERENCE.md](QUICK_REFERENCE.md) 快速上手
3. ✅ 运行 `mvn clean install` 编译项目
4. ✅ 按照 [STARTUP_GUIDE.md](STARTUP_GUIDE.md) 启动服务

祝开发愉快！🎉
