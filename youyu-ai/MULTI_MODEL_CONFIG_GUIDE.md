# YouYu AI 多模型配置指南

## 📋 概述

本文档说明如何在 YouYu AI 服务中配置和使用多个 AI 模型提供商。

**当前状态**: ✅ **多模型支持已实现**

---

## 🚀 快速开始（二选一）

### 方案 1：单模型配置（简单实用）⭐ 推荐新手

如果你只需要使用一个 AI 提供商，这是最简单的方式。

#### 步骤 1：配置文件

在 `application.yml` 中添加：

```yaml
ai:
  providers:
    # 只配置一个提供商（如硅基流动）
    siliconflow:
      api-key: ${SILICONFLOW_API_KEY}
      base-url: https://api.siliconflow.cn/v1
      model: Qwen/Qwen2.5-72B-Instruct
      type: openai
```

#### 步骤 2：直接使用

代码会自动使用配置的提供商，无需任何修改：

```java
@Service
@RequiredArgsConstructor
public class ProductDescriptionService {
    
    private final AiTextService aiTextService;
    
    public String polishDescription(String description) {
        // 自动使用配置的 AI 提供商
        AiPolishRequest request = new AiPolishRequest();
        request.setOriginalText(description);
        
        AiPolishResponse response = aiTextService.polishText(request);
        return response.getPolishedText();
    }
}
```

**优点**：
- ✅ 配置简单，只需几行 YAML
- ✅ 代码无需关心模型选择
- ✅ 适合大多数场景

**缺点**：
- ❌ 无法动态切换模型
- ❌ 无法根据场景选择不同模型

---

### 方案 2：多模型配置（灵活强大）⭐ 推荐生产环境

如果你需要根据不同场景使用不同模型，或者想要负载均衡、故障降级等功能。

#### 步骤 1：配置多个提供商

在 `application.yml` 中添加：

```yaml
ai:
  providers:
    # 硅基流动 - 用于日常对话（成本低）
    siliconflow:
      api-key: ${SILICONFLOW_API_KEY}
      base-url: https://api.siliconflow.cn/v1
      model: Qwen/Qwen2.5-72B-Instruct
      type: openai
    
    # 智谱 GLM-4 - 用于复杂推理（能力强）
    zhipu:
      api-key: ${ZHIPU_API_KEY}
      base-url: https://open.bigmodel.cn/api/paas/v4
      model: glm-4-plus
      type: openai
    
    # DeepSeek - 用于代码生成
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com/v1
      model: deepseek-chat
      type: openai
    
    # 通义千问 - 企业级应用（中文优化好）
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      model: qwen-max  # 可选: qwen-max, qwen-plus, qwen-turbo, qwen-long
      type: dashscope
```

#### 步骤 2：业务层动态选择模型

```java
@Service
@RequiredArgsConstructor
public class SmartAiService {
    
    private final AiTextServiceImpl aiTextService;
    
    /**
     * 场景 1：商品描述润色（使用低成本的硅基流动）
     */
    public String polishProductDescription(String description) {
        ChatClient client = aiTextService.getChatClient("siliconflow");
        
        return client.prompt()
            .user("润色商品描述：" + description)
            .call()
            .content();
    }
    
    /**
     * 场景 2：复杂数据分析（使用强推理的智谱 GLM-4）
     */
    public String analyzeData(String data) {
        ChatClient client = aiTextService.getChatClient("zhipu");
        
        return client.prompt()
            .user("分析以下数据：" + data)
            .call()
            .content();
    }
    
    /**
     * 场景 3：代码生成（使用 DeepSeek）
     */
    public String generateCode(String requirement) {
        ChatClient client = aiTextService.getChatClient("deepseek");
        
        return client.prompt()
            .user("生成代码：" + requirement)
            .call()
            .content();
    }
    
    /**
     * 场景 4：企业级客服（使用通义千问）
     */
    public String customerService(String question) {
        ChatClient client = aiTextService.getChatClient("dashscope");
        
        return client.prompt()
            .user("回答客户问题：" + question)
            .call()
            .content();
    }
}
```

