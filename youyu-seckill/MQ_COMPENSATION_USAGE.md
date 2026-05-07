# YouYu-Seckill MQ 补偿机制配置说明

## 一、概述

YouYu-Seckill 模块已集成框架层的 MQ 消息补偿机制，无需自己实现补偿逻辑。

### 主要改进

1. ✅ **使用框架层的 `ReliableMessageProducer`**：自动处理消息发送失败时的补偿
2. ✅ **删除重复实现**：移除了 SeckillMessageCompensationService 等自定义补偿类
3. ✅ **统一补偿表**：使用框架层的 `mq_message_compensation` 表
4. ✅ **简化维护**：补偿逻辑由框架层统一管理，业务方只需配置即可

---

## 二、配置步骤

### 1. 执行 SQL 初始化脚本

执行框架层的补偿表脚本：

```bash
# 执行 youyu-framework/doc/mq-compensation-table.sql
mysql -u root -p your_database < youyu-framework/doc/mq-compensation-table.sql
```

或者手动执行：

```sql
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
    UNIQUE KEY `uk_message_id_topic_tag` (`message_id`, `topic`, `tag`),
    KEY `idx_status` (`status`),
    KEY `idx_next_retry_time` (`next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消息补偿表';
```

### 2. 配置 application.yml

在 `youyu-seckill-bootstrap/src/main/resources/application.yml` 中添加：

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

# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/youyu_seckill?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

### 3. 启用定时任务

确保 Spring Boot 启用了定时任务支持：

```java
@SpringBootApplication
@EnableScheduling  // 添加此注解
public class YouYuSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(YouYuSeckillApplication.class, args);
    }
}
```

---

## 三、使用方式

### 发送消息（自动补偿）

在业务代码中使用 `SeckillOrderMessageProducer`：

```java
@Service
public class SeckillOrderService {
    
    @Autowired
    private SeckillOrderMessageProducer messageProducer;
    
    public void createSeckillOrder(SeckillOrder order) {
        // 1. 执行业务逻辑（扣减库存、创建订单等）
        // ...
        
        // 2. 发送 MQ 消息（自动补偿）
        messageProducer.send(
            order.getOrderId(),
            order.getUserId(),
            order.getProductId(),
            order.getQuantity(),
            order.getAmount(),
            order.getActivityId()
        );
    }
}
```

### 补偿任务自动执行

框架层已提供 `MqMessageCompensationTask`，每分钟自动执行一次补偿任务，无需额外配置。

---

## 四、架构说明

### 消息发送流程

```
┌─────────────────────────────────────────────────────────┐
│              SeckillOrderService                         │
│                   ↓                                      │
│         SeckillOrderMessageProducer                      │
│                   ↓                                      │
│      ReliableMessageProducer (框架层)                     │
│     ┌──────────────────────────────┐                    │
│     │ 1. 异步发送 MQ 消息           │                    │
│     │ 2. 成功 → 记录日志            │                    │
│     │ 3. 失败 → 记录补偿表          │                    │
│     └──────────────────────────────┘                    │
└─────────────────────────────────────────────────────────┘
```

### 补偿执行流程

```
┌─────────────────────────────────────────────────────────┐
│         MqMessageCompensationTask (@Scheduled)           │
│                   ↓                                      │
│      MessageCompensationService (框架层)                  │
│     ┌──────────────────────────────┐                    │
│     │ 1. 查询待处理的补偿记录       │                    │
│     │ 2. 重新发送 MQ 消息           │                    │
│     │ 3. 成功 → 标记为成功          │                    │
│     │ 4. 失败 → 增加重试次数        │                    │
│     │ 5. 超过最大重试 → 标记失败    │                    │
│     └──────────────────────────────┘                    │
└─────────────────────────────────────────────────────────┘
```

---

## 五、注意事项

### 1. 分布式环境下的并发控制

当前 `MqMessageCompensationTask` 使用 `@Scheduled` 定时执行，在单机部署时无问题。

**分布式部署时：**

#### 方案1：使用 XXL-Job（推荐）

```java
@Component
public class MqMessageCompensationJob {
    
