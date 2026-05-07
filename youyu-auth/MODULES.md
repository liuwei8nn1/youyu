# Auth Service 模块说明

## 模块架构

Auth Service 采用 DDD 分层架构,当前为单领域服务:

```
auth-service/
├── youyu-auth-api          # API契约层(预留)
├── youyu-auth-impl         # 核心实现层(包含 interfaces,application,domain,infrastructure)
├── youyu-auth-sdk          # SDK工具层(预留)
├── auth-service-external     # 防腐层(预留)
└── youyu-auth-bootstrap    # 启动装配层
```

---

## 依赖方向(符合依赖倒置原则)

```
interfaces → application → domain ← infrastructure
                                ↑
                           实现 domain 的接口
```

**具体依赖:**
- Controller 依赖 Application Service
- Application Service 依赖 Domain Service + Repository 接口
- Domain Service 不依赖任何基础设施
- Infrastructure 实现 Repository 接口(依赖倒置)

---

## 各模块职责

### 1. youyu-auth-api (API契约层)

**包结构:**
```
com.youyu.auth.api/
├── client/                    # Feign Client 接口(预留)
│   └── AuthFeignClient.java
└── dto/                       # 数据传输对象(预留)
    ├── LoginRequest.java
    └── LoginResponse.java
```

**职责:**
- 定义对外提供的 Feign Client 接口(预留)
- 定义认证相关 DTO(预留)
- 供其他微服务依赖调用

**依赖关系:**
- 不依赖任何其他 auth-service 模块
- 被其他微服务依赖

---

### 2. youyu-auth-impl (核心实现层)

**职责:**
- 实现 DDD 四层架构的核心业务逻辑
- 包含 interfaces、application、domain、infrastructure 四个层次

详细分层说明见下文 [Impl 内部 DDD 分层架构](#impl-内部-ddd-分层架构)

---

### 3. youyu-auth-sdk (SDK工具层)

**包结构:**
```
com.youyu.auth.sdk/
└── client/                    # SDK 客户端(预留)
    └── AuthServiceClient.java
```

**职责:**
- 提供便捷的认证客户端(预留)
- 封装 Token 验证逻辑
- 供其他服务使用

**依赖关系:**
- 依赖 youyu-auth-api

---

### 4. auth-service-external (防腐层)

**包结构:**
```
com.youyu.auth.external/
├── sms/                       # 短信服务(预留)
│   └── SmsService.java
└── oauth/                     # OAuth第三方登录(预留)
    └── OAuthProvider.java
```

**职责:**
- 短信服务适配器(预留)
- OAuth 第三方登录适配器(预留)

**依赖关系:**
- 依赖 youyu-auth-api

---

### 5. youyu-auth-bootstrap (启动装配层)

**包结构:**
```
com.youyu.auth.bootstrap/
└── AuthServiceApplication.java  # Spring Boot 启动类

resources/
├── application.yml               # 主配置文件
├── application-dev.yml           # 开发环境配置
├── application-test.yml          # 测试环境配置
└── application-prod.yml          # 生产环境配置
```

**职责:**
- Spring Boot 启动类
- application.yml 配置文件
- @EnableFeignClients 配置
- @MapperScan 配置

**启动类示例:**
```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.youyu.auth")
@MapperScan("com.youyu.auth.infrastructure.persistence.mapper")
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

**依赖关系:**
- 依赖 youyu-auth-impl
- 依赖 youyu-auth-api

---

## Impl 内部 DDD 分层架构

### 1. Interfaces 层(用户接口层)

**包结构:**
```
interfaces/
├── controller/                # Web API 接口(HTTP/REST)
│   ├── AuthController.java    # 认证控制器
│   ├── AdminController.java   # 管理控制器
│   └── UserController.java    # 用户控制器
└── dto/                       # 请求/响应 DTO
    ├── LoginRequest.java
    ├── LoginResponse.java
    └── DeviceInfoDTO.java
```

**职责:**
- ✅ 所有外部系统的入口点(Web API、RPC、消息监听、定时任务等)
- ✅ 接收外部请求
- ✅ 参数校验和格式转换
- ✅ 身份认证和授权
- ✅ 调用应用服务
- ✅ 返回响应数据
- ❌ **不包含业务逻辑**

**依赖关系:**
- Controller → Application Service

---

### 2. Application 层(应用层)

**包结构:**
```
application/
└── service/
    ├── LoginApplicationService.java      # 登录应用服务
    └── DeviceManagementService.java      # 设备管理服务
```

**职责:**
- ✅ 协调领域对象完成业务用例
- ✅ 事务管理
- ✅ 缓存管理
- ✅ 权限校验(业务级别)
- ✅ 调用外部系统(通过基础设施层)
- ❌ **不包含核心业务规则**

**依赖关系:**
- Application Service → Domain Service + Repository 接口

---

### 3. Domain 层(领域层) ⭐核心

**包结构:**
```
domain/
├── model/                     # 领域模型
│   ├── UserSession.java       # 用户会话聚合根
│   └── Device.java            # 设备实体
├── repository/                # 仓储接口
│   └── UserSessionRepository.java
└── service/                   # 领域服务
    └── TokenDomainService.java
```

**职责:**
- ✅ 聚合根(Aggregate Root)
- ✅ 实体(Entity)和值对象(Value Object)
- ✅ 领域服务(Domain Service)
- ✅ 仓储接口(Repository Interface)
- ✅ 领域事件(Domain Event)
- ✅ **包含所有核心业务规则和逻辑**
- ❌ **不依赖任何框架和基础设施**

**设计原则:**
- 领域层是核心,不应该依赖任何其他层
- 通过 Repository 接口与基础设施层解耦(依赖倒置原则)

---

### 4. Infrastructure 层(基础设施层)

**包结构:**
```
infrastructure/
├── config/                    # 配置类
│   └── SecurityConfig.java
├── persistence/               # 持久化模块
│   ├── entity/                # DO (Data Object)
│   ├── mapper/                # MyBatis Mapper
│   ├── converter/             # MapStruct 转换器
│   └── repository/            # Repository 实现
└── security/                  # 安全相关实现
    └── JwtTokenProvider.java
```

**职责:**
- ✅ 数据库访问(MyBatis)
- ✅ 缓存实现(Redis)
- ✅ JWT Token 生成和验证
- ✅ 密码加密
- ✅ 只负责技术实现,不包含业务逻辑

**为什么 Infrastructure 是包而不是独立模块?**
- ✅ 当前 auth-service 只有**认证一个领域**,没有代码重复
- ✅ Infrastructure 作为包放在 impl 内,结构简单清晰
- ✅ 符合 DDD 标准分层,易于理解和维护

---

## ⚠️ 注意事项

1. **领域层是核心**,不应该依赖任何其他层
2. **基础设施层**通过实现领域层的接口来提供技术服务
3. **应用层**协调领域对象,但不包含业务规则
4. **接口层**只做参数校验和响应格式化,不做业务判断
5. DTO 转换应该在应用层或基础设施层完成,不要在领域层
