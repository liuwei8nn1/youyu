# YouYu AI Service 启动指南

## 前置条件检查清单

在启动之前，请确保以下组件已安装并运行：

- [ ] **JDK 25** 或更高版本
- [ ] **Maven 3.6+**
- [ ] **Nacos** （端口 8848）
- [ ] **MySQL** （端口 3306）
- [ ] **Redis** （端口 6379）
- [ ] **API Key** （通义千问或 OpenAI）

## 步骤 1：获取 API Key

### 选项 A：阿里云通义千问（推荐）

1. 访问 https://dashscope.console.aliyun.com/
2. 注册/登录阿里云账号
3. 进入"API-KEY管理"
4. 创建新的 API Key
5. 复制并保存 API Key

**免费额度**：新用户有一定的免费调用额度

### 选项 B：OpenAI

1. 访问 https://platform.openai.com/
2. 注册/登录 OpenAI 账号
3. 进入 "API Keys" 页面
4. 创建新的 Secret Key
5. 复制并保存 API Key

**注意**：OpenAI 需要绑定支付方式

## 步骤 2：初始化数据库

执行 SQL 脚本创建数据库和表：

```bash
# 方法 1：命令行
mysql -u root -p < youyu-ai/sql/init-ai.sql

# 方法 2：MySQL 客户端
source youyu-ai/sql/init-ai.sql
```

验证数据库是否创建成功：

```sql
SHOW DATABASES LIKE 'youyu_ai';
USE youyu_ai;
SHOW TABLES;
```

应该看到两张表：
- `ai_call_record`
- `ai_usage_stats`

## 步骤 3：配置环境变量

### Linux / macOS

```bash
export DASHSCOPE_API_KEY=sk-your-api-key-here
export NACOS_SERVER_ADDR=127.0.0.1:8848
export SPRING_PROFILES_ACTIVE=dev
```

### Windows PowerShell

```powershell
$env:DASHSCOPE_API_KEY="sk-your-api-key-here"
$env:NACOS_SERVER_ADDR="127.0.0.1:8848"
$env:SPRING_PROFILES_ACTIVE="dev"
```

### Windows CMD

```cmd
set DASHSCOPE_API_KEY=sk-your-api-key-here
set NACOS_SERVER_ADDR=127.0.0.1:8848
set SPRING_PROFILES_ACTIVE=dev
```

**验证配置**：

```bash
echo $DASHSCOPE_API_KEY  # Linux/Mac
echo %DASHSCOPE_API_KEY% # Windows CMD
```

## 步骤 4：编译项目

在项目根目录（`youyu/`）执行：

```bash
mvn clean install -DskipTests
```

预计耗时：2-5 分钟（首次编译需要下载依赖）

**成功标志**：

```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

## 步骤 5：启动服务

### 方法 1：Maven 直接运行（开发环境推荐）

```bash
mvn spring-boot:run -pl youyu-ai/youyu-ai-bootstrap
```

### 方法 2：打包后运行

```bash
# 1. 打包
cd youyu-ai/youyu-ai-bootstrap
mvn clean package -DskipTests

# 2. 运行
java -jar target/youyu-ai-bootstrap-0.0.1-SNAPSHOT.jar
```

### 方法 3：后台运行（生产环境）

```bash
nohup java -jar youyu-ai-bootstrap-0.0.1-SNAPSHOT.jar > ai-service.log 2>&1 &
```

## 步骤 6：验证启动

### 1. 查看启动日志

成功的启动日志应包含：

```
========================================
   YouYu AI Service Started Successfully!
========================================
```

### 2. 检查健康状态

```bash
curl http://localhost:9010/actuator/health
```

预期响应：

```json
{
  "status": "UP"
}
```

### 3. 访问 API 文档

打开浏览器访问：

- **Knife4j UI**: http://localhost:9010/doc.html
- **Swagger UI**: http://localhost:9010/swagger-ui/index.html

### 4. 测试 API

使用 curl 测试文本润色：

```bash
curl -X POST http://localhost:9010/ai/polish \
  -H "Content-Type: application/json" \
  -d '{
    "originalText": "这个产品很好用",
    "style": "marketing",
    "targetLanguage": "zh"
  }'
```

预期响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "polishedText": "这款产品以其卓越的性能...",
    "modelName": "dashscope",
    "tokensUsed": 85,
    "processingTimeMs": 1250
  }
}
```

## 常见问题排查

### ❌ 问题 1：启动失败 - 找不到 Nacos

**错误信息**：
```
Connect to localhost:8848 failed
```

