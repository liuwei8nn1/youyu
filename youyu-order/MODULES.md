# Order Service 模块说明

## 模块架构

Order Service 采用 DDD 分层架构,当前为单领域服务:

```
order-service/
├── youyu-order-api          # API契约层(Feign Client + DTO)
├── youyu-order-impl         # 核心实现层(包含 interfaces,application,domain,infrastructure)
├── youyu-order-sdk          # SDK工具层(可选)
└── youyu-order-bootstrap    # 启动装配层
```

---

## 依赖方向(符合依赖倒置原则)

```
interfaces → application → domain ← infrastructure
                                ↑
                           实现 domain 的接口
```

**具体依赖:**
- Controller/Listener 依赖 Application Service
- Application Service 依赖 Domain Service + Repository 接口
- Domain Service 不依赖任何基础设施
- Infrastructure 实现 Repository 接口(依赖倒置)

---

## 各模块职责

### 1. youyu-order-api (API契约层)

**包结构:**
```
com.youyu.order.api/
├── client/                    # Feign Client 接口
│   └── OrderFeignClient.java  # 订单查询接口
└── dto/                       # 数据传输对象
    ├── OrderQueryRequest.java
    ├── OrderResponse.java
    └── SeckillOrderCreateRequest.java
```

**职责:**
- 定义对外提供的 Feign Client 接口
- 定义订单相关 DTO
- 供其他微服务依赖调用

**示例:**
- `OrderFeignClient`: 订单查询接口
- `SeckillOrderCreateRequest`: 秒杀订单创建请求

**依赖关系:**
- 不依赖任何其他 order-service 模块
- 被其他微服务依赖

---

### 2. youyu-order-impl (核心实现层)

**职责:**
- 实现 DDD 四层架构的核心业务逻辑
- 包含 interfaces、application、domain、infrastructure 四个层次

