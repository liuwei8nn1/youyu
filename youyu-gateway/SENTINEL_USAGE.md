# Gateway Sentinel 限流配置说明

## 📋 概述

Gateway 服务已集成 Sentinel 限流组件,支持以下限流维度:
- ✅ **路由级别限流**: 保护后端服务不被压垮
- ✅ **IP 级别限流**: 防止单个 IP 刷接口
- ✅ **用户ID 级别限流**: 防止单个用户频繁请求

## 🚀 快速开始

### 1. 启动 Sentinel Dashboard

```bash
# 下载 Sentinel Dashboard
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar

# 启动 Dashboard
java -Dserver.port=8080 -jar sentinel-dashboard-1.8.6.jar

# 访问控制台
# http://localhost:8080
# 默认账号密码: sentinel / sentinel
```

### 2. 启动 Nacos (如果未启动)

```bash
# 确保 Nacos 已启动
# http://localhost:8848/nacos
```

### 3. 启动 Gateway 服务

首次启动时,`GatewayNacosSentinelConfigInitializer` 会自动将默认规则写入 Nacos。

## 📊 限流规则说明

### 当前配置的限流规则

| 维度 | 路由 | 限制值 | 说明 |
|------|------|--------|------|
| **路由总QPS** | order-service | 1000/s | 保护订单服务 |
| | seckill-service | 500/s | 秒杀服务更严格 |
| **IP限流** | order-service | 50/s | 防止单IP刷单 |
| | auth-public | 5/s | 防止暴力破解 |
| **用户ID限流** | order-service | 5/s | 单用户频率控制 |
| | seckill-service | 1/s | 秒杀严格限制 |
| **系统级限流** | 全局 | QPS=5000 | 保护整个应用 |
| | | CPU=85% | CPU使用率超过85%触发 |
| | | RT=5000ms | 平均响应时间超过5秒 |
| | | Thread=500 | 并发线程数超过500 |

### 规则文件位置

- **本地默认规则**: 
  - Gateway流控: `youyu-gateway-impl/src/main/resources/sentinel-default-rules/gateway-flow-rules.json`
  - 系统级限流: `youyu-gateway-impl/src/main/resources/sentinel-default-rules/gateway-system-rules.json`
- **Nacos 配置**: 
  - Gateway流控: `gateway-service-gateway-flow-rules`
  - 系统级限流: `gateway-service-system-rules`
  - Group: `DEFAULT_GROUP`
  - Namespace: `demo-cloud-dev` (开发环境)

### 完整 JSON 配置示例

#### 示例1: 路由级别限流 (不设置 paramItem)
```json
{
  "resource": "order-service",
  "resourceMode": 0,
  "grade": 1,
  "count": 1000,
  "intervalSec": 1,
  "controlBehavior": 0,
  "paramItem": null
}
```

#### 示例2: IP 级别限流 (parseStrategy=0)
```json
{
  "resource": "order-service",
  "resourceMode": 0,
  "grade": 1,
  "count": 50,
  "intervalSec": 1,
  "paramItem": {
    "parseStrategy": 0,
    "fieldName": null
  }
}
```

#### 示例3: 用户ID 限流 (parseStrategy=2, 从 Header 获取)
```json
{
  "resource": "order-service",
  "resourceMode": 0,
  "grade": 1,
  "count": 5,
  "intervalSec": 1,
  "paramItem": {
    "parseStrategy": 2,
    "fieldName": "X-User-Id"
  }
}
```

#### 示例4: 系统级限流 (保护整个应用)
```json
[
  {
    "highestSystemLoad": -1,
    "highestCpuUsage": 0.85,
    "avgRt": 5000,
    "maxThread": 500,
    "qps": 5000
  }
]
```

**系统级限流参数说明**:
- `highestSystemLoad`: 系统负载阈值 (-1表示不限制,仅Linux有效)
- `highestCpuUsage`: CPU使用率阈值 (0.85 = 85%)
- `avgRt`: 平均响应时间阈值 (毫秒)
- `maxThread`: 最大并发线程数
- `qps`: 全局QPS阈值

