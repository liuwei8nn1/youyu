# ProxyRequest 使用指南

## 概述

`ProxyRequest` 是一个便捷的请求上下文对象，可以在 Controller 方法参数中直接注入，提供对 `HttpServletRequest`、`HttpServletResponse` 和用户信息的快速访问。

## 核心功能

### 1. 基础属性访问

```java
@GetMapping("/example")
public Result<Void> example(ProxyRequest proxyRequest) {
    // 获取 HttpServletRequest
    HttpServletRequest request = proxyRequest.getRequest();
    
    // 获取 HttpServletResponse
    HttpServletResponse response = proxyRequest.getResponse();
    
    // 获取完整的 UserInfo 对象
    UserInfo userInfo = proxyRequest.getUserInfo();
    
    return Result.success();
}
```

### 2. 用户信息便捷访问

```java
@GetMapping("/user-info")
public Result<UserInfoVO> getUserInfo(ProxyRequest proxyRequest) {
    // 获取用户ID
    Long userId = proxyRequest.getUserId();
    
    // 获取用户名
    String username = proxyRequest.getUsername();
    
    // 获取用户类型
    Integer userType = proxyRequest.getUserType();
    
    // 判断是否已登录
    boolean isLogin = proxyRequest.isLogin();
    
    // 获取设备ID
    String deviceId = proxyRequest.getDeviceId();
    
    // 获取追踪ID
    String traceId = proxyRequest.getTraceId();
    
    // 获取角色列表
    String roles = proxyRequest.getRoles();
    
    UserInfoVO vo = new UserInfoVO();
    vo.setUserId(userId);
    vo.setUsername(username);
    // ... 设置其他字段
    
    return Result.success(vo);
}
```

### 3. 请求信息便捷访问

```java
@PostMapping("/request-info")
public Result<RequestInfoVO> getRequestInfo(ProxyRequest proxyRequest) {
    // 获取客户端IP地址（支持 X-Forwarded-For、X-Real-IP）
    String clientIp = proxyRequest.getClientIp();
    
    // 获取请求URI
    String uri = proxyRequest.getRequestUri();
    
    // 获取请求方法（GET/POST等）
    String method = proxyRequest.getRequestMethod();
    
    RequestInfoVO vo = new RequestInfoVO();
    vo.setClientIp(clientIp);
    vo.setUri(uri);
    vo.setMethod(method);
    
    return Result.success(vo);
}
```

### 4. 与其他参数组合使用

```java
@GetMapping("/list")
public Result<Page<AdminLogVO>> platformList(
        ProxyRequest proxyRequest, 
        @Validated OperLogQuery query) {
    
    // 记录操作日志：谁在什么时候做了什么
    log.info("用户 {} (ID: {}) 查询操作日志，IP: {}", 
             proxyRequest.getUsername(),
             proxyRequest.getUserId(),
             proxyRequest.getClientIp());
    
    // 可以根据用户权限过滤数据
    if (!proxyRequest.isLogin()) {
        throw new PermissionException("请先登录");
    }
    
    // 业务逻辑
    Page<AdminLogVO> page = adminLogService.query(query);
    
    return Result.success(page);
}
```

### 5. 实际业务场景示例

#### 场景1：记录操作审计日志

```java
@PostMapping("/update")
public Result<Void> updateConfig(
        ProxyRequest proxyRequest,
        @Validated @RequestBody ConfigUpdateDTO dto) {
    
    // 记录审计日志
    auditLogService.log(
        proxyRequest.getUserId(),      // 操作用户ID
        proxyRequest.getUsername(),    // 操作用户名
        proxyRequest.getClientIp(),    // 操作IP
        "UPDATE_CONFIG",               // 操作类型
        dto.getConfigKey()             // 操作内容
    );
    
    configService.update(dto);
    
    return Result.success();
}
```

#### 场景2：基于用户权限的数据过滤

```java
@GetMapping("/orders")
public Result<Page<OrderVO>> listOrders(
        ProxyRequest proxyRequest,
        OrderQuery query) {
    
    // 如果不是管理员，只能查看自己的订单
    if (!isAdmin(proxyRequest.getUserType())) {
        query.setUserId(proxyRequest.getUserId());
    }
    
    Page<OrderVO> orders = orderService.query(query);
    
    return Result.success(orders);
}
```

#### 场景3：设备绑定验证

```java
@PostMapping("/sensitive-operation")
public Result<Void> sensitiveOperation(
        ProxyRequest proxyRequest,
        @Validated @RequestBody SensitiveOpDTO dto) {
    
    // 验证当前设备是否与用户绑定
    String currentDeviceId = proxyRequest.getDeviceId();
    if (!deviceService.isBound(proxyRequest.getUserId(), currentDeviceId)) {
        throw new BusinessException("当前设备未绑定，请先绑定设备");
    }
    
    // 执行敏感操作
    sensitiveService.execute(dto);
    
    return Result.success();
}
```

## 扩展 ProxyRequest

如果后续需要添加更多便捷方法，只需在 `ProxyRequest` 类中添加即可：

```java
/**
 * 获取请求头中的某个值
 *
 * @param headerName 请求头名称
 * @return 请求头值
 */
public String getHeader(String headerName) {
    return request.getHeader(headerName);
}

/**
 * 获取 Cookie
 *
 * @param cookieName Cookie 名称
 * @return Cookie 值
 */
public String getCookie(String cookieName) {
    if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
    }
    return null;
}

/**
 * 判断是否为 AJAX 请求
 *
 * @return true-是 AJAX 请求
 */
public boolean isAjax() {
    return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
}
```

## 技术原理

### 1. HandlerMethodArgumentResolver

Spring MVC 提供了 `HandlerMethodArgumentResolver` 接口，允许自定义方法参数的解析逻辑。

**工作流程：**
1. Spring MVC 在调用 Controller 方法前，遍历所有注册的 `HandlerMethodArgumentResolver`
2. 调用 `supportsParameter()` 判断是否支持该参数类型
3. 如果支持，调用 `resolveArgument()` 解析参数值
4. 将解析后的值注入到方法参数中

### 2. 自动注册机制

通过 `WebMvcConfigurer.addArgumentResolvers()` 方法注册自定义解析器：

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ProxyRequestArgumentResolver());
    }
}
```

### 3. 与 UserContextHolder 集成

`ProxyRequest` 内部通过 `UserContextHolder.getUserInfo()` 获取当前用户信息，而 `UserContextHolder` 是由网关传递的请求头信息设置的，因此可以无缝获取用户上下文。

## 注意事项

1. **线程安全**：`ProxyRequest` 本身是无状态的，每个请求都会创建新的实例，因此是线程安全的。

2. **性能影响**：创建 `ProxyRequest` 对象的开销极小，只是简单的对象构造和引用赋值，不会对性能产生明显影响。

3. **非必需参数**：`ProxyRequest` 应该作为可选参数使用，不要强制所有 Controller 方法都使用它。

4. **与 Filter 的配合**：确保在 Filter 或 Interceptor 中已经调用了 `UserContextUtils.extractAndSet(request)` 设置了用户上下文。

## 总结

`ProxyRequest` 提供了一种优雅的方式来访问请求上下文和用户信息，避免了在每个 Controller 方法中重复编写获取用户信息的代码。它的设计遵循了以下原则：

- ✅ **简洁性**：一行代码即可获取常用信息
- ✅ **可扩展性**：可以轻松添加新的便捷方法
- ✅ **一致性**：与现有的 `UserContextHolder` 机制完美集成
- ✅ **标准化**：基于 Spring MVC 标准机制实现
