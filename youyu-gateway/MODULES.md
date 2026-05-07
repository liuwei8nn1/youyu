# Gateway Service 模块说明

## 模块架构

Gateway Service 采用三层模块化架构(无api和external):

```
gateway-service/
├── youyu-gateway-impl      # 网关核心实现（DDD领域模式）
├── youyu-gateway-sdk       # SDK工具层(用户信息解析)
└── youyu-gateway-bootstrap # 启动装配层
```

**注意:** Gateway不需要api模块(不提供Feign接口)和external模块(无第三方依赖)。

---

## 各模块职责

### 1. youyu-gateway-impl (网关核心实现)

**职责:**
- JWT过滤器(JwtFilter)
- 路由配置(GatewayConfig)
- 国际化配置(GatewayI18nConfig)
- 用户类型枚举(UserType)
- 权限级别枚举(PermissionLevel)
- JWT工具类(JwtUtil)
- 网关核心业务逻辑

**依赖:**
- Spring Cloud Gateway
- JWT (jjwt)
- Redis (用于查询用户信息)
- Nacos Discovery/Config

**包结构（符合DDD领域模式）:**
```
com.youyu.gateway/
├── config/           # 配置类（基础设施层）
│   ├── GatewayConfig.java
│   └── GatewayI18nConfig.java
├── filter/          # 过滤器（接口层 - 网关特有）
│   └── JwtFilter.java
├── enums/           # 枚举（领域层）
│   ├── UserType.java
│   └── PermissionLevel.java
├── util/            # 工具类（基础设施层）
│   └── JwtUtil.java
└── interfaces/      # 用户接口层（预留扩展）
```

**核心功能:**
- JWT Token验证
- 用户信息解析
- 权限检查
- 路由转发
- 国际化支持

---

### 2. youyu-gateway-sdk (SDK工具层)

**职责:**
- 提供用户信息解析工具
- 帮助其他微服务解析网关下发的用户信息
- 封装JWT解码逻辑

**使用场景:**
其他微服务需要解析网关传递的用户信息时使用:

```java
@Autowired
private GatewaySdkClient gatewaySdkClient;

// 解析网关传递的Token或用户信息
UserInfo userInfo = gatewaySdkClient.parseUserInfo(token);
```

**当前状态:**
- ⚠️ 占位模块,可根据实际需求扩展

---

### 3. youyu-gateway-bootstrap (启动装配层)

**职责:**
- Spring Boot启动类
- application.yml配置文件
- 网关路由配置

**依赖:**
- `youyu-gateway-impl`
- `youyu-gateway-sdk` (可选)
- Nacos Discovery/Config

**启动类:**
```java
@SpringBootApplication(scanBasePackages = {
    "com.youyu.gateway"
})
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

---

## 模块依赖关系

```
youyu-gateway-bootstrap
    ↓ 依赖
youyu-gateway-impl + youyu-gateway-sdk(可选)
```

---

## 为什么Gateway没有api和external?

### 无api模块
- Gateway是入口服务,不对外提供Feign接口
- 其他服务通过HTTP调用Gateway,不是Feign
- 无需定义API契约

### 无external模块
- Gateway主要做路由和鉴权
- 暂时无第三方服务依赖
- 如未来需要(如OAuth),可再添加

---

## 版本管理

### gateway-service/pom.xml
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.youyu</groupId>
            <artifactId>youyu-gateway-impl</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.youyu</groupId>
            <artifactId>youyu-gateway-sdk</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.youyu</groupId>
            <artifactId>youyu-gateway-bootstrap</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 编译与运行

### 编译所有模块
```bash
cd gateway-service
mvn clean install
```

### 启动网关
```bash
cd youyu-gateway-bootstrap
mvn spring-boot:run
```
