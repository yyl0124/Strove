package com.erokin.strove.service;

import com.erokin.strove.dto.ChatRequest;
import com.erokin.strove.dto.ChatResponse;
import com.erokin.strove.dto.InspirationRequest;
import com.erokin.strove.entity.Conversation;
import com.erokin.strove.repository.ConversationRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI服务
 */
@Service
public class AIService {

    private final ConversationRepository conversationRepository;
    private final RestTemplate restTemplate;

    public AIService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * AI对话
     */
    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        String aiReply = callAIAPI(request.getMessage(), request.getApiKey(), 
                                   request.getProvider(), request.getModel(), request.getApiUrl());

        // 保存对话记录
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setMessage(request.getMessage());
        conversation.setResponse(aiReply);
        conversation = conversationRepository.save(conversation);

        return new ChatResponse(aiReply, conversation.getId());
    }

    /**
     * 生成灵感
     */
    public List<String> generateInspiration(InspirationRequest request) {
        String prompt = "请根据关键词\"" + request.getKeyword() + 
                       "\"生成3-5条写作灵感，每条20-50字，用换行分隔。";
        
        String response = callAIAPI(prompt, request.getApiKey(), 
                                   request.getProvider(), request.getModel(), request.getApiUrl());
        
        // 解析AI返回的灵感列表
        return parseInspirationResponse(response);
    }

    /**
     * 润色文本
     */
    public String polishText(String text, String apiKey, String provider, String model, String style, String apiUrl) {
        // 使用system message强制AI遵守格式
        String systemMessage = "你是一个专业的文本润色助手。你的输出必须遵守以下规则：\n" +
                               "1. 只输出润色后的文本\n" +
                               "2. 禁止输出任何解释、说明、备注\n" +
                               "3. 禁止输出多个版本供选择\n" +
                               "4. 禁止输出\"以下是...\"、\"润色后...\"等引导语\n" +
                               "5. 直接输出结果文本，不要任何额外内容";
        
        String userMessage = "润色要求：" + (style != null ? style : "更专业") + "\n\n" +
                            "原文：\n" + text + "\n\n" +
                            "请直接返回润色后的文本，不要其他内容。";
        
        return callOpenAIWithSystem(userMessage, systemMessage, apiKey, model, apiUrl);
    }

    /**
     * 测试连接
     */
    public Map<String, Object> testConnection(String apiKey, String provider, String model, String apiUrl) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 发送一个简单的测试消息
            String testMessage = "Hi, this is a connection test.";
            String response = callAIAPI(testMessage, apiKey, provider, model, apiUrl);
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("success", true);
            result.put("message", "连接成功");
            result.put("response", response);
            result.put("responseTime", duration + "ms");
            result.put("model", model);
            result.put("provider", provider);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("success", false);
            result.put("message", "连接失败");
            result.put("error", e.getMessage());
            result.put("responseTime", duration + "ms");
        }
        
        return result;
    }


    /**
     * 调用AI API
     */
    private String callAIAPI(String message, String apiKey, String provider, String model, String apiUrl) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("API密钥不能为空");
        }

        try {
            if ("openai".equalsIgnoreCase(provider)) {
                return callOpenAI(message, apiKey, model != null ? model : "gpt-3.5-turbo", apiUrl);
            } else if ("qianwen".equalsIgnoreCase(provider)) {
                return callQianwen(message, apiKey, model != null ? model : "qwen-turbo");
            } else if ("wenxin".equalsIgnoreCase(provider)) {
                return callWenxin(message, apiKey, model != null ? model : "ernie-bot");
            } else {
                // 默认使用OpenAI
                return callOpenAI(message, apiKey, model != null ? model : "gpt-3.5-turbo", apiUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAI(String message, String apiKey, String model, String apiUrl) {
        // 构建完整的API URL
        String url;
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            // 如果用户提供了自定义URL
            url = apiUrl.trim();
            // 如果URL不包含 /chat/completions，自动添加
            if (!url.contains("/chat/completions")) {
                // 移除末尾的斜杠（如果有）
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                url = url + "/chat/completions";
            }
        } else {
            // 使用默认的OpenAI官方API地址
            url = "https://api.openai.com/v1/chat/completions";
        }
        
        System.out.println("调用OpenAI API - URL: " + url + ", Model: " + model);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
            Map.of("role", "user", "content", message)
        ));
        request.put("temperature", 0.7);
        request.put("max_tokens", 40960); // 增加到40960，避免长文本被截断

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                    return (String) messageObj.get("content");
                }
            }
            
            // 如果响应中包含错误信息
            if (response != null && response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMessage = (String) error.get("message");
                throw new RuntimeException("API错误: " + errorMessage);
            }
            
            throw new RuntimeException("API返回格式错误");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 处理4xx错误（如401未授权、400错误请求等）
            String errorBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("API Key无效或已过期，请检查您的API Key配置");
            } else if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("API请求频率超限，请稍后再试");
            } else {
                throw new RuntimeException("API请求失败(" + e.getStatusCode() + "): " + 
                    (errorBody.length() > 100 ? errorBody.substring(0, 100) + "..." : errorBody));
            }
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // 处理5xx服务器错误
            throw new RuntimeException("API服务暂时不可用(" + e.getStatusCode() + ")，请稍后重试");
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 处理网络连接问题
            throw new RuntimeException("无法连接到API服务，请检查网络或API URL配置: " + e.getMessage());
        } catch (org.springframework.web.client.RestClientException e) {
            // 处理其他RestTemplate异常，包括响应格式错误
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("text/html")) {
                throw new RuntimeException("API返回了HTML页面而非JSON数据，请检查:\n" +
                    "1. API URL配置是否正确\n" +
                    "2. API Key是否有效\n" +
                    "3. 如使用中转服务，中转服务是否正常");
            }
            throw new RuntimeException("调用API失败: " + errorMsg);
        } catch (Exception e) {
            if (e instanceof RuntimeException && e.getMessage() != null && 
                (e.getMessage().startsWith("API错误") || 
                 e.getMessage().startsWith("API Key无效") ||
                 e.getMessage().startsWith("API请求") ||
                 e.getMessage().startsWith("API服务") ||
                 e.getMessage().startsWith("无法连接") ||
                 e.getMessage().startsWith("API返回了HTML"))) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("调用OpenAI API失败: " + e.getMessage());
        }
    }

    /**
     * 调用OpenAI API（带System Message）
     */
    private String callOpenAIWithSystem(String userMessage, String systemMessage, String apiKey, String model, String apiUrl) {
        // 构建完整的API URL
        String url;
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            url = apiUrl.trim();
            if (!url.contains("/chat/completions")) {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                url = url + "/chat/completions";
            }
        } else {
            url = "https://api.openai.com/v1/chat/completions";
        }
        
        System.out.println("调用OpenAI API (with system) - URL: " + url + ", Model: " + model);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 添加system message和user message
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
            Map.of("role", "system", "content", systemMessage),
            Map.of("role", "user", "content", userMessage)
        ));
        request.put("temperature", 0.3);  // 低temperature使输出更稳定
        request.put("max_tokens", 40960);// 设置最大token数

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                    return (String) messageObj.get("content");
                }
            }
            throw new RuntimeException("API返回格式错误");
        } catch (Exception e) {
            // 复用callOpenAI的异常处理逻辑
            throw new RuntimeException("调用OpenAI API失败: " + e.getMessage());
        }
    }

    /**
     * 调用通义千问API
     */
    private String callQianwen(String message, String apiKey, String model) {
        // 简化实现：返回模拟响应
        // 实际项目中需要集成阿里云SDK
        return "（通义千问API集成待完善）这是一个示例回复：" + message;
    }

    /**
     * 调用文心一言API
     */
    private String callWenxin(String message, String apiKey, String model) {
        // 简化实现：返回模拟响应
        // 实际项目中需要集成百度AI SDK
        return "（文心一言API集成待完善）这是一个示例回复：" + message;
    }

    /**
     * 解析灵感响应
     */
    private List<String> parseInspirationResponse(String response) {
        // 按换行分割
        String[] lines = response.split("\n");
        List<String> inspirations = new ArrayList<>();

        for (String line : lines) {
            String cleaned = line.trim();
            // 移除数字编号
            cleaned = cleaned.replaceFirst("^\\d+[.、]\\s*", "");
            if (!cleaned.isEmpty() && cleaned.length() > 10) {
                inspirations.add(cleaned);
            }
        }

        return inspirations.isEmpty() ? List.of(response) : inspirations;
    }

    /**
     * 获取用户对话历史
     */
    public List<Conversation> getUserConversations(Long userId, int limit) {
        return conversationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * 删除对话记录
     */
    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话记录不存在"));
        
        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此对话记录");
        }

        conversationRepository.delete(conversation);
    }
}
