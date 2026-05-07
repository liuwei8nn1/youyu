# Nacos 配置中心备份

本目录包含 Nacos 配置中心的配置文件本地备份，用于参考和版本管理。

## 📁 目录结构

```
nacos-config/
├── common-db.yaml              # 公共数据库配置
├── common-redis.yaml           # 公共 Redis 配置
├── common-rocketmq.yaml        # 公共 RocketMQ 配置
├── gateway-service.yaml        # Gateway 服务专属配置
├── auth-service.yaml           # Auth 服务专属配置
├── product-service.yaml        # Product 服务专属配置
├── seckill-service.yaml        # Seckill 服务专属配置
├── user-service.yaml           # User 服务专属配置
└── order-service.yaml          # Order 服务专属配置
```

## 🔧 Nacos 配置信息

- **Server Address**: `127.0.0.1:8848`（可通过环境变量 `NACOS_SERVER_ADDR` 覆盖）
- **Namespace**: `demo-cloud`（可通过环境变量 `NACOS_NAMESPACE` 覆盖）
- **Group**: `DEFAULT_GROUP`（可通过环境变量 `NACOS_GROUP` 覆盖）
- **File Extension**: `yaml`

## 📋 配置优先级

配置加载优先级（从高到低）：

1. `${spring.application.name}.yaml` - 应用专属配置
2. `common-rocketmq.yaml` - 公共 RocketMQ 配置
3. `common-redis.yaml` - 公共 Redis 配置
4. `common-db.yaml` - 公共数据库配置
5. `application-prod.yml` - 生产环境本地配置
6. `application.yml` - 基础配置

## 🚀 使用方式

### 在 application.yml 中导入

```yaml
spring:
  config:
    import:
      - "nacos:common-db.yaml"
      - "nacos:common-redis.yaml"
      - "nacos:common-rocketmq.yaml"
      - "nacos:${spring.application.name}.yaml"
```

### 通过环境变量覆盖

```bash
# 启动时指定 Nacos 地址
NACOS_SERVER_ADDR=192.168.1.100:8848 \
NACOS_NAMESPACE=prod-namespace \
java -jar app.jar
```

## 📝 配置说明

### 公共配置

- **common-db.yaml**: 所有微服务共享的数据库配置
- **common-redis.yaml**: 所有微服务共享的 Redis 配置
- **common-rocketmq.yaml**: 所有微服务共享的 RocketMQ 基础配置

### 应用专属配置

每个微服务都有独立的配置文件，包含：
- MyBatis-Plus 配置
- 业务特定配置（如 JWT、RocketMQ Group 等）
- 日志配置

## ⚠️ 注意事项

1. **敏感信息**: 生产环境的密码等敏感信息应通过环境变量或 Nacos 加密功能管理
2. **配置同步**: 修改 Nacos 配置后，此目录的备份文件应手动同步更新
3. **版本管理**: 建议将重要配置变更提交到版本控制系统

## 🔄 同步 Nacos 配置

当 Nacos 配置中心的配置发生变更时，请手动更新此目录的对应文件，保持同步。
