# Seckill Service 模块说明

## 模块架构

Seckill Service 采用 DDD 分层架构,当前为单领域服务:

```
seckill-service/
├── youyu-seckill-api          # API契约层(预留)
├── youyu-seckill-impl         # 核心实现层(包含 infrastructure 包)
│   └── infrastructure/          # 基础设施层(包)
│       ├── persistence/         # 数据持久化
│       ├── messaging/           # 消息队列
│       └── config/              # 配置类
├── youyu-seckill-sdk          # SDK工具层(预留)
├── seckill-service-external     # 防腐层(预留)
└── youyu-seckill-bootstrap    # 启动装配层
```

---

## 依赖方向(符合依赖倒置原则)

```
interfaces → application → domain ← infrastructure
                                ↑
                           实现 domain 的接口
```

**具体依赖:**
- Controller/Consumer/Task 依赖 Application Service
- Application Service 依赖 Domain Service + Repository 接口
- Domain Service 不依赖任何基础设施
- Infrastructure 实现 Repository 接口(依赖倒置)

---

## 各模块职责

### 1. youyu-seckill-api (API契约层)

**包结构:**
```
com.youyu.seckill.api/
├── client/                    # Feign Client 接口(预留)
│   └── SeckillFeignClient.java
└── dto/                       # 数据传输对象(预留)
    ├── SeckillActivityQueryRequest.java
    └── SeckillActivityResponse.java
```

**职责:**
- 定义秒杀活动查询接口(预留)
- 定义秒杀相关DTO(预留)

**依赖关系:**
- 不依赖任何其他 seckill-service 模块
- 被其他微服务依赖

---

### 2. youyu-seckill-impl (核心实现层)

**职责:**
- 实现 DDD 四层架构的核心业务逻辑
- 包含 interfaces、application、domain、infrastructure 四个层次

详细分层说明见下文 [Impl 内部 DDD 分层架构](#impl-内部-ddd-分层架构)

---

### 3. youyu-seckill-sdk (SDK工具层)

**包结构:**
```
com.youyu.seckill.sdk/
└── client/                    # SDK 客户端(预留)
    └── SeckillQueryClient.java
```

**职责:**
- 提供秒杀活动查询客户端(预留)

**依赖关系:**
- 依赖 youyu-seckill-api

---

### 4. seckill-service-external (防腐层)

**包结构:**
```
com.youyu.seckill.external/
├── mq/                        # 消息队列适配器(预留)
│   └── RocketMQAdapter.java
└── lock/                      # 分布式锁适配器(预留)
    └── RedisLockAdapter.java
```

**职责:**
- 消息队列适配器(预留)
- 分布式锁适配器(预留)

**依赖关系:**
- 依赖 youyu-seckill-api

---

### 5. youyu-seckill-bootstrap (启动装配层)

**包结构:**
```
com.youyu.seckill.bootstrap/
└── SeckillServiceApplication.java  # Spring Boot 启动类

resources/
├── application.yml               # 主配置文件
├── application-dev.yml           # 开发环境配置
├── application-test.yml          # 测试环境配置
└── application-prod.yml          # 生产环境配置
```

**职责:**
- Spring Boot 启动类
- application.yml 配置文件

**启动类示例:**
```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.youyu.seckill")
@MapperScan("com.youyu.seckill.infrastructure.persistence.mapper")
public class SeckillServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillServiceApplication.class, args);
    }
}
```

**依赖关系:**
- 依赖 youyu-seckill-impl
- 依赖 youyu-seckill-api

---

## Impl 内部 DDD 分层架构

### 1. Interfaces 层(用户接口层)

**包结构:**
```
interfaces/
├── controller/                # Web API 接口(HTTP/REST)
│   └── SeckillController.java # 秒杀控制器
├── listener/                  # 消息监听器(MQ Consumer)
│   └── SeckillOrderListener.java  # 秒杀订单消费者
└── task/                      # 定时任务
    └── SeckillOrderCompensationTask.java  # 补偿任务
```

**职责:**
- ✅ 所有外部系统的入口点(Web API、MQ 消费者、定时任务)
- ✅ 接收秒杀请求
- ✅ 消费秒杀订单消息
- ✅ 补偿处理死信队列
- ❌ **不包含业务逻辑**

**依赖关系:**
- Controller/Listener/Task → Application Service

---

### 2. Application 层(应用层)

**包结构:**
```
application/
├── service/
│   └── SeckillOrderApplicationService.java  # 秒杀订单应用服务
└── dto/
    ├── SeckillOrderResponse.java
    └── SeckillOrderMessage.java
```

**职责:**
- ✅ 协调领域对象完成秒杀流程
- ✅ **核心流程**:
  1. 查询秒杀活动并校验
  2. 缓存秒杀价格到 Redis
  3. Redis Lua 原子扣减库存
  4. 记录用户购买数量
  5. 生成订单ID
  6. 发送 MQ 消息(异步创建订单)
  7. 立即返回排队结果
- ✅ 失败时不回滚库存,依靠幂等性保证
- ❌ **不包含核心业务规则**

**依赖关系:**
- Application Service → Domain Service + Repository 接口

---

### 3. Domain 层(领域层) ⭐核心

**包结构:**
```
domain/
├── model/                     # 领域模型
│   └── SeckillActivityAggregate.java  # 秒杀活动聚合根
├── repository/                # 仓储接口
│   └── SeckillActivityRepository.java
└── service/                   # 领域服务
    └── SeckillStockDomainService.java   # 库存领域服务(Redis Lua)
```

**职责:**
- ✅ 秒杀活动聚合根
- ✅ 库存领域服务(Redis Lua 脚本)
- ✅ 仓储接口(定义契约,不依赖基础设施)
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
├── config/                    # 配置类
│   └── SeckillRocketMQConfig.java
├── messaging/                 # MQ 生产者
│   └── SeckillOrderMessageProducer.java
└── persistence/               # 持久化模块
    ├── entity/                # DO (Data Object)
    ├── mapper/                # MyBatis Mapper
    ├── converter/             # MapStruct 转换器
    └── repository/            # Repository 实现
```

**职责:**
- ✅ RocketMQ 配置和消息生产
- ✅ 数据库访问(MyBatis)
- ✅ 只负责技术实现,不包含业务逻辑

**为什么 Infrastructure 是包而不是独立模块?**
- ✅ 当前 seckill-service 只有**秒杀一个领域**,没有代码重复
- ✅ Infrastructure 作为包放在 impl 内,结构简单清晰
- ✅ 符合 DDD 标准分层,易于理解和维护

---

## ⚠️ 注意事项

1. **领域层是核心**,不应该依赖任何其他层
2. **基础设施层**通过实现领域层的接口来提供技术服务
3. **应用层**协调领域对象,但不包含业务规则
4. **接口层**只做参数校验和响应格式化,不做业务判断
5. ⚠️ **关键**: MQ 消费失败时不回滚库存,依靠幂等性防止超卖
6. 超过重试次数的消息进入死信队列,由补偿任务处理