**注意**: 
- ⚠️ 系统级限流是**全局生效**的,不区分IP、用户、路由
- ⚠️ 任意一个指标超过阈值都会触发限流
- ❌ **系统级限流不支持按IP、用户ID等维度限流**
- ✅ 如需IP限流,请使用 Gateway Flow Rules (见示例2)

### 限流类型对比

| 限流类型 | 配置方式 | 是否需指定resource | 支持维度 | 适用场景 |
|---------|---------|------------------|---------|----------|
| **Gateway Flow Rules** | `gateway-flow-rules.json` | ✅ **必须指定** | 路由、IP、用户ID、Header、URL参数 | 细粒度业务限流 |
| **System Rules** | `system-rules.json` | ❌ **无需指定** (全局生效) | 仅全局指标 (QPS/CPU/RT/Thread) | 粗粒度系统保护 |

**重要说明**:
- ⚠️ **Gateway Flow Rules 必须指定 `resource`**,不能做真正的全局限流
- ⚠️ **System Rules 无需指定 `resource`**,自动对所有请求生效
- ✅ 如需全局限流,请使用 System Rules
- ✅ 如需按路由/IP/用户限流,请使用 Gateway Flow Rules

**选择建议**:
- 🎯 **IP限流**: 使用 Gateway Flow Rules + `parseStrategy=0`
- 🎯 **用户ID限流**: 使用 Gateway Flow Rules + `parseStrategy=2` + `fieldName=X-User-Id`
- 🎯 **系统保护**: 使用 System Rules 设置全局QPS、CPU等阈值

**参数说明**:
- `resource`: 路由ID (对应 application.yml 中的 route id)
- `resourceMode`: 0=路由模式, 1=API分组模式
- `grade`: 限流阈值类型 (1=QPS, 0=线程数)
- `count`: 限流阈值
- `intervalSec`: 统计窗口(秒)
- `paramItem.parseStrategy`: 参数解析策略
  - `0`: 客户端IP
  - `1`: Host
  - `2`: Header参数
  - `3`: URL参数
  - `4`: Cookie参数
- `paramItem.fieldName`: 当 parseStrategy=2/3/4 时,指定参数名

## 🔧 动态调整规则

### 方式1: 通过 Sentinel Dashboard (推荐)

1. 访问 http://localhost:8080
2. 选择 `gateway-service` 应用
3. 点击 "网关流控" 菜单
4. 可以实时查看监控数据和调整规则

**优点**: 
- ✅ 实时监控 QPS、响应时间等指标
- ✅ 可视化配置,操作简单
- ✅ 立即生效,无需重启

### 方式2: 通过 Nacos 控制台

1. 访问 http://localhost:8848/nacos
2. 进入 "配置管理" -> "配置列表"
3. 找到 `gateway-service-gateway-flow-rules`
4. 编辑 JSON 配置并发布

**JSON 格式示例**:

```json
[
  {
    "resource": "order-service",
    "resourceMode": 0,
    "grade": 1,
    "count": 1000,
    "intervalSec": 1,
    "controlBehavior": 0,
    "paramItem": null
  },
  {
    "resource": "order-service",
    "resourceMode": 0,
    "grade": 1,
    "count": 50,
    "intervalSec": 1,
    "paramItem": {
      "parseStrategy": 0,
      "fieldName": null
    }
  }
]
```

**参数说明**:
- `resource`: 路由ID (对应 application.yml 中的 route id)
- `resourceMode`: 0=路由模式, 1=API分组模式
- `grade`: 限流阈值类型 (1=QPS, 0=线程数)
- `count`: 限流阈值
- `intervalSec`: 统计窗口(秒)
- `paramItem.parseStrategy`: 参数解析策略
  - `0`: 客户端IP
  - `1`: Host
  - `2`: Header参数
  - `3`: URL参数
  - `4`: Cookie参数
