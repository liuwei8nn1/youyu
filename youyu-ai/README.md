# YouYu AI Service 启动指南

## 前置条件

1. **JDK 25** - 必须使用 JDK 25 或更高版本
2. **Maven 3.6+** - 用于构建项目
3. **Nacos** - 配置中心和注册中心（端口 8848）
4. **MySQL** - 数据库（端口 3306）
5. **Redis** - 缓存（端口 6379）

## 快速启动

### 1. 获取 API Key

#### 选项 A：阿里云通义千问（推荐）

1. 访问 [阿里云 DashScope](https://dashscope.console.aliyun.com/)
2. 注册/登录账号
3. 创建 API Key
4. 复制 API Key

#### 选项 B：OpenAI

1. 访问 [OpenAI Platform](https://platform.openai.com/)
2. 注册/登录账号
3. 创建 API Key
4. 复制 API Key

### 2. 初始化数据库

执行 SQL 脚本：

```bash
mysql -u root -p < youyu-ai/sql/init-ai.sql
```

### 3. 配置环境变量

```bash
# Linux/Mac
export DASHSCOPE_API_KEY=your-api-key-here

# Windows PowerShell
$env:DASHSCOPE_API_KEY="your-api-key-here"

# Windows CMD
set DASHSCOPE_API_KEY=your-api-key-here
```

### 4. 编译项目

在项目根目录执行：

```bash
mvn clean install -DskipTests
```

### 5. 启动服务

```bash
mvn spring-boot:run -pl youyu-ai/youyu-ai-bootstrap
```

或者打包后运行：

```bash
cd youyu-ai/youyu-ai-bootstrap/target
java -jar youyu-ai-bootstrap-0.0.1-SNAPSHOT.jar
```

### 6. 验证启动

看到以下输出表示启动成功：

```
========================================
   YouYu AI Service Started Successfully!
========================================
```

访问 http://localhost:9010/doc.html 查看 API 文档。

## 测试 API

### 使用 curl 测试

```bash
# 文本润色
curl -X POST http://localhost:9010/ai/polish \
  -H "Content-Type: application/json" \
  -d '{
    "originalText": "这个产品很好用",
    "style": "marketing",
    "targetLanguage": "zh"
  }'

# 文本生成
curl -X POST http://localhost:9010/ai/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "请为一款智能手表写一段产品介绍",
    "length": "medium"
  }'
```

### 使用 Knife4j UI

1. 打开浏览器访问：http://localhost:9010/doc.html
2. 找到 `/ai/polish` 或 `/ai/generate` 接口
3. 点击"尝试一下"
4. 填写请求参数
5. 点击"发送"

## 常见问题

### 1. 启动失败：找不到 Nacos

确保 Nacos 已启动并在 8848 端口监听：

```bash
# 检查 Nacos 状态
curl http://localhost:8848/nacos/
```

### 2. AI 调用失败：API Key 无效

检查环境变量是否正确设置：

```bash
echo $DASHSCOPE_API_KEY  # Linux/Mac
echo %DASHSCOPE_API_KEY% # Windows CMD
```

### 3. 编译失败：依赖找不到

确保已在根 pom.xml 中添加了 Spring AI 依赖管理，并执行了 `mvn clean install`。

### 4. 端口冲突

如果 9010 端口被占用，修改 `application.yml`：

```yaml
server:
  port: 9011  # 改为其他可用端口
```

## 生产环境部署

### 1. 配置 Nacos

在 Nacos 配置中心创建 `youyu-ai.yml`：

```yaml
ai:
  provider: dashscope

spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
```

### 2. 设置环境变量

```bash
export DASHSCOPE_API_KEY=your-production-api-key
export NACOS_SERVER_ADDR=your-nacos-server:8848
export SPRING_PROFILES_ACTIVE=prod
```

### 3. 启动服务

```bash
nohup java -jar youyu-ai-bootstrap-0.0.1-SNAPSHOT.jar > ai-service.log 2>&1 &
```

### 4. 监控日志

```bash
tail -f ai-service.log
```

## 性能优化建议

1. **启用缓存**：SDK 已内置本地缓存，相同请求 10 分钟内不会重复调用
2. **连接池配置**：根据并发量调整 HTTP 连接池大小
3. **超时设置**：设置合理的超时时间（建议 30 秒）
4. **限流保护**：使用 Sentinel 限制 QPS，避免超额调用

## 监控指标

建议监控以下指标：

- API 调用成功率
- 平均响应时间
- Token 消耗量
- 错误率
- 缓存命中率

## 下一步

- 阅读 [MODULES.md](MODULES.md) 了解模块架构
- 查看 [AI_INTEGRATION_EXAMPLE.md](../youyu-product/doc/AI_INTEGRATION_EXAMPLE.md) 学习如何在其他服务中集成
- 参考 [Spring AI 官方文档](https://spring.io/projects/spring-ai)