详细分层说明见下文 [Impl 内部 DDD 分层架构](#impl-内部-ddd-分层架构)

---

### 3. youyu-order-sdk (SDK工具层)

**包结构:**
```
com.youyu.order.sdk/
└── client/                    # SDK 客户端
    └── OrderQueryClient.java  # 订单查询客户端(示例)
```

**职责:**
- 提供便捷的订单查询客户端(可选)
- 封装复杂的查询逻辑
- 供前端或其他服务使用

**使用场景:**
- 当查询逻辑复杂,需要封装多个 API 调用时
- 提供统一的错误处理和重试机制
- 简化调用方的代码

**依赖关系:**
- 依赖 youyu-order-api

---

### 4. youyu-order-bootstrap (启动装配层)

**包结构:**
```
com.youyu.order.bootstrap/
└── OrderServiceApplication.java  # Spring Boot 启动类

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
@EnableFeignClients(basePackages = "com.youyu.order")
@MapperScan("com.youyu.order.infrastructure.persistence.mapper")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

**依赖关系:**
- 依赖 youyu-order-impl
- 依赖 youyu-order-api

---

## Impl 内部 DDD 分层架构

### 1. Interfaces 层(用户接口层)

**包结构:**
```
interfaces/
├── controller/                # Web API 接口(HTTP/REST)
│   └── OrderController.java   # 订单控制器
└── listener/                  # 消息监听器(MQ Consumer)
    └── SeckillOrderListener.java  # 秒杀订单消费者
```

**职责:**
- ✅ 所有外部系统的入口点(Web API、MQ 消费者等)
- ✅ 接收外部请求/消息
- ✅ 参数校验和格式转换
- ✅ 调用应用服务
- ✅ 返回响应数据
- ❌ **不包含业务逻辑**

**依赖关系:**
- Controller/Listener → Application Service

---

### 2. Application 层(应用层)

**包结构:**
```
application/
└── service/
    └── OrderApplicationService.java  # 订单应用服务
```

**职责:**
- ✅ 协调领域对象完成业务用例
- ✅ 事务管理
- ✅ 支持普通订单和秒杀订单两种类型
- ❌ **不包含核心业务规则**

**依赖关系:**
- Application Service → Domain Service + Repository 接口

---

### 3. Domain 层(领域层) ⭐核心

**包结构:**
```
domain/
├── model/                     # 领域模型
│   ├── OrderAggregate.java    # 订单聚合根
│   └── ShippingAddress.java   # 收货地址值对象
├── repository/                # 仓储接口(端口)
│   ├── OrderRepository.java   # 订单仓储
│   ├── UserRepository.java    # 用户仓储
│   └── ProductRepository.java # 商品仓储
└── service/                   # 领域服务
    └── OrderDomainService.java
```

**职责:**
- ✅ 订单聚合根(支持 NORMAL 和 SECKILL 两种类型)
- ✅ 领域服务(订单创建逻辑)
- ✅ 仓储接口(定义契约,不依赖基础设施)
- ✅ **包含所有核心业务规则**
- ❌ **不依赖任何框架和基础设施**

**设计原则:**
- 领域层是核心,不应该依赖任何其他层
- 通过 Repository 接口与基础设施层解耦(依赖倒置原则)

---

### 4. Infrastructure 层(基础设施层)

**Infrastructure 包结构:**
```
infrastructure/
├── persistence/               # 数据持久化
│   ├── entity/                # DO (Data Object)
│   ├── mapper/                # MyBatis Mapper
│   ├── converter/             # MapStruct 转换器
│   └── repository/            # Repository 实现
│
├── external/                  # 外部服务集成 ⭐
│   ├── adapter/               # 适配器模式
│   │   ├── UserRepositoryImpl.java      # Feign → Java微服务
│   │   ├── ProductRepositoryImpl.java   # Feign → Java微服务
│   │   └── GoUserRepositoryImpl.java    # HTTP → Go服务(示例)
│   └── acl/                   # 防腐层(ACL)
│       └── GoUserServiceClient.java     # Go 服务HTTP客户端
│
└── messaging/                 # 消息队列
    ├── OrderTimeoutMessageProducer.java
    └── NormalStockRollbackMessageProducer.java
```

**为什么 Infrastructure 是包而不是独立模块?**
- ✅ 当前 order-service 只有**订单一个领域**,没有代码重复
- ✅ Infrastructure 作为包放在 impl 内,结构简单清晰
- ✅ 符合 DDD 标准分层,易于理解和维护

**External 子包说明:**
- **adapter/**: 适配器模式,将外部服务适配为领域层接口
  - `UserRepositoryImpl`: 通过 Feign 调用 user-service
  - `ProductRepositoryImpl`: 通过 Feign 调用 product-service
  - `GoUserRepositoryImpl`: 通过 HTTP 调用 Go 服务(示例)
- **acl/**: 防腐层(Anti-Corruption Layer)
  - `GoUserServiceClient`: 封装对 Go 服务的 HTTP 调用
  - 防止外部系统的变化影响领域层

**为什么需要 Adapter?**
1. **解耦**: Application Service 不直接依赖 Feign Client
2. **灵活**: 可以轻松切换不同的实现(Java/Go/Python)
3. **测试**: 可以 Mock Repository 接口进行单元测试
4. **DDD**: 符合六边形架构(Hexagonal Architecture)

---

## 未来演进(多领域场景)

当 order-service 扩展为多个领域(订单、支付、退款)时,需要重构为以下结构:

### 方案一:按领域拆分子模块(推荐)

```
order-service/
├── youyu-order-api/              # API契约层(保持不变)
│   └── com.youyu.order.api/
│       ├── client/                 # Feign Client
│       └── dto/                    # DTO
│
├── order-domain/                   # 订单领域模块 ⭐新增
│   ├── src/main/java/com/demo/order/domain/order/
│   │   ├── model/                  # 聚合根、值对象
│   │   ├── repository/             # 仓储接口
│   │   └── service/                # 领域服务
│   └── pom.xml
│
├── payment-domain/                 # 支付领域模块 ⭐新增
│   ├── src/main/java/com/demo/order/domain/payment/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── pom.xml
│
├── refund-domain/                  # 退款领域模块 ⭐新增
│   ├── src/main/java/com/demo/order/domain/refund/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── pom.xml
│
├── order-service-infrastructure/   # 基础设施层 ⭐独立模块
│   ├── src/main/java/com/demo/order/infrastructure/
│   │   ├── persistence/            # 数据持久化(共享)
│   │   │   ├── order/              # 订单相关DO/Mapper
│   │   │   ├── payment/            # 支付相关DO/Mapper
│   │   │   └── refund/             # 退款相关DO/Mapper
│   │   ├── external/               # 外部服务集成(共享)
│   │   │   ├── adapter/            # 适配器模式
│   │   │   │   ├── UserRepositoryImpl.java
│   │   │   │   ├── ProductRepositoryImpl.java
│   │   │   │   └── PaymentGatewayAdapter.java
│   │   │   └── acl/                # 防腐层
│   │   │       └── PaymentGatewayClient.java
│   │   └── messaging/              # 消息队列(共享)
│   │       ├── OrderMessageProducer.java
│   │       ├── PaymentMessageProducer.java
│   │       └── RefundMessageProducer.java
│   └── pom.xml
│
├── youyu-order-impl/             # 应用服务层 + 接口层(编排层) ⭐改造
│   ├── src/main/java/com/demo/order/
│   │   ├── interfaces/             # 接口层(控制器、消息监听器)
│   │   │   ├── controller/         # REST API 控制器
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── RefundController.java
│   │   │   └── listener/           # MQ 消息监听器
│   │   │       ├── OrderMessageListener.java
│   │   │       ├── PaymentMessageListener.java
│   │   │       └── RefundMessageListener.java
│   │   └── application/            # 应用服务层(业务编排)
│   │       ├── order/              # 订单应用服务
│   │       │   └── OrderApplicationService.java
│   │       ├── payment/            # 支付应用服务
│   │       │   └── PaymentApplicationService.java
│   │       └── refund/             # 退款应用服务
│   │           └── RefundApplicationService.java
│   └── pom.xml (依赖所有domain + infrastructure)
│
├── youyu-order-sdk/              # SDK工具层(保持不变)
└── youyu-order-bootstrap/        # 启动装配层(保持不变)
```

**关键变化说明:**

1. **Domain 层拆分**: 每个领域成为独立的 Maven 子模块
   - ✅ 避免循环依赖
   - ✅ 清晰的领域边界
   - ✅ 可独立测试和部署

2. **Infrastructure 独立**: 抽取为独立模块
   - ✅ 被所有 domain 模块共享
   - ✅ 统一管理外部依赖
   - ✅ 避免代码重复

3. **Application + Interfaces 层保留在 impl**: 作为编排层和接口层
   - ✅ **Interfaces 层**: 处理 HTTP 请求、MQ 消息等外部输入
   - ✅ **Application 层**: 协调多个领域完成业务流程
   - ✅ 事务边界控制
   - ✅ 依赖所有 domain + infrastructure

4. **为什么 Interfaces 不拆分到各 domain?**
   - ❌ Domain 层应该**纯粹**,只包含领域逻辑,不应依赖 Spring MVC
   - ❌ Controller 需要依赖 Application Service,如果放在 domain 会导致循环依赖
   - ✅ Interfaces 放在 impl 层,统一处理所有领域的对外接口
   - ✅ 按领域分包(`interfaces/controller/order/`, `interfaces/controller/payment/`)保持清晰

4. **依赖关系**:
   ```
   bootstrap → impl → (order-domain + payment-domain + refund-domain + infrastructure)
   
   impl 内部:
   - interfaces/controller → application service → domain service → repository interface
   - interfaces/listener → application service → domain service → repository interface
   
   infrastructure → (order-domain + payment-domain + refund-domain) [仅依赖仓储接口]
   api ← 其他微服务
   ```

---

### 方案二:保持单模块,按包划分领域(适用于简单场景)

如果领域间耦合较高或团队规模较小,可以保持单模块结构:

```
youyu-order-impl/
└── src/main/java/com/demo/order/
    ├── interfaces/
    ├── application/
    ├── domain/
    │   ├── order/                  # 订单领域包
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── service/
    │   ├── payment/                # 支付领域包
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── service/
    │   └── refund/                 # 退款领域包
    │       ├── model/
    │       ├── repository/
    │       └── service/
    └── infrastructure/
        ├── persistence/
        │   ├── order/
        │   ├── payment/
        │   └── refund/
        ├── external/
        └── messaging/
```

**适用场景:**
- 领域数量 ≤ 3个
- 团队规模小
- 领域间交互频繁
- 不需要独立部署领域

**优缺点对比:**

| 维度 | 方案一(多模块) | 方案二(单模块) |
|------|--------------|--------------|
| 复杂度 | 高 | 低 |
| 可维护性 | 高(边界清晰) | 中(需严格规范) |
| 编译速度 | 慢(模块多) | 快 |
| 团队协作 | 好(可并行开发) | 一般(易冲突) |
| 适用规模 | 中大型项目 | 小型项目 |

---

## ⚠️ 注意事项

1. **领域层是核心**,不应该依赖任何其他层
2. **基础设施层**通过实现领域层的接口来提供技术服务
3. **应用层**协调领域对象,但不包含业务规则
4. **接口层**只做参数校验和响应格式化,不做业务判断
5. 订单类型差异化配置在 OrderAggregate 中定义