- `paramItem.fieldName`: 当 parseStrategy=2/3/4 时,指定参数名

### 方式3: 修改代码中的默认规则

修改 `GatewaySentinelConfig.java` 中的 `initFlowRules()` 方法,然后重新部署。

## 💻 编程式限流检查

如果您需要在代码中自行判断是否已限流,可以使用 Sentinel API:

### 示例1: 在自定义 Filter 中手动检查

```java
@Component
public class CustomRateLimitFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String resourceName = "order-service";
        
        // 手动触发限流检查
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.IN);
            // 通过限流检查,继续执行
            return chain.filter(exchange);
        } catch (BlockException e) {
            // 被限流了,返回自定义响应
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            String body = "{\"code\":429,\"message\":\"请求过于频繁\"}";
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
            return response.writeWith(Mono.just(buffer));
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
    
    @Override
    public int getOrder() {
        return -2; // 在 SentinelGatewayFilter 之前执行
    }
}
```

### 示例2: 结合业务逻辑的限流

```java
@Service
public class OrderService {
    
    public Result createOrder(Long userId, OrderRequest request) {
        // 针对特定用户的限流
        String resourceName = "create_order_user_" + userId;
        
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName);
            // 执行业务逻辑
            return orderRepository.save(request);
        } catch (BlockException e) {
            return Result.fail("操作太频繁,请稍后重试");
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
```

**优点**:
- ✅ 完全控制限流逻辑
- ✅ 可以返回自定义响应
- ✅ 可以结合业务场景灵活处理

**缺点**:
- ❌ 需要手动管理 Entry 的生命周期
- ❌ 代码侵入性强

## 📝 添加新的限流规则

### 示例1: 按用户ID限制上传接口

```java
// 在 GatewaySentinelConfig.java 中添加
rules.add(new GatewayFlowRule("order-service")
    .setCount(2)                 // 每秒 2 次
    .setIntervalSec(1)
    .setParamItem(new GatewayParamFlowItem()
        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
        .setFieldName("X-User-Id")
    ));
```

### 示例2: 按 URL 参数限流

```java
// 例如: /api/product/detail?productId=123
rules.add(new GatewayFlowRule("product-service")
    .setCount(10)
    .setIntervalSec(1)
    .setParamItem(new GatewayParamFlowItem()
        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
        .setFieldName("productId")  // 按商品ID限流
    ));
```

### 示例3: API 分组限流

```java
// 1. 定义 API 分组
ApiDefinition groupApi = new ApiDefinition("group-api")
    .setPredicateItems(new HashSet<ApiPredicateItem>() {{
        add(new ApiPathPredicateItem().setPattern("/api/order/**"));
        add(new ApiPathPredicateItem().setPattern("/api/seckill/**"));
    }});

// 2. 对分组进行限流
rules.add(new GatewayFlowRule("group-api")
    .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
    .setCount(1500)
    .setIntervalSec(1));
```

## 🔍 监控与排查

### 查看实时数据

1. **Sentinel Dashboard**: 
   - 实时监控 QPS、响应时间、限流次数
   - 图表展示,直观易懂

2. **日志文件**:
   ```bash
   # Gateway 日志
   tail -f ~/logs/csp/gateway-service/metrics.log
   
   # Sentinel 内部日志
   tail -f ~/logs/csp/gateway-service/sentinel-record.log.xxx
   ```

### 限流触发后的行为

当请求被限流时,Gateway 会返回:
- **HTTP 状态码**: 429 (Too Many Requests)
- **响应体**: 由 `GatewayGlobalExceptionHandler` 统一处理

可以在 `application.yml` 中自定义降级响应:

```yaml
spring:
  cloud:
    sentinel:
      scg:
        fallback:
          mode: response
          response-status: 429
          response-body: '{"code":429,"message":"请求过于频繁,请稍后重试"}'
```

## ⚠️ 注意事项

### 1. 单机 vs 分布式限流

