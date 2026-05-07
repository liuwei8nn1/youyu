# YouYu Basics Service - 基础公共服务

## 📖 模块说明

**youyu-basics** 是一个合并的基础公共服务，包含文件管理和通知发送两大功能域。

### 设计理念

- **避免过度拆分**：将相关的公共服务合并到一个微服务中
- **功能内聚**：通过功能域分包（file、notification）保持代码清晰
- **易于扩展**：未来如需独立部署，可按功能域拆分为独立微服务

---

## 🏗️ 模块结构

```
youyu-basics/
├── youyu-basics-api/              # API 定义模块
├── youyu-basics-impl/             # 实现模块（DDD 四层架构）
├── youyu-basics-sdk/              # 客户端 SDK
└── youyu-basics-bootstrap/        # 启动模块
```

---

## 📦 功能域划分

### 1. File（文件功能域）

**职责**：文件上传、下载、存储管理

**支持存储方式**：
- 本地文件系统
- 阿里云 OSS
- MinIO

**典型场景**：
- 用户头像上传
- 商品图片管理
- 文档存储

### 2. Notification（通知功能域）

**职责**：统一通知服务，根据类型自动选择发送渠道

**支持渠道**：
- **SMS**：短信通知（阿里云、腾讯云）
- **Email**：邮件通知（SMTP）
- **IN_APP**：站内信

**典型场景**：
- 注册验证码发送
- 订单状态通知
- 系统公告推送

---

## 🎯 DDD 四层架构 + 功能域分包

### 目录结构示例

```
com.youyu.basics/
├── interfaces/                    # 接口层
│   ├── FileController.java
│   └── NotificationController.java
│
├── application/                   # 应用层（按功能分包）
│   ├── file/
│   │   ├── FileService.java
│   │   └── impl/FileServiceImpl.java
│   └── notification/
│       ├── NotificationService.java
│       ├── SmsService.java
│       ├── EmailService.java
│       └── impl/
│
├── domain/                        # 领域层（按功能分包）
│   ├── file/
│   │   ├── entity/FileRecord.java
│   │   └── valueobject/StorageType.java
│   └── notification/
│       ├── entity/NotificationLog.java
│       └── valueobject/ChannelConfig.java
│
└── infrastructure/                # 基础设施层（按功能分包）
    ├── file/
    │   ├── mapper/FileRecordMapper.java
    │   └── repository/FileRepositoryImpl.java
    └── notification/
        ├── mapper/NotificationLogMapper.java
        └── repository/NotificationRepositoryImpl.java
```

### 分层职责

| 层级 | 职责 | 示例 |
|------|------|------|
| **interfaces** | REST 接口、参数校验 | FileController、NotificationController |
| **application** | 业务编排、事务控制 | FileService、NotificationService |
| **domain** | 核心业务逻辑、领域模型 | FileRecord、NotificationLog |
| **infrastructure** | 数据访问、外部调用 | Mapper、Repository、第三方 API |

---

## 🔗 依赖关系

```
其他业务服务 (auth/user/order...)
    ↓ 通过 Feign 调用
youyu-basics-sdk
    ↓ 依赖
youyu-basics-api
    ↓ 依赖
youyu-basics-impl
    ↓ 依赖
youyu-starter → youyu-framework → youyu-common
```

---

## 🚀 使用示例

### 1. 在其他服务中引入 SDK

```xml
<dependency>
    <groupId>com.youyu</groupId>
    <artifactId>youyu-basics-sdk</artifactId>
</dependency>
```

### 2. 注入客户端

```java
@Service
public class UserService {
    
    @Autowired
    private FileClientWrapper fileClient;
    
    @Autowired
    private NotificationClientWrapper notificationClient;
    
    public void register(UserDTO user) {
        // 发送短信验证码
        notificationClient.sendSms(user.getPhone(), "123456");
        
        // 上传头像
        String avatarUrl = fileClient.upload(avatarFile);
    }
}
```

---

## 📝 开发规范

### 包命名规范

- API 模块：`com.youyu.basics.api.{功能域}.*`
- 实现模块：`com.youyu.basics.{层级}.{功能域}.*`
- SDK 模块：`com.youyu.basics.sdk.*`

### 功能域扩展

如果需要添加新功能域（如 `message`）：

1. 在 API 模块创建：`api/message/dto/`
2. 在 impl 模块创建：
   - `application/message/`
   - `domain/message/`
   - `infrastructure/message/`
3. 在 interfaces 添加：`MessageController.java`

---

## 🔄 未来演进

### 当前阶段（单体公共服务）
```
youyu-basics/
├── file/
└── notification/
```

### 未来可能的拆分（独立微服务）
```
youyu-file/          # 独立文件服务
youyu-notification/  # 独立通知服务
```

**拆分时机**：
- 某个功能域的 QPS 非常高，需要独立扩展
- 不同功能域的技术栈差异很大
- 团队规模扩大，需要独立维护

---

## 📊 模块统计

| 子模块 | 说明 |
|--------|------|
| youyu-basics-api | API 定义（DTO、Client 接口） |
| youyu-basics-impl | 业务实现（DDD 四层） |
| youyu-basics-sdk | 客户端封装 |
| youyu-basics-bootstrap | 启动入口 |

**总计**：1 个主模块，4 个子模块，2 个功能域

---

**最后更新**: 2026-04-26  
**维护者**: Alan
