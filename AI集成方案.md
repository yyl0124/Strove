# Strove AI 集成方案 - Spring AI 实现

## 方案概述
采用 **Spring AI** 框架统一接入符合 OpenAI API 规范的所有AI服务，包括：
- OpenAI (GPT-3.5/GPT-4)
- 阿里云通义千问（Qwen）
- 百度文心一言
- Deepseek
- 月之暗面Kimi
- 智谱清言
- 其他兼容OpenAI协议的服务

## 为什么选择 Spring AI？

### ✅ 优势
1. **官方支持**：Spring生态官方项目，长期维护
2. **统一接口**：一套API对接多个AI服务商
3. **简单易用**：零配置即可使用，开箱即用
4. **类型安全**：完整的TypeScript风格的Java API
5. **流式支持**：支持SSE流式响应
6. **自动重试**：内置重试和错误处理机制

### 对比其他方案
| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **Spring AI** | 官方支持、简单、统一 | 相对较新 | ⭐⭐⭐⭐⭐ |
| LangChain4j | 功能丰富、社区活跃 | 配置复杂 | ⭐⭐⭐⭐ |
| 原生实现 | 灵活可控 | 需要自己处理各种细节 | ⭐⭐⭐ |

---

## Maven依赖配置

在 `pom.xml` 中添加Spring AI依赖：

```xml
<!-- Spring AI BOM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0-M4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Spring AI OpenAI -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

---

## 配置文件

### application.yml
```yaml
spring:
  ai:
    openai:
      # 默认配置（可被用户自定义覆盖）
      api-key: ${OPENAI_API_KEY:sk-default-key}
      base-url: ${OPENAI_API_BASE:https://api.openai.com}
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.7
          max-tokens: 2000

# 自定义配置：支持多服务商
strove:
  ai:
    # 是否允许用户自定义密钥（默认true）
    allow-custom-key: true
    # 默认超时时间（秒）
    timeout-seconds: 30
    # 支持的服务商配置
    providers:
      openai:
        name: OpenAI
        base-url: https://api.openai.com
      qianwen:
        name: 通义千问
        base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      deepseek:
        name: Deepseek
        base-url: https://api.deepseek.com
      kimi:
        name: 月之暗面Kimi
        base-url: https://api.moonshot.cn/v1
```

---

## Java代码实现

### 1. AI配置类

```java
package com.erokin.strove.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    
    /**
     * 创建动态的AI客户端
     * 根据用户提供的API密钥和base-url动态创建
     */
    public OpenAiChatModel createChatModel(String apiKey, String baseUrl) {
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo")
                .withTemperature(0.7)
                .withMaxTokens(2000)
                .build();
        
        return new OpenAiChatModel(openAiApi, options);
    }
}
```

### 2. AI服务接口

```java
package com.erokin.strove.service;

public interface AIService {
    /**
     * 发送消息到AI并获取回复
     * @param message 用户消息
     * @param apiKey 用户的API密钥
     * @param baseUrl AI服务的base URL
     * @param model 模型名称（可选）
     * @return AI回复
     */
    String chat(String message, String apiKey, String baseUrl, String model);
    
    /**
     * 生成写作灵感
     * @param keyword 关键词
     * @param apiKey 用户的API密钥
     * @param baseUrl AI服务的base URL
     * @return 灵感列表（3-5条）
     */
    List<String> generateInspiration(String keyword, String apiKey, String baseUrl);
}
```

### 3. AI服务实现

```java
package com.erokin.strove.service.impl;

import com.erokin.strove.config.AIConfig;
import com.erokin.strove.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final AIConfig aiConfig;
    
    @Override
    public String chat(String message, String apiKey, String baseUrl, String model) {
        // 创建临时的ChatModel实例
        OpenAiChatModel chatModel = aiConfig.createChatModel(apiKey, baseUrl);
        
        // 发送消息并获取响应
        Prompt prompt = new Prompt(new UserMessage(message));
        ChatResponse response = chatModel.call(prompt);
        
        return response.getResult().getOutput().getContent();
    }
    
    @Override
    public List<String> generateInspiration(String keyword, String apiKey, String baseUrl) {
        OpenAiChatModel chatModel = aiConfig.createChatModel(apiKey, baseUrl);
        
        String promptText = String.format(
            "请为关键词"%s"生成3-5条写作灵感，每条20-50字，用换行分隔。" +
            "只输出灵感内容，不要其他说明。", 
            keyword
        );
        
        Prompt prompt = new Prompt(new UserMessage(promptText));
        ChatResponse response = chatModel.call(prompt);
        String content = response.getResult().getOutput().getContent();
        
        // 解析返回的灵感（按行分割）
        return Arrays.stream(content.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(5)
                .collect(Collectors.toList());
    }
}
```

### 4. DTO定义

```java
package com.erokin.strove.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;
    
    @NotBlank(message = "服务地址不能为空")
    private String baseUrl;
    
    private String model = "gpt-3.5-turbo"; // 默认模型
}

@Data
public class ChatResponse {
    private String reply;
    private String model;
    private Integer tokensUsed; // 可选
}

@Data
public class InspirationRequest {
    @NotBlank(message = "关键词不能为空")
    private String keyword;
    
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;
    