    @Autowired
    private MessageCompensationService compensationService;
    
    @XxlJob("mqCompensationJob")
    public void execute() {
        compensationService.execute();
    }
}
```

#### 方案2：使用 Redisson 分布式锁

```java
@Component
public class MqMessageCompensationTask {
    
    @Autowired
    private MessageCompensationService compensationService;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Scheduled(fixedRate = 60 * 1000)
    public void executeCompensation() {
        RLock lock = redissonClient.getLock("seckill-mq-compensation-lock");
        try {
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

### 2. 监控与告警

建议监控以下指标：

```sql
-- 查询待处理的补偿记录数量
SELECT COUNT(*) FROM mq_message_compensation WHERE status = 0;

-- 查询失败的补偿记录
SELECT * FROM mq_message_compensation WHERE status = 3;

-- 查询超过最大重试次数的记录
SELECT * FROM mq_message_compensation 
WHERE retry_count >= max_retry_count AND status != 2;
```

当出现大量补偿失败时，应及时告警并人工介入。

### 3. 降级方案

如果框架层的 `ReliableMessageProducer` 未配置（如未启用补偿机制），`SeckillOrderMessageProducer` 会自动降级为直接发送模式（无补偿机制）。

日志会输出：
```
可靠消息生产者未配置，使用直接发送方式，orderId: xxx
```

---

## 六、常见问题

### Q1: 为什么要使用框架层的补偿机制？

A: 
- **避免重复实现**：多个微服务都需要补偿机制，统一由框架层管理
- **降低维护成本**：补偿逻辑修改只需改一处
- **提高可靠性**：框架层经过充分测试和优化

### Q2: 可以自定义补偿表名吗？

A: 可以，在配置文件中指定：

```yaml
mq:
  compensation:
    table-name: my_custom_compensation_table
```

### Q3: 如何调整补偿任务的执行频率？

A: 修改 `MqMessageCompensationTask` 中的 `@Scheduled` 注解：

```java
@Scheduled(fixedRate = 30 * 1000)  // 改为30秒
```

或者使用 cron 表达式：

```java
@Scheduled(cron = "0 */2 * * * ?")  // 每2分钟执行一次
```

### Q4: 如何处理超过最大重试次数的消息？

A: 
1. 查询失败记录：`SELECT * FROM mq_message_compensation WHERE status = 3`
2. 分析失败原因（网络问题、MQ 故障、消息格式错误等）
3. 修复问题后，可以：
   - 手动更新状态为待处理：`UPDATE mq_message_compensation SET status = 0 WHERE id = xxx`
   - 或者重新触发补偿任务

---

## 七、技术栈对比

### 之前（自己实现）

- ❌ SeckillMessageCompensationService（自定义）
- ❌ SeckillMessageCompensationRepository（自定义）
- ❌ SeckillMessageCompensationDO（自定义）
- ❌ SeckillMessageCompensationMapper（自定义）
- ❌ SeckillOrderCompensationTask（自定义）
- ❌ 表名：`seckill_message_compensation`

### 现在（使用框架层）

- ✅ ReliableMessageProducer（框架层）
- ✅ MessageCompensationService（框架层）
- ✅ JdbcMessageCompensationRepository（框架层）
- ✅ MqMessageCompensationTask（使用框架层服务）
- ✅ 表名：`mq_message_compensation`（统一）

**代码量减少：约 400+ 行**

---

## 八、总结

通过使用框架层的 MQ 补偿机制，YouYu-Seckill 模块：

1. ✅ **消除了重复代码**：删除了 8 个自定义补偿类
2. ✅ **降低了维护成本**：补偿逻辑由框架层统一管理
3. ✅ **提高了可靠性**：使用经过充分测试的框架层实现
4. ✅ **简化了配置**：只需在 application.yml 中启用即可
5. ✅ **保持了灵活性**：支持自定义表名、重试策略等

**建议其他微服务也采用相同的方式，使用框架层的补偿机制。**
