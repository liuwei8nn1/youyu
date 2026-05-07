# User Service 模块说明

## 模块架构

User Service 采用 DDD 分层架构,当前为单领域服务:

```
user-service/
├── youyu-user-api          # API契约层
├── youyu-user-impl         # 核心实现层(包含 interfaces,application,domain,infrastructure)
├── youyu-user-sdk          # SDK工具层
├── user-service-external     # 防腐层
└── youyu-user-bootstrap    # 启动装配层
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

### 1. youyu-user-api (API契约层)

**包结构:**
```
com.youyu.user.api/
├── client/                    # Feign Client 接口
│   └── UserFeignClient.java   # 用户查询接口
└── dto/                       # 数据传输对象
    ├── UserLoginInfo.java
    ├── UserProfileRequest.java
    └── UserProfileResponse.java
```

**职责:**
- 定义对外提供的 Feign Client 接口
- 定义用户相关 DTO
- 供其他微服务依赖调用

**示例:**
```java
@FeignClient(name = "user-service", path = "/api/user")
public interface UserFeignClient {
    @GetMapping("/by-username/{username}")
    Result<UserLoginInfo> getByUsername(@PathVariable("username") String username);
}
```

**依赖关系:**
- 不依赖任何其他 user-service 模块
- 被其他微服务依赖

---

### 2. youyu-user-impl (核心实现层)

**职责:**
- 实现 DDD 四层架构的核心业务逻辑
- 包含 interfaces、application、domain、infrastructure 四个层次

详细分层说明见下文 [Impl 内部 DDD 分层架构](#impl-内部-ddd-分层架构)

---

### 3. youyu-user-sdk (SDK工具层)

**包结构:**
```
com.youyu.user.sdk/
└── client/                    # SDK 客户端
    └── UserServiceClient.java # 带缓存的用户服务客户端
```

**职责:**
- 提供便捷的用户查询客户端
- 封装缓存 + Feign 调用逻辑
- 简化调用方的代码

**使用场景:**
```java
@Autowired
private UserServiceClient userServiceClient;

// 自动带缓存,无需关心Feign细节
UserLoginInfo user = userServiceClient.getByUsernameWithCache("admin");
```

**依赖关系:**
- 依赖 youyu-user-api

---

### 4. user-service-external (防腐层)

**包结构:**
```
com.youyu.user.external/
└── sms/                       # 短信服务
    ├── SmsService.java        # 短信服务接口
    ├── AliyunSmsService.java  # 阿里云实现
    └── TencentSmsService.java # 腾讯云实现
```

**职责:**
- 短信服务适配器
- 数据转换(第三方DTO → 领域对象)

**激活方式:**
```yaml
spring:
  profiles:
    active: aliyun  # 或 tencent
```

**依赖关系:**
- 依赖 youyu-user-api

---

### 5. youyu-user-bootstrap (启动装配层)

**包结构:**
```
com.youyu.user.bootstrap/
└── UserServiceApplication.java  # Spring Boot 启动类

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
@EnableFeignClients(basePackages = "com.youyu.user")
@MapperScan("com.youyu.user.impl.infrastructure.persistence.mapper")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

**依赖关系:**
- 依赖 youyu-user-impl
- 依赖 youyu-user-api

---

## Impl 内部 DDD 分层架构

### 1. Interfaces 层(用户接口层)

**包结构:**
```
interfaces/
└── controller/                # Web API 接口(HTTP/REST)
    ├── UserProfileController.java  # 用户资料控制器
    └── UserQueryController.java    # 用户查询控制器
```

**职责:**
- ✅ 所有外部系统的入口点
- ✅ 接收用户资料和查询请求
- ✅ 参数校验和格式转换
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
    └── UserProfileApplicationService.java  # 用户资料应用服务
```

**职责:**
- ✅ 协调领域对象完成业务用例
- ✅ 用户资料管理
- ✅ 收货地址管理
- ✅ 事务管理
- ❌ **不包含核心业务规则**

**依赖关系:**
- Application Service → Domain Service + Repository 接口

---

### 3. Domain 层(领域层) ⭐核心

**包结构:**
```
domain/
├── model/                     # 领域模型
│   ├── UserProfile.java       # 用户资料聚合根
│   └── Address.java           # 地址值对象
├── repository/                # 仓储接口
│   └── UserProfileRepository.java
└── service/                   # 领域服务
    └── UserDomainService.java
```

**职责:**
- ✅ 用户资料聚合根
- ✅ 地址值对象
- ✅ 领域服务
- ✅ 仓储接口
- ✅ **包含所有核心业务规则**
- ❌ **不依赖任何框架和基础设施**

**设计原则:**
- 领域层是核心,不应该依赖任何其他层
- 通过 Repository 接口与基础设施层解耦(依赖倒置原则)

---

### 4. Infrastructure 层(基础设施层)

**包结构:**
```
infrastructure/
├── persistence/               # 持久化模块
│   ├── entity/                # DO (Data Object)
│   ├── mapper/                # MyBatis Mapper
│   ├── converter/             # MapStruct 转换器
│   └── repository/            # Repository 实现
└── external/                  # 外部集成
    └── sms/                   # 短信服务
        └── SmsServiceImpl.java
```

**职责:**
- ✅ 数据库访问(MyBatis)
- ✅ 短信服务集成(阿里云/腾讯云)
- ✅ 只负责技术实现,不包含业务逻辑

**为什么 Infrastructure 是包而不是独立模块?**
- ✅ 当前 user-service 只有**用户一个领域**,没有代码重复
- ✅ Infrastructure 作为包放在 impl 内,结构简单清晰
- ✅ 符合 DDD 标准分层,易于理解和维护

---

## ⚠️ 注意事项

1. **领域层是核心**,不应该依赖任何其他层
2. **基础设施层**通过实现领域层的接口来提供技术服务
3. **应用层**协调领域对象,但不包含业务规则
4. **接口层**只做参数校验和响应格式化,不做业务判断
5. 用户模块无积分、成长值等功能(已裁剪)
6. SDK 层提供带缓存的便捷客户端,供其他微服务调用
