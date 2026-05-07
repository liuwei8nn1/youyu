# YouYu Product 集成 AI 润色功能示例

## 概述

本示例展示如何在 youyu-product 服务中集成 AI 文本润色功能，用于优化商品描述。

## 实现步骤

### 1. 添加依赖

在 `youyu-product/youyu-product-impl/pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.youyu</groupId>
    <artifactId>youyu-ai-sdk</artifactId>
</dependency>
```

### 2. 创建 AI 润色服务

在 `youyu-product-impl` 中创建新的应用服务：

```java
package com.youyu.product.application.service;

import com.youyu.ai.api.dto.AiPolishRequest;
import com.youyu.ai.api.dto.AiPolishResponse;
import com.youyu.ai.sdk.AiServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDescriptionPolishService {
    
    private final AiServiceClient aiServiceClient;
    
    /**
     * 润色商品描述
     */
    public String polishDescription(String originalDescription) {
        if (originalDescription == null || originalDescription.trim().isEmpty()) {
            return originalDescription;
        }
        
        try {
            AiPolishRequest request = AiPolishRequest.builder()
                .originalText(originalDescription)
                .style("marketing")  // 营销推广风格
                .targetLanguage("zh")
                .maxLength(500)
                .build();
            
            AiPolishResponse response = aiServiceClient.polishText(request);
            
            log.info("商品描述润色成功, tokensUsed: {}, processingTime: {}ms", 
                response.getTokensUsed(), response.getProcessingTimeMs());
            
            return response.getPolishedText();
        } catch (Exception e) {
            log.error("商品描述润色失败，返回原始描述", e);
            // 降级处理：返回原始描述
            return originalDescription;
        }
    }
}
```

### 3. 在 Controller 中添加接口

在 `ProductController` 中添加：

```java
@Autowired
private ProductDescriptionPolishService polishService;

/**
 * 润色商品描述
 */
@PostMapping("/polish-description")
public Result<String> polishDescription(@RequestParam String description) {
    try {
        String polished = polishService.polishDescription(description);
        return Result.success(polished);
    } catch (Exception e) {
        log.error("润色商品描述失败", e);
        return Result.error(e.getMessage());
    }
}
```

### 4. 前端调用示例

```javascript
// 在商品编辑页面添加"AI 润色"按钮
async function polishDescription() {
  const originalText = document.getElementById('description').value;
  
  try {
    const response = await fetch('/api/product/polish-description', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ description: originalText })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      // 显示润色后的文本供用户确认
      showPolishPreview(result.data);
    } else {
      alert('润色失败: ' + result.message);
    }
  } catch (error) {
    console.error('润色请求失败:', error);
  }
}

// 显示预览并让用户确认
function showPolishPreview(polishedText) {
  const modal = document.getElementById('polish-preview-modal');
  document.getElementById('polished-text').textContent = polishedText;
  modal.style.display = 'block';
}

// 用户确认后更新描述
function applyPolishedText() {
  const polishedText = document.getElementById('polished-text').textContent;
  document.getElementById('description').value = polishedText;
  document.getElementById('polish-preview-modal').style.display = 'none';
}
```

## 使用场景

1. **商品上架时**：商家输入简单描述，AI 自动润色为营销文案
2. **批量优化**：对已有商品描述进行批量优化
3. **多语言支持**：将中文描述翻译并润色为其他语言

## 注意事项

1. **用户体验**：AI 调用需要时间（1-3秒），建议显示加载状态
2. **降级策略**：AI 服务失败时应返回原始文本，不影响正常使用
3. **成本控制**：可以设置每日调用次数限制
4. **内容审核**：润色后仍需人工确认，避免不当内容

## 扩展功能

可以进一步扩展的功能：

1. **多种风格选择**：专业、轻松、营销、简洁等
2. **关键词强调**：让 AI 突出某些卖点
3. **长度控制**：短描述、中等、详细
4. **A/B 测试**：对比不同风格的转化率