**当前配置是单机模式**:
- ✅ 性能好,无网络开销
- ❌ 不精准,每个实例独立计数

**如果需要分布式精准限流**:
- 方案A: 启用 Sentinel 集群模式 (需要 Token Server,复杂度高)
- 方案B: 业务层使用 Redis + Lua (推荐,如秒杀的双层限流)

### 2. 用户ID 获取机制

**Sentinel Gateway Filter 如何获取用户ID?**

```
请求流程:
1. GatewayLogFilter (生成 TraceId)
2. JwtFilter (解析 JWT,设置 UserInfo 到 attributes 和 Header)
   ├─ exchange.attributes[JWT_USERINFO] = UserInfo
   └─ Request Header: X-User-Id = userId
3. SentinelGatewayFilter (限流检查)
   └─ 从 Header "X-User-Id" 获取用户ID
4. PermissionFilter (权限检查)
5. 路由转发
```

**重要说明**:
- ✅ `fieldName` 必须与 JwtFilter 设置的 Header 名称一致
- ✅ 当前使用的是 `UserContextUtils.USER_ID_HEADER` (值为 "X-User-Id")
- ❌ **未登录用户没有 X-User-Id**,不会触发用户ID限流规则
- ✅ 未登录用户可以改用 IP 限流或 SmartKeyResolver

**SmartKeyResolver (智能降级)**:
```java
// 已登录: 使用用户ID
// 未登录: 降级使用 IP
@Component("smartKeyResolver")
public class SmartKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        UserInfo userInfo = exchange.getAttribute(JWT_USERINFO);
        if (userInfo != null && userInfo.getUserId() != null) {
            return Mono.just("user:" + userInfo.getUserId());
        }
        String clientIp = exchange.getRequest().getRemoteAddress()
            .getAddress().getHostAddress();
        return Mono.just("ip:" + clientIp);
    }
}
```

**使用 SmartKeyResolver**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 5
                key-resolver: "#{@smartKeyResolver}"
```

### 3. 规则优先级

多个规则同时生效时:
- 路由级别规则先判断
- 参数级别规则后判断
- 任意一个触发限流都会拒绝请求

### 4. 性能影响

- Sentinel 单机模式性能损耗 < 1%
- 建议定期清理过期规则
- 避免配置过多的细粒度规则

## 🎯 最佳实践

### Gateway 层 (粗粒度保护)
```
✅ 路由总 QPS 限流
✅ IP 频率限制 (防刷)
✅ 简单的用户ID限流
```

### 业务层 (细粒度控制)
```
✅ 双层限流 (Caffeine + Redis)
✅ 复杂的业务逻辑限流
✅ 秒杀等特殊场景
```

## 📚 相关文档

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/docs/gateway-flow-control.html)
- [Spring Cloud Alibaba Sentinel](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/en-us/index.html#_spring_cloud_alibaba_sentinel)
- 项目内文档: `doc/feature.md`

## 🆘 常见问题

### Q1: Sentinel Dashboard 看不到 gateway-service?

**原因**: Gateway 未启动或 Dashboard 地址配置错误

**解决**:
1. 检查 `application.yml` 中的 `sentinel.transport.dashboard` 配置
2. 确认 Dashboard 已启动
3. 检查防火墙/网络连通性

### Q2: 规则修改后不生效?

**原因**: Nacos 配置未刷新或 Dashboard 未推送

**解决**:
1. 检查 Nacos 配置是否发布成功
2. 在 Dashboard 中点击 "刷新" 按钮
3. 查看日志确认规则加载情况

### Q3: 如何测试限流是否生效?

**方法1: 使用 ab 压测**
```bash
ab -n 1000 -c 50 http://localhost:9000/api/order/list
```

**方法2: 使用 JMeter**
- 创建线程组,设置并发数
- 观察 Sentinel Dashboard 的实时监控

**方法3: 简单循环请求**
```bash
for i in {1..100}; do
  curl -H "X-User-Id: 123" http://localhost:9000/api/order/list
done
```
