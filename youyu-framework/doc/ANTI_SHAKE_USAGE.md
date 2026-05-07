# AntiShake 防抖组件使用说明

## 快速开始

### 1. 在 Controller 方法上添加注解

```java
@PostMapping("/create")
@AntiShake(
    intervalMs = 1000,                              // 1秒内不允许重复
    keyPrefix = "order:create",                     // Key 前缀
    keyExpression = "#request.userId + ':' + #request.productId"  // SpEL 表达式
)
public Message<OrderResult> createOrder(@RequestBody CreateOrderRequest request) {
    // 业务逻辑
}
```

### 2. 配置缓存类型（application.yml）

```yaml
app:
  anti-shake:
    cache-type: LOCAL  # LOCAL（单机）| REDIS（集群）
```

## 三种 Key 生成方式

### 方式1：SpEL 表达式（推荐）✅

```java
@AntiShake(
    keyPrefix = "order:create",
    keyExpression = "#request.userId + ':' + #request.productId"
)
```

**优点：**
- 可读性强
- 灵活可控
- 调试方便

**常用表达式：**
```java
// 单个参数
keyExpression = "#userId"

// 对象属性
keyExpression = "#request.userId"

// 组合多个参数
keyExpression = "#request.userId + ':' + #request.productId + ':' + #request.quantity"

// 调用方法
keyExpression = "#request.getUserId().toString()"
```

### 方式2：自动生成 Key

```java
@AntiShake(
    keyPrefix = "order:create",
    useAutoKey = true  // 基于方法签名 + 参数 hash
)
```

**优点：**
- 无需手动写表达式
- 几乎不会冲突

**缺点：**
- Key 不可读（MD5 hash）
- 调试困难

### 方式3：强制指定缓存类型

```java
// 强制使用本地缓存（即使全局配置为 REDIS）
@AntiShake(
    keyPrefix = "order:create",
    keyExpression = "#request.userId",
    cacheType = CacheType.LOCAL
)

// 强制使用 Redis（即使全局配置为 LOCAL）
@AntiShake(
    keyPrefix = "order:create",
    keyExpression = "#request.userId",
    cacheType = CacheType.REDIS
)
```

## 完整示例

### 订单创建接口

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping("/create")
    @AntiShake(
        intervalMs = 1000,
        keyPrefix = "order:create",
        keyExpression = "#request.userId + ':' + #request.productId"
    )
    public Message<OrderResult> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResult result = orderApplicationService.createOrder(...);
        return Message.success(result);
    }
}
```

### 用户登录接口

```java
@PostMapping("/login")
@AntiShake(
    intervalMs = 2000,  // 2秒内不允许重复
    keyPrefix = "user:login",
    keyExpression = "#request.username"
)
public Message<LoginVO> login(@RequestBody LoginRequest request) {
    // ...
}
```

### 查询接口（较短的防抖时间）

```java
@GetMapping("/{orderId}")
@AntiShake(
    intervalMs = 500,  // 0.5秒
    keyPrefix = "order:query",
    keyExpression = "#orderId"
)
public Message<OrderVO> getOrder(@PathVariable Long orderId) {
    // ...
}
```

## 配置说明

### 单机部署（默认）

```yaml
app:
  anti-shake:
    cache-type: LOCAL  # 使用 Caffeine 本地缓存
```

**特点：**
- ✅ 性能最优（无网络开销）
- ✅ 适合单实例部署
- ❌ 多实例间不共享状态

### 集群部署

```yaml
app:
  anti-shake:
    cache-type: REDIS  # 使用 Redis 分布式缓存
```

**特点：**
- ✅ 多实例间共享状态
- ✅ 保证全局唯一性
- ⚠️ 有网络开销

## 国际化提示

被限流时会返回国际化消息：

**中文：** `操作过于频繁，请稍后重试`

**英文：** `Operation too frequent, please try again later`

## 注意事项

1. **Key 冲突避免**：推荐使用 SpEL 表达式明确指定 key 生成规则
2. **防抖时间设置**：
   - 高频操作（查询）：300-500ms
   - 普通操作（下单）：1000ms
   - 敏感操作（支付）：2000-3000ms
3. **缓存类型选择**：
   - 单机部署：LOCAL（性能优先）
   - 集群部署：REDIS（一致性优先）
4. **异常处理**：被限流时抛出 RuntimeException，由全局异常处理器统一处理

## 技术实现

- **本地缓存**：Caffeine（expireAfterWrite 策略）
- **分布式缓存**：Redis（SETNX 原子操作）
- **表达式解析**：Spring SpEL（带缓存优化）
- **AOP 切面**：基于注解拦截