    @NotBlank(message = "服务地址不能为空")
    private String baseUrl;
}

@Data
public class InspirationResponse {
    private List<String> ideas;
    private String keyword;
}
```

### 5. AI控制器

```java
package com.erokin.strove.controller;

import com.erokin.strove.dto.*;
import com.erokin.strove.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    /**
     * AI对话接口
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        try {
            String reply = aiService.chat(
                request.getMessage(),
                request.getApiKey(),
                request.getBaseUrl(),
                request.getModel()
            );
            
            ChatResponse response = new ChatResponse();
            response.setReply(reply);
            response.setModel(request.getModel());
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("AI请求失败：" + e.getMessage()));
        }
    }
    
    /**
     * 生成写作灵感
     */
    @PostMapping("/inspiration")
    public ResponseEntity<ApiResponse<InspirationResponse>> generateInspiration(
            @Valid @RequestBody InspirationRequest request) {
        try {
            List<String> ideas = aiService.generateInspiration(
                request.getKeyword(),
                request.getApiKey(),
                request.getBaseUrl()
            );
            
            InspirationResponse response = new InspirationResponse();
            response.setIdeas(ideas);
            response.setKeyword(request.getKeyword());
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("灵感生成失败：" + e.getMessage()));
        }
    }
}
```

---

## 前端对接示例

### API调用（api/ai.ts）

```typescript
import apiClient from './client'

export interface ChatRequest {
  message: string
  apiKey: string
  baseUrl: string
  model?: string
}

export interface InspirationRequest {
  keyword: string
  apiKey: string
  baseUrl: string
}

export const aiApi = {
  // AI对话
  chat: (data: ChatRequest) => {
    return apiClient.post<{
      success: boolean
      data: {
        reply: string
        model: string
      }
    }>('/api/ai/chat', data)
  },
  
  // 生成灵感
  generateInspiration: (data: InspirationRequest) => {
    return apiClient.post<{
      success: boolean
      data: {
        ideas: string[]
        keyword: string
      }
    }>('/api/ai/inspiration', data)
  }
}
```

### 使用示例（Chat组件）

```typescript
import { aiApi } from '@/api/ai'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const sendMessage = async () => {
  try {
    const response = await aiApi.chat({
      message: inputMessage.value,
      apiKey: settingsStore.apiKey,
      baseUrl: settingsStore.baseUrl,
      model: 'gpt-3.5-turbo'
    })
    
    if (response.data.success) {
      // 处理AI回复
      const reply = response.data.data.reply
      messages.value.push({
        role: 'assistant',
        content: reply
      })
    }
  } catch (error) {
    ElMessage.error('AI请求失败')
  }
}
```

---

## 支持的AI服务商配置

### 1. OpenAI
```
Base URL: https://api.openai.com
API Key: sk-xxx
Model: gpt-3.5-turbo / gpt-4
```

### 2. 阿里云通义千问
```
Base URL: https://dashscope.aliyuncs.com/compatible-mode/v1
API Key: sk-xxx (Dashscope API Key)
Model: qwen-turbo / qwen-plus
```

### 3. Deepseek
```
Base URL: https://api.deepseek.com
API Key: sk-xxx
Model: deepseek-chat
```

### 4. 月之暗面Kimi
```
Base URL: https://api.moonshot.cn/v1
API Key: sk-xxx
Model: moonshot-v1-8k / moonshot-v1-32k
```

### 5. 智谱清言
```
Base URL: https://open.bigmodel.cn/api/paas/v4
API Key: xxx
Model: glm-4 / glm-3-turbo
```

---

## 错误处理

### 常见错误及处理

```java
try {
    String reply = chatModel.call(prompt);
} catch (org.springframework.ai.retry.NonRetryableException e) {
    // API密钥无效
    throw new BadRequestException("API密钥无效，请检查配置");
} catch (org.springframework.ai.openai.api.OpenAiApiException e) {
    // OpenAI API错误
    throw new BadRequestException("AI服务请求失败：" + e.getMessage());
} catch (Exception e) {
    // 其他错误
    throw new InternalServerException("系统错误：" + e.getMessage());
}
```

---

## 性能优化建议

1. **连接池复用**：为同一用户缓存ChatModel实例
2. **超时设置**：设置合理的超时时间（30秒）
3. **异步处理**：对于长时间请求使用异步
4. **流式响应**：支持SSE流式输出，提升用户体验

---

## 安全考虑

1. **密钥不存储**：后端不持久化存储用户API密钥
2. **HTTPS传输**：所有请求必须使用HTTPS
3. **请求限流**：防止滥用（可选）
4. **日志脱敏**：日志中不记录完整API密钥

---

## 总结

使用Spring AI可以用最少的代码实现AI功能集成，推荐方案：
- ✅ **简单**：代码量少，易于维护
- ✅ **统一**：一套接口支持所有OpenAI协议服务商
- ✅ **安全**：官方支持，安全性有保障
- ✅ **扩展**：后续可轻松添加新的AI服务商

---

**开发优先级**：
1. 先实现OpenAI接口
2. 测试通过后添加其他服务商配置
3. 优化错误处理和用户体验