**优点**：
- ✅ 灵活选择模型
- ✅ 可以根据场景优化成本和性能
- ✅ 支持故障降级（一个模型失败时切换到另一个）

**缺点**：
- ❌ 配置稍复杂
- ❌ 代码需要管理模型选择逻辑

## 🔧 技术实现细节

### 为什么需要注入 ToolCallingManager、RetryTemplate 等组件？

Spring AI 的自动配置会创建以下组件：

1. **ToolCallingManager** - 管理函数调用（Tool Calling）
2. **RetryTemplate** - 处理网络请求重试
3. **ObservationRegistry** - 监控和追踪（Micrometer）

如果我们手动创建 `ChatClient` 时不注入这些组件，会导致：
- ❌ Tool Calling 无法正常工作
- ❌ 网络失败时不会自动重试
- ❌ 无法监控 AI 调用情况

**解决方案**：通过构造函数注入 Spring AI 自动配置的这些 Bean：

```java
@Configuration
@RequiredArgsConstructor
public class AiClientConfig {
    
    // 注入 Spring AI 自动配置的组件
    private final ToolCallingManager toolCallingManager;
    private final RetryTemplate retryTemplate;
    private final ObservationRegistry observationRegistry;
    
    private ChatClient createClient(...) {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(options)
            .toolCallingManager(toolCallingManager)  // ✅ 支持 Tool Calling
            .retryTemplate(retryTemplate)            // ✅ 支持自动重试
            .observationRegistry(observationRegistry)// ✅ 支持监控
            .build();
        
        return ChatClient.create(chatModel);
    }
}
```

这样既保留了手动配置的灵活性（多模型支持），又拥有了自动配置的全部功能！

---

1. **研究 Spring AI 1.1.5 的最新 API**
   - 查看官方文档：https://docs.spring.io/spring-ai/reference/
   - 确认 `OpenAiChatModel` 的正确构造方式

2. **完善多模型配置**
   - 实现 `AiClientConfig` 中的多客户端创建逻辑
   - 添加模型路由和负载均衡功能

3. **添加模型切换功能**
   - 支持运行时动态切换模型
   - 实现基于场景的自动路由

## 📚 支持的 AI 提供商

### OpenAI 兼容接口（type: openai）

这些提供商使用相同的 API 格式，只需修改 `base-url` 和 `api-key`：

| 提供商 | Base URL | 推荐模型 | 特点 |
|--------|----------|----------|------|
| **硅基流动** | https://api.siliconflow.cn/v1 | Qwen/Qwen2.5-72B-Instruct | 成本低、速度快 |
| **智谱 GLM** | https://open.bigmodel.cn/api/paas/v4 | glm-4-plus | 推理能力强 |
| **DeepSeek** | https://api.deepseek.com/v1 | deepseek-chat | 代码生成优秀 |
| **Moonshot (Kimi)** | https://api.moonshot.cn/v1 | moonshot-v1-8k | 长文本处理 |

### 阿里云 DashScope（type: dashscope）

| 模型 | 特点 | 适用场景 |
|------|------|----------|
| **qwen-max** | 最强能力 | 复杂任务、高质量输出 |
| **qwen-plus** | 平衡性能和成本 | 日常应用 |
| **qwen-turbo** | 快速响应 | 实时对话 |
| **qwen-long** | 长文本处理 | 文档分析、摘要生成 |

---

## 📚 参考资料

- Spring AI 官方文档：https://docs.spring.io/spring-ai/reference/
- OpenAI Chat 配置：https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html
- Spring AI Alibaba：https://github.com/alibaba/spring-ai-alibaba
- Spring AI GitHub：https://github.com/spring-projects/spring-ai

## 💡 总结

✅ **单模型配置**：简单实用，适合大多数场景  
✅ **多模型配置**：灵活强大，支持动态切换  
✅ **完整功能**：支持 Tool Calling、Retry、Observation  
✅ **广泛支持**：5+ AI 提供商，覆盖各种场景

---

**最后更新**: 2026-05-07  
**作者**: YouYu Team