**解决方案**：

1. 检查 Nacos 是否启动：
   ```bash
   curl http://localhost:8848/nacos/
   ```

2. 如果未启动，启动 Nacos：
   ```bash
   cd nacos/bin
   sh startup.sh -m standalone  # Linux/Mac
   startup.cmd -m standalone    # Windows
   ```

3. 确认 Nacos 地址配置正确：
   ```yaml
   spring:
     cloud:
       nacos:
         server-addr: 127.0.0.1:8848
   ```

### ❌ 问题 2：AI 调用失败 - API Key 无效

**错误信息**：
```
Invalid API key
```

**解决方案**：

1. 检查环境变量是否设置：
   ```bash
   echo $DASHSCOPE_API_KEY
   ```

2. 确认 API Key 格式正确（不应有空格或换行）

3. 测试 API Key 是否有效：
   ```bash
   curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation \
     -H "Authorization: Bearer $DASHSCOPE_API_KEY" \
     -H "Content-Type: application/json" \
     -d '{"model":"qwen-turbo","input":{"messages":[{"role":"user","content":"Hi"}]}}'
   ```

### ❌ 问题 3：端口被占用

**错误信息**：
```
Port 9010 was already in use
```

**解决方案**：

1. 查找占用端口的进程：
   ```bash
   lsof -i :9010  # Linux/Mac
   netstat -ano | findstr :9010  # Windows
   ```

2. 杀死进程或修改端口：
   ```yaml
   server:
     port: 9011  # 改为其他可用端口
   ```

### ❌ 问题 4：编译失败 - 依赖找不到

**错误信息**：
```
Could not resolve dependency
```

**解决方案**：

1. 清理 Maven 缓存：
   ```bash
   mvn clean
   rm -rf ~/.m2/repository/com/youyu
   ```

2. 重新编译：
   ```bash
   mvn clean install -DskipTests -U
   ```

3. 检查网络连接（需要下载 Spring AI 依赖）

### ❌ 问题 5：数据库连接失败

**错误信息**：
```
Cannot create PoolableConnectionFactory
```

**解决方案**：

1. 检查 MySQL 是否启动：
   ```bash
   mysql -u root -p -e "SELECT 1"
   ```

2. 确认数据库已创建：
   ```sql
   SHOW DATABASES LIKE 'youyu_ai';
   ```

3. 检查数据库配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/youyu_ai
       username: root
       password: your-password
   ```

## 性能优化建议

### 1. JVM 参数调优

```bash
java -Xms512m -Xmx2g \
     -XX:+UseZGC \
     -jar youyu-ai-bootstrap-0.0.1-SNAPSHOT.jar
```

### 2. 启用虚拟线程（JDK 21+）

已在 `application.yml` 中启用：

```yaml
server:
  tomcat:
    threads:
      virtual: true
```

### 3. 连接池配置

根据并发量调整数据库连接池：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## 监控和日志

### 查看实时日志

```bash
tail -f logs/ai-service.log
```

### 关键日志级别

- **DEBUG**：详细的 AI 调用信息
- **INFO**：正常的业务日志
- **WARN**：警告信息
- **ERROR**：错误信息

### 修改日志级别

在 `application-dev.yml` 中：

```yaml
logging:
  level:
    com.youyu.ai: DEBUG
    org.springframework.ai: DEBUG
```

## 生产环境部署

### 1. 配置文件准备

在 Nacos 中创建生产配置：

- Data ID: `youyu-ai.yml`
- Group: `DEFAULT_GROUP`
- 内容参考 `nacos-config/youyu-ai.yml`

### 2. 环境变量设置

```bash
export DASHSCOPE_API_KEY=prod-api-key
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseZGC"
```

### 3. 启动命令

```bash
nohup java $JAVA_OPTS \
  -jar youyu-ai-bootstrap-0.0.1-SNAPSHOT.jar \
  > /var/log/youyu-ai/ai-service.log 2>&1 &
```

### 4. 健康检查

配置负载均衡器的健康检查：

```
GET http://localhost:9010/actuator/health
Interval: 30s
Timeout: 5s
```

## 下一步

✅ 服务已成功启动！

现在你可以：

1. 📖 阅读 [README.md](README.md) 了解完整功能
2. 💻 使用 [QUICK_REFERENCE.md](QUICK_REFERENCE.md) 快速开发
3. 🔗 在其他服务中集成 AI SDK
4. 📊 监控 API 调用情况和 Token 消耗

遇到问题？查看 [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) 中的常见问题部分。

祝使用愉快！🎉
