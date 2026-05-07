# YouYu AI Service - AI 能力微服务

## 模块概述

YouYu AI Service 是一个独立的 AI 能力微服务，提供文本润色、文本生成等 AI 功能。基于 Spring AI 框架实现，支持多种 AI 提供商（OpenAI、阿里云通义千问等）。

## 模块结构

```
youyu-ai/
├── youyu-ai-api/          # 接口定义层
│   ├── client/            # Feign Client
│   └── dto/               # 数据传输对象
├── youyu-ai-impl/         # 业务实现层（DDD）
│   ├── interfaces/        # 控制器层
│   ├── application/       # 应用服务层
│   ├── domain/            # 领域服务层
│   └── infrastructure/    # 基础设施层
├── youyu-ai-bootstrap/    # 启动模块
│   └── resources/         # 配置文件
├── youyu-ai-sdk/          # 客户端 SDK
│   └── autoconfigure/     # 自动配置
└── sql/                   # 数据库脚本
```

## 技术栈

- **Spring Boot 4.0.5**
- **Spring AI 1.0.0-M6**
- **Spring Cloud Alibaba 2025.1.0.0**
- **Java 25**
- **MyBatis Plus 3.5.15**
- **Nacos**（配置中心 + 注册中心）

## 支持的 AI 提供商

1. **阿里云通义千问（DashScope）** - 默认
   - 模型：qwen-turbo, qwen-plus, qwen-max
   - 优势：中文处理能力强，成本低

2. **OpenAI**
   - 模型：gpt-3.5-turbo, gpt-4
   - 优势：国际通用，生态完善

## 快速开始

### 1. 数据库初始化

执行 `sql/init-ai.sql` 脚本创建数据库和表。

### 2. 配置 API Key

在 Nacos 配置中心添加 `youyu-ai.yml` 配置，或设置环境变量：

```bash
# 阿里云通义千问
export DASHSCOPE_API_KEY=your-api-key-here

# 或使用 OpenAI
export OPENAI_API_KEY=your-api-key-here
```

### 3. 启动服务

```bash
# 编译整个项目
mvn clean install -DskipTests

# 启动 AI 服务
mvn spring-boot:run -pl youyu-ai/youyu-ai-bootstrap
```

### 4. 访问文档

- Knife4j UI: http://localhost:9010/doc.html
- Swagger UI: http://localhost:9010/swagger-ui/index.html

## API 接口

### 1. 文本润色

**请求：**
```http
POST /ai/polish
Content-Type: application/json

{
  "originalText": "这个产品很好用",
  "style": "marketing",
  "targetLanguage": "zh",
  "maxLength": 100
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "polishedText": "这款产品以其卓越的性能和人性化的设计，为用户带来前所未有的使用体验...",
    "modelName": "dashscope",
    "tokensUsed": 85,
    "processingTimeMs": 1250
  }
}
```

### 2. 文本生成

**请求：**
```http
POST /ai/generate
Content-Type: application/json

{
  "prompt": "请为一款智能手表写一段产品介绍",
  "topic": "智能穿戴设备",
  "length": "medium",
  "temperature": 0.7
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "generatedText": "全新智能手表，融合科技与时尚...",
    "modelName": "dashscope",
    "tokensUsed": 120,
    "processingTimeMs": 1800
  }
}
```

## 在其他服务中使用

### 1. 添加依赖

在目标服务的 pom.xml 中添加：

```xml
<dependency>
    <groupId>com.youyu</groupId>
    <artifactId>youyu-ai-sdk</artifactId>
</dependency>
```

### 2. 注入客户端

```java
@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    
    private final AiServiceClient aiServiceClient;
    
    public String polishProductDescription(String description) {
        AiPolishRequest request = AiPolishRequest.builder()
            .originalText(description)
            .style("marketing")
            .build();
        
        AiPolishResponse response = aiServiceClient.polishText(request);
        return response.getPolishedText();
    }
}
```

## 配置说明

### 切换 AI 提供商

在 `application.yml` 或 Nacos 配置中修改：

```yaml
ai:
  provider: openai  # 或 dashscope

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
```

### 性能优化

SDK 内置了本地缓存机制，相同的润色请求会在 10 分钟内返回缓存结果，避免重复调用 AI 服务。

## 监控与统计

数据库提供了两张表用于监控：

1. **ai_call_record** - 记录每次 AI 调用的详细信息
2. **ai_usage_stats** - 按用户和日期统计使用情况

## 注意事项

1. **API Key 安全**：不要将 API Key 硬编码在代码中，使用环境变量或配置中心管理
2. **成本控制**：建议设置调用频率限制和每日预算上限
3. **错误处理**：AI 服务可能因网络或配额问题失败，需要做好降级处理
4. **内容审核**：对于用户生成的内容，建议增加敏感词过滤

## 未来规划

- [ ] 支持图片生成（Stable Diffusion、DALL-E）
- [ ] 支持语音识别和合成
- [ ] 增加 AI 调用限流和熔断
- [ ] 实现更智能的缓存策略
- [ ] 添加 A/B 测试功能（对比不同模型效果）

## 相关文档

- [Spring AI 官方文档](https://spring.io/projects/spring-ai)
- [阿里云通义千问文档](https://help.aliyun.com/zh/dashscope/)
- [OpenAI API 文档](https://platform.openai.com/docs)
