# YouYu 微服务项目 - Agent 指南

## 项目概览

基于 Spring Boot 4.0.5 + Spring Cloud 2025.1.0 + Java 25 的微服务电商项目。

## 核心命令

```bash
# 全量编译（必须从根目录执行）
mvn clean install -DskipTests

# 编译单个服务（需先编译基础设施层）
mvn clean install -pl youyu-auth -am -DskipTests

# 启动单个服务的 bootstrap 模块
mvn spring-boot:run -pl youyu-auth/youyu-auth-bootstrap
```

## 启动依赖顺序

1. Nacos（配置中心 + 注册中心）
2. Redis
3. RocketMQ
4. MySQL（执行各模块 `sql/init-*.sql`）
5. youyu-gateway-bootstrap
6. youyu-auth-bootstrap
7. 其他业务服务

## 模块架构

```
基础设施层: youyu-common → youyu-framework → youyu-starter
公共服务层: youyu-basics（file + notification）
网关服务:   youyu-gateway
业务服务层: youyu-auth, youyu-user, youyu-product, youyu-order, youyu-seckill
```

每个业务服务标准结构：`*-api`（接口定义） → `*-impl`（业务实现） → `*-bootstrap`（启动入口） → `*-sdk`（客户端封装）

## 关键约束

- **Java 25**：必须使用 JDK 25，低版本无法编译
- **Lombok + MapStruct**：编译器插件已配置 `lombok-mapstruct-binding`，不要手动修改 annotationProcessorPaths 顺序
- **服务间调用**：业务服务之间通过 Feign + SDK 调用，禁止直接依赖其他服务的 impl 模块
- **API 模块纯净**：`*-api` 只能依赖 youyu-common，不能依赖 impl 或 framework
- **循环依赖**：严禁模块间循环依赖

## DDD 四层结构（impl 模块内）

`interfaces`（Controller）→ `application`（Service 编排）→ `domain`（实体/领域服务）→ `infrastructure`（Mapper/Repository/外部调用）

## MQ 可靠发送

框架层提供 `ReliableMessageProducer`（位于 youyu-framework），支持同步/异步/延时消息，内置补偿机制。业务服务禁止直接使用 RocketMQTemplate，统一通过 ReliableMessageProducer 发送。

延时级别：1=1s, 5=1m, 9=5m, 17=1h, 18=2h（共 18 级）

## 数据库管理

每个模块独立管理 SQL 脚本，位于 `模块/sql/init-*.sql`。执行顺序：auth → user → product → order → seckill。生产环境建议迁移到 Flyway/Liquibase。

## 重要文档

- `MODULE_STRUCTURE.md` - 完整模块结构说明
- `DOC_SQL_STRUCTURE.md` - SQL 文件组织说明
- `MQ_RELIABLE_SEND_UPGRADE.md` - MQ 可靠发送改造说明
- `todo.md` - 待开发功能清单

## 常见坑

- 新增服务后必须在根 pom.xml 的 `<modules>` 中注册
- bootstrap 模块打包为可执行 JAR，其他模块为普通 JAR
- Nacos 配置文件位于 `nacos-config/` 目录
- 日志输出到 `logs/` 目录
