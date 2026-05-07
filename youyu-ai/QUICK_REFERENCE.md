# YouYu AI Service - 快速参考卡片

## 🚀 快速启动

```bash
# 1. 设置 API Key
export DASHSCOPE_API_KEY=your-api-key

# 2. 编译项目
mvn clean install -DskipTests

# 3. 启动服务
mvn spring-boot:run -pl youyu-ai/youyu-ai-bootstrap
```

## 📡 API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/polish` | 文本润色 |
| POST | `/ai/generate` | 文本生成 |

## 💻 代码示例

### 文本润色

```java
AiPolishRequest request = AiPolishRequest.builder()
    .originalText("这个产品很好用")
    .style("marketing")
    .targetLanguage("zh")
    .maxLength(500)
    .build();

AiPolishResponse response = aiServiceClient.polishText(request);
String polished = response.getPolishedText();
```

### 文本生成

```java
AiGenerateRequest request = AiGenerateRequest.builder()
    .prompt("请为一款智能手表写一段产品介绍")
    .topic("智能穿戴设备")
    .length("medium")
    .temperature(0.7)
    .build();

AiGenerateResponse response = aiServiceClient.generateText(request);
String generated = response.getGeneratedText();
```

## 🔧 配置切换

### 切换到 OpenAI

```yaml
ai:
  provider: openai

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
```

### 切换到通义千问

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
```

## 🎨 润色风格

| 风格 | 说明 | 适用场景 |
|------|------|----------|
| `professional` | 专业正式 | 商务文档、技术说明 |
| `casual` | 轻松随意 | 社交媒体、聊天 |
| `marketing` | 营销推广 | 商品描述、广告文案 |
| `concise` | 简洁明了 | 摘要、简报 |

## 📏 生成长度

| 长度 | 说明 | 字数范围 |
|------|------|----------|
| `short` | 简短 | 100字以内 |
| `medium` | 中等 | 100-300字 |
| `long` | 详细 | 300字以上 |

## 🔍 调试命令

```bash
# 查看服务状态
curl http://localhost:9010/actuator/health

# 测试润色接口
curl -X POST http://localhost:9010/ai/polish \
  -H "Content-Type: application/json" \
  -d '{"originalText":"测试文本","style":"marketing"}'

# 查看日志
tail -f logs/ai-service.log
```

## 📊 监控指标

- **响应时间**：< 3秒（正常）
- **成功率**：> 95%
- **缓存命中率**：> 30%
- **Token 消耗**：根据用量监控

## ⚠️ 常见问题

### API 调用失败
```bash
# 检查 API Key
echo $DASHSCOPE_API_KEY

# 检查网络连接
ping dashscope.aliyuncs.com
```

### 服务启动失败
```bash
# 检查端口占用
lsof -i :9010

# 检查 Nacos 连接
curl http://localhost:8848/nacos/
```

### 响应太慢
- 检查网络延迟
- 考虑使用更快的模型（qwen-turbo）
- 启用缓存（相同请求自动缓存）

## 🔗 相关链接

- 📖 完整文档：[README.md](README.md)
- 🏗️ 架构说明：[MODULES.md](MODULES.md)
- 🚀 启动指南：[STARTUP_GUIDE.md](STARTUP_GUIDE.md)
- 💡 集成示例：[AI_INTEGRATION_EXAMPLE.md](../youyu-product/doc/AI_INTEGRATION_EXAMPLE.md)

## 📞 技术支持

遇到问题？
1. 查看日志文件
2. 检查配置文件
3. 查阅完整文档
4. 联系开发团队

---

**提示**：将此卡片打印或保存为书签，方便日常查阅！
