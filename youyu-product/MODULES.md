# Product Service 模块说明

## 模块架构

Product Service 采用 DDD 分层架构,当前为单领域服务:

```
product-service/
├── youyu-product-api          # API契约层(预留)
├── youyu-product-impl         # 核心实现层(包含 interfaces,application,domain,infrastructure)
├── youyu-product-sdk          # SDK工具层(预留)
├── product-service-external     # 防腐层(预留)
└── youyu-product-bootstrap    # 启动装配层
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

### 1. youyu-product-api (API契约层)

**包结构:**
```
com.youyu.product.api/
├── client/                    # Feign Client 接口(预留)
│   └── ProductFeignClient.java
└── dto/                       # 数据传输对象(预留)
    ├── ProductQueryRequest.java
    └── ProductResponse.java
```

**职责:**
- 定义对外提供的 Feign Client 接口(预留)
- 定义商品相关 DTO(预留)
- 供其他微服务依赖调用

**依赖关系:**
- 不依赖任何其他 product-service 模块
- 被其他微服务依赖

---

### 2. youyu-product-impl (核心实现层)

**职责:**
- 实现 DDD 四层架构的核心业务逻辑
- 包含 interfaces、application、domain、infrastructure 四个层次

详细分层说明见下文 [Impl 内部 DDD 分层架构](#impl-内部-ddd-分层架构)

---

### 3. youyu-product-sdk (SDK工具层)

**包结构:**
```
com.youyu.product.sdk/
└── client/                    # SDK 客户端(预留)
    └── ProductQueryClient.java
```

**职责:**
- 提供便捷的商品查询客户端(预留)
- 封装复杂的查询逻辑
- 供其他服务使用

**依赖关系:**
- 依赖 youyu-product-api

---

### 4. product-service-external (防腐层)

**包结构:**
```
com.youyu.product.external/
└── ...                        # 外部系统集成(预留)
```

**职责:**
- 外部系统集成(预留)

**依赖关系:**
- 依赖 youyu-product-api

---

### 5. youyu-product-bootstrap (启动装配层)

**包结构:**
```
com.youyu.product.bootstrap/
└── ProductServiceApplication.java  # Spring Boot 启动类

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
@EnableFeignClients(basePackages = "com.youyu.product")
@MapperScan("com.youyu.product.infrastructure.persistence.mapper")
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

**依赖关系:**
- 依赖 youyu-product-impl
- 依赖 youyu-product-api

---

## Impl 内部 DDD 分层架构

### 1. Interfaces 层(用户接口层)

**包结构:**
```
interfaces/
└── controller/                # Web API 接口(HTTP/REST)
    ├── ProductController.java # 商品控制器
    └── StockController.java   # 库存控制器
```

**职责:**
- ✅ 所有外部系统的入口点
- ✅ 接收外部请求
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
    └── ProductApplicationService.java  # 商品应用服务
```

**职责:**
- ✅ 协调领域对象完成业务用例
- ✅ 商品 CRUD 操作
- ✅ 库存管理
- ✅ 分类管理
- ✅ 价格历史追踪
- ❌ **不包含核心业务规则**

**依赖关系:**
- Application Service → Domain Service + Repository 接口

---

### 3. Domain 层(领域层) ⭐核心

**包结构:**
```
domain/
├── model/                     # 领域模型
│   ├── ProductAggregate.java  # 商品聚合根
│   ├── Category.java          # 分类实体
│   └── PriceHistory.java      # 价格历史值对象
├── repository/                # 仓储接口
│   └── ProductRepository.java
└── service/                   # 领域服务
    └── StockDomainService.java
```

**职责:**
- ✅ 商品聚合根
- ✅ 分类实体
- ✅ 价格历史值对象
- ✅ 领域服务(库存扣减逻辑)
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
└── cache/                     # 缓存模块
    └── ProductCacheService.java
```

**职责:**
- ✅ 数据库访问(MyBatis)
- ✅ 缓存实现(Redis)
- ✅ 只负责技术实现,不包含业务逻辑

**为什么 Infrastructure 是包而不是独立模块?**
- ✅ 当前 product-service 只有**商品一个领域**,没有代码重复
- ✅ Infrastructure 作为包放在 impl 内,结构简单清晰
- ✅ 符合 DDD 标准分层,易于理解和维护

---

## ⚠️ 注意事项

1. **领域层是核心**,不应该依赖任何其他层
2. **基础设施层**通过实现领域层的接口来提供技术服务
3. **应用层**协调领域对象,但不包含业务规则
4. **接口层**只做参数校验和响应格式化,不做业务判断
5. 库存扣减使用 Redis Lua 脚本保证原子性
