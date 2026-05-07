# MQ 消息补偿机制使用指南

## 一、功能说明

MQ 消息补偿机制用于保证消息的最终一致性，当 MQ 消息发送失败时，自动记录到数据库补偿表，由定时任务重试发送。

### 核心特性

1. **可靠消息发送**：提供 `ReliableMessageProducer`，支持同步/异步发送，失败自动记录补偿表
2. **自动补偿**：提供 `MessageCompensationService`，定时扫描补偿表并重试发送
3. **指数退避**：重试间隔采用指数退避策略（60s, 120s, 240s, 480s...）
4. **灵活配置**：支持自定义表名、重试次数、重试间隔等
5. **轻量级依赖**：基于 JdbcTemplate 实现，不强制依赖 MyBatis-Plus

---

## 二、快速开始

### 1. 执行 SQL 初始化脚本

```sql
-- 执行 youyu-framework/doc/mq-compensation-table.sql
CREATE TABLE IF NOT EXISTS `mq_message_compensation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_id` VARCHAR(100) NOT NULL COMMENT '消息ID（业务唯一标识）',
    `topic` VARCHAR(100) NOT NULL COMMENT 'MQ Topic',
    `tag` VARCHAR(50) DEFAULT NULL COMMENT 'MQ Tag',
    `message_body` TEXT NOT NULL COMMENT '消息体（JSON字符串）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-成功, 3-失败',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_next_retry_time` (`next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消息补偿表';
-- -- UNIQUE KEY `uk_message_id_topic_tag` (`message_id`, `topic`, `tag`), 可以根据业务需求添加
```

### 2. 添加依赖

在微服务的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.youyu</groupId>
    <artifactId>youyu-starter</artifactId>
</dependency>
```

### 3. 配置 application.yml

#### 生产环境配置

```yaml
# 启用 MQ 补偿机制
mq:
  compensation:
    enabled: true                          # 启用补偿机制
    table-name: mq_message_compensation    # 补偿表名（可选，默认：mq_message_compensation）
    max-retry-count: 3                     # 最大重试次数（可选，默认：3）
    retry-interval-seconds: 60             # 重试间隔秒数（可选，默认：60）
    batch-size: 100                        # 每次批量处理数量（可选，默认：100）

# RocketMQ 配置
rocketmq:
  name-server: ${ROCKETMQ_NAME_SERVER:localhost:9876}
  producer:
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
    retry-times-when-send-async-failed: 2

# 数据源配置（Spring Boot 自动配置）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

#### 本地/开发环境配置

```yaml
# 禁用 MQ 补偿机制（使用 Mock 实现）
mq:
  compensation:
    enabled: false                         # 禁用补偿机制，使用 Mock 实现

# RocketMQ 可以不配置，框架层会自动提供 Mock RocketMQTemplate
# rocketmq:
#   name-server: localhost:9876
```

**注意：**
- 本地开发时，不需要配置 `rocketmq.name-server`
- 框架层会自动创建 Mock `RocketMQTemplate` 和 Mock `ReliableMessageProducer`
- 所有 MQ 操作只记录日志，不会实际发送消息
- 业务代码无需修改，直接使用即可

### 4. 注入 ReliableMessageProducer 发送消息

```java
@Service
public class OrderService {
    
    @Autowired
    private ReliableMessageProducer messageProducer;
    
    public void createOrder(Order order) {
        // 1. 执行业务逻辑（保存订单、扣减库存等）
        orderRepository.save(order);
        
        // 2. 发送 MQ 消息（异步，推荐）
        String messageId = generateMessageId();
        messageProducer.sendAsync(
            "order-topic",
            "create",
            messageId,
            JSON.toJSONString(order)
        );
        
        // 或者使用同步发送
        // messageProducer.sendSync("order-topic", "create", messageId, JSON.toJSONString(order));
    }
}
```

### 5. 创建定时任务执行补偿

#### 方式1：使用 @Scheduled（单机部署）

```java
@Component
public class MqCompensationTask {
    
    @Autowired
    private MessageCompensationService compensationService;
    
    /**
     * 每1分钟执行一次补偿任务
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void executeCompensation() {
        compensationService.execute();
    }
}
```

#### 方式2：使用 XXL-Job（分布式部署）

```java
@Component
public class MqCompensationJob {
    
    @Autowired
    private MessageCompensationService compensationService;
    
    @XxlJob("mqCompensationJob")
    public void execute() {
        compensationService.execute();
    }
}
```

#### 方式3：手动触发（HTTP 接口）

```java
@RestController
@RequestMapping("/admin/mq-compensation")
public class MqCompensationController {
    
    @Autowired
    private MessageCompensationService compensationService;
    
    @PostMapping("/execute")
    public Result<Void> execute() {
        compensationService.execute();
        return Result.success();
    }
}
```

---

## 三、高级用法

### 1. 自定义 Repository 实现

如果需要使用 MyBatis-Plus 或其他 ORM 框架，可以自定义 Repository 实现：

```java
@Repository
public class CustomMessageCompensationRepository implements MessageCompensationRepository {
    
    @Autowired
    private CustomMapper customMapper;
    
    @Override
    public void save(MessageCompensationRecord record) {
        // 自定义实现
    }
    
    @Override
    public void update(MessageCompensationRecord record) {
        // 自定义实现
    }
    
    @Override
    public List<MessageCompensationRecord> findPendingCompensations(int limit) {
        // 自定义实现
    }
    
    @Override
    public MessageCompensationRecord findByMessageId(String messageId) {
        // 自定义实现
    }
}
```

框架会自动检测到自定义的 Bean，不会创建默认的 JdbcTemplate 实现。

### 2. 自定义表名

在配置文件中指定表名：

```yaml
mq:
  compensation:
    table-name: my_custom_compensation_table
```

### 3. 失败回调处理

当补偿记录保存失败时，可以通过回调处理：

```java
messageProducer.sendAsync(
    "order-topic",
    "create",
    messageId,
    JSON.toJSONString(order),
    (sendFail) -> {
        // 处理保存补偿表失败的情况
        log.error("补偿记录保存失败: {}", sendFail.messageId(), sendFail.e());
        // 可以触发告警、回滚业务操作等
    }
);
```

---

## 四、注意事项

### 1. 分布式环境下的并发控制

`MessageCompensationService.execute()` 方法使用了 `synchronized` 关键字保证单机线程安全。

**分布式环境下：**
- 如果使用 **XXL-Job** 等分布式任务调度框架，由其保证单实例执行
- 如果使用 **@Scheduled**，建议使用分布式锁（如 Redisson）避免多实例重复执行

示例（使用 Redisson 分布式锁）：

```java
@Component
public class MqCompensationTask {
    
    @Autowired
    private MessageCompensationService compensationService;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Scheduled(fixedRate = 60 * 1000)
    public void executeCompensation() {
        RLock lock = redissonClient.getLock("mq-compensation-lock");
        try {
            // 尝试获取锁，最多等待1秒，锁自动释放时间60秒
            if (lock.tryLock(1, 60, TimeUnit.SECONDS)) {
                compensationService.execute();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

### 2. 消息ID的唯一性

确保 `messageId` 在业务上是唯一的，建议使用：
- 雪花算法 ID
- UUID
- 业务唯一标识（如订单ID）

### 3. 补偿表索引优化

补偿表已创建以下索引：
- `uk_message_id`：唯一索引，防止重复记录
- `idx_status`：优化状态查询
- `idx_next_retry_time`：优化重试时间查询

如果查询性能不佳，可以考虑添加联合索引：
```sql
ALTER TABLE mq_message_compensation 
ADD INDEX idx_status_next_retry (`status`, `next_retry_time`);
```

### 4. 监控与告警

建议监控以下指标：
- 补偿表中待处理记录数量
- 补偿成功/失败次数
- 超过最大重试次数的记录数量

当出现大量补偿失败时，应及时告警并人工介入。

---

## 五、常见问题

### Q1: 为什么不用 MyBatis-Plus？

A: 框架层使用 JdbcTemplate 是为了降低集成方的依赖负担。集成方只需提供 DataSource 即可使用补偿机制，无需引入 MyBatis-Plus。如果集成方想使用 MyBatis-Plus，可以自定义 Repository 实现。

### Q2: 补偿任务执行频率多少合适？

A: 建议设置为 1 分钟。太频繁会增加数据库压力，太慢会影响消息实时性。可根据业务需求调整。

### Q3: 如何处理超过最大重试次数的消息？

A: 超过最大重试次数的消息会被标记为"失败"状态，需要人工介入处理。建议：
1. 设置告警通知（邮件/短信/钉钉）
2. 定期导出失败记录，人工排查原因
3. 修复问题后，手动重新触发补偿或重新发送消息

### Q4: 补偿机制会影响性能吗？

A: 影响很小：
- 正常发送成功时，无额外开销
- 发送失败时，需要插入一条补偿记录（异步发送不影响主流程）
- 补偿任务定时执行，批量处理，对数据库压力可控

---

## 六、技术架构

### Bean 创建流程

```
┌─────────────────────────────────────────────────────────┐
│              Spring Boot 启动                            │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  检查配置: mq.compensation.enabled                       │
└─────────────────────────────────────────────────────────┘
                    ↓
        ┌───────────┴───────────┐
        │                       │
    enabled=true           enabled=false
        │                       │
        ↓                       ↓
┌──────────────────┐   ┌──────────────────────┐
│ MqCompensation   │   │ MqCompensationMock   │
│ AutoConfiguration│   │ Config               │
│                  │   │                      │
│ 创建真实 Bean:   │   │ 检查环境:            │
│ - Repository     │   │ - local/dev → Mock   │
│ - Service        │   │ - prod → 抛异常      │
│ - Producer       │   │                      │
└──────────────────┘   └──────────────────────┘
        │                       │
        └───────────┬───────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  业务代码注入                                             │
│  - private final ReliableMessageProducer producer;      │
│  - 不需要 @Autowired(required = false)                   │
│  - 保证一定有值（真实或 Mock）                             │
└─────────────────────────────────────────────────────────┘
```

### 消息发送流程

```
┌─────────────────────────────────────────────────────────┐
│                   业务服务层                              │
│  ┌──────────────┐                                       │
│  │ OrderService │                                       │
│  └──────┬───────┘                                       │
│         │                                               │
│         ▼                                               │
│  ┌──────────────────────┐                               │
│  │ ReliableMessageProd. │  ← 发送消息，失败记录补偿表     │
│  └──────┬───────────────┘                               │
│         │                                               │
└─────────┼───────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────┐
│                 框架层 (youyu-framework)                  │
│                                                         │
│  ┌──────────────────────┐    ┌──────────────────────┐  │
│  │ MessageCompensation  │    │ JdbcMessageCompensa- │  │
│  │ Service              │◄───┤ tionRepository       │  │
│  │ (定时扫描补偿表)      │    │ (JdbcTemplate 实现)   │  │
│  └──────────────────────┘    └──────────┬───────────┘  │
│                                         │               │
│                                         ▼               │
│                              ┌──────────────────────┐  │
│                              │   MySQL 补偿表        │  │
│                              └──────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 七、更新日志

### v1.0.0 (2026-04-28)
- ✅ 基于 JdbcTemplate 实现，移除 MyBatis-Plus 依赖
- ✅ 支持自定义表名
- ✅ 添加 synchronized 保证单机线程安全
- ✅ 完善分布式环境下的使用说明
