# 请求日志和链路追踪使用指南

## 功能概述

本系统实现了完整的请求日志记录和分布式链路追踪功能：

1. **TraceId 全链路追踪** - 从网关到微服务的完整链路追踪
2. **用户上下文提取** - 从网关传递的请求头中提取用户信息
3. **可配置的请求日志** - 支持开关、采样率、Body 日志控制
4. **便捷的静态工具类** - 微服务中快速获取用户信息

## 架构设计

```
客户端请求
    ↓
Gateway (生成/传递 TraceId)
    ↓ X-Trace-Id, X-User-Id, X-Merchant-Id
微服务 (GlobalLogFilter)
    ↓ 提取用户信息，设置 MDC
业务代码 (RequestContextUtil)
    ↓ 获取当前用户信息
Log4j2 (打印 TraceId)
```

## 配置说明

### 1. Gateway 配置

Gateway 会自动处理 TraceId，无需额外配置。

### 2. 微服务配置（Nacos）

在 Nacos 配置中心添加以下配置：

```yaml
logging:
  request:
    enabled: true              # 是否启用请求日志（默认 true）
    sample-rate: 1.0           # 采样率：0.0-1.0（默认 1.0 = 100%）
    body-enabled: false        # 是否记录请求体（默认 false，性能考虑）
```

**配置建议：**
- **开发环境**: `enabled=true, sample-rate=1.0, body-enabled=true`
- **测试环境**: `enabled=true, sample-rate=1.0, body-enabled=false`
- **生产环境**: `enabled=true, sample-rate=0.1, body-enabled=false`

### 3. Log4j2 配置

base-starter 中的 `log4j2-spring.xml` 已配置好 TraceId 输出格式：

```xml
<!-- 控制台日志 -->
<Property name="CONSOLE_LOG_PATTERN">
  %clr{%d{yyyy-MM-dd HH:mm:ss.SSS}}{faint} 
  %clr{%5p} 
  %clr{%pid}{magenta} 
  %clr{---}{faint} 
  %clr{[%15.15t]}{faint} 
  %clr{[%X{traceId}]}{yellow}  <!-- TraceId 黄色显示 -->
  %clr{%-40.40c{1.}}{cyan} 
  %clr{:}{faint} 
  %m%n
</Property>

<!-- 文件日志 -->
<Property name="PATTERN">
  %d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${appName} [%X{traceId}] %c - %m%n
</Property>
```

`%X{traceId}` 会自动从 MDC 中获取 TraceId。

## 使用示例

### 1. 在业务代码中获取用户信息

```java
import com.youyu.starter.util.RequestContextUtil;
import com.youyu.context.UserInfo;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@RequestBody CreateOrderRequest request) {
        // 方式1：获取当前用户ID
        Long userId = RequestContextUtil.getCurrentUserId();
        
        // 方式2：获取完整用户信息
        UserInfo userInfo = RequestContextUtil.getCurrentUserInfo();
        Long merchantId = userInfo.getMerchantId();
        String traceId = userInfo.getTraceId();
        
        // 方式3：判断是否登录
        if (!RequestContextUtil.isLogin()) {
            return Result.error("请先登录");
        }
        
        // 方式4：获取 TraceId（用于日志关联）
        String traceId = RequestContextUtil.getCurrentTraceId();
        
        // 方式5：获取客户端 IP
        String clientIp = RequestContextUtil.getCurrentClientIp();
        
        // 业务逻辑...
        return Result.success(order);
    }
}
```

### 2. 在 Service 层使用

```java
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // 自动获取当前用户ID
        Long userId = RequestContextUtil.getCurrentUserId();
        String traceId = RequestContextUtil.getCurrentTraceId();
        
        log.info("创建订单，userId: {}, traceId: {}", userId, traceId);
        
        Order order = Order.create(userId, command.getProductId(), command.getQuantity());
        orderRepository.save(order);
        
        return order;
    }
}
```

### 3. 日志输出示例

**控制台日志（带颜色）：**
```
2024-01-15 10:30:45.123 INFO 12345 --- [nio-8080-exec-1] [a1b2c3d4e5f6g7h8] c.d.order.controller - 创建订单成功
```

**文件日志：**
```
2024-01-15 10:30:45.123 INFO auth-service-dev [a1b2c3d4e5f6g7h8] c.d.web.Logs - {"app":"auth-service-dev","userId":12345,"method":"POST","uri":"/api/auth/login","params":"username=admin","ip":"192.168.1.100","time":1705283445123,"useTimeMs":45,"response":"{\"code\":200,\"msg\":\"success\"}"}
```

## 网关请求头规范

网关在转发请求时会添加以下请求头：

| 请求头 | 说明 | 示例 |
|--------|------|------|
| X-Trace-Id | 链路追踪ID | `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6` |
| X-User-Id | 用户ID | `12345` |
| X-Merchant-Id | 商户ID（可选） | `67890` |

## 性能优化建议

### 1. 采样率控制

高并发场景下，建议降低采样率：

```yaml
logging:
  request:
    sample-rate: 0.01  # 只记录 1% 的请求
```

### 2. 关闭 Body 日志

请求体序列化会消耗 CPU 和内存，生产环境建议关闭：

```yaml
logging:
  request:
    body-enabled: false
```

### 3. 完全禁用日志

如果性能要求极高，可以完全禁用：

```yaml
logging:
  request:
    enabled: false
```

## 常见问题

### Q1: 为什么获取不到用户ID？

**原因：** 网关没有正确传递 `X-User-Id` 请求头。

**解决：** 检查网关的认证过滤器是否正确设置了请求头。

### Q2: TraceId 为什么为空？

**原因：** 可能不在请求上下文中（如定时任务、MQ 消费者）。

**解决：** `RequestContextUtil` 只能在 HTTP 请求线程中使用。异步线程需要手动传递 TraceId。

### Q3: 如何自定义日志格式？

**解决：** 修改 `base-starter/src/main/resources/log4j2-spring.xml` 中的 Pattern。

### Q4: 采样率是如何工作的？

**说明：** 采样率在 Filter 中通过 `Math.random()` 判断，如果随机数大于采样率则跳过日志记录。

例如 `sample-rate=0.1` 表示大约 10% 的请求会被记录。

## 技术细节

### TraceId 生成规则

- 格式：UUID 去掉横杠，32位十六进制字符串
- 示例：`a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`
- 生成位置：Gateway（首次请求）或微服务（直接调用微服务时）

### MDC 清理

`GlobalLogFilter` 会在请求结束后自动清理 MDC，避免内存泄漏：

```java
try {
    MDC.put("traceId", traceId);
    chain.doFilter(req, resp);
} finally {
    MDC.clear();  // 确保清理
}
```

### 线程安全

`RequestContextUtil` 基于 Spring 的 `RequestContextHolder`，是线程安全的。
