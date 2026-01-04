package com.erokin.strove.controller;

import com.erokin.strove.dto.ApiResponse;
import com.erokin.strove.dto.ChatRequest;
import com.erokin.strove.dto.ChatResponse;
import com.erokin.strove.dto.InspirationRequest;
import com.erokin.strove.entity.Conversation;
import com.erokin.strove.service.AIService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI对话
     */
    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request,
                                          Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            System.out.println("收到AI对话请求 - userId: " + userId + 
                             ", provider: " + request.getProvider() + 
                             ", model: " + request.getModel() + 
                             ", apiUrl: " + request.getApiUrl());
            ChatResponse response = aiService.chat(userId, request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            System.err.println("AI对话失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 测试连接
     */
    @PostMapping("/test-connection")
    public ApiResponse<Map<String, Object>> testConnection(@RequestBody Map<String, String> request) {
        try {
            String apiKey = request.get("apiKey");
            String provider = request.get("provider");
            String model = request.get("model");
            String apiUrl = request.get("apiUrl");

            Map<String, Object> result = aiService.testConnection(apiKey, provider, model, apiUrl);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 生成灵感
     */
    @PostMapping("/inspiration")
    public ApiResponse<List<String>> generateInspiration(@Valid @RequestBody InspirationRequest request) {
        try {
            List<String> inspirations = aiService.generateInspiration(request);
            return ApiResponse.success(inspirations);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 润色文本
     */
    @PostMapping("/polish")
    public ApiResponse<String> polishText(@RequestBody Map<String, String> request,
                                          Authentication authentication) {
        try {
            String text = request.get("text");
            String apiKey = request.get("apiKey");
            String provider = request.get("provider");
            String model = request.get("model");
            String style = request.get("style");
            String apiUrl = request.get("apiUrl");

            String polished = aiService.polishText(text, apiKey, provider, model, style, apiUrl);
            return ApiResponse.success(polished);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/conversations")
    public ApiResponse<List<Conversation>> getConversations(Authentication authentication,
                                                            @RequestParam(defaultValue = "20") int limit) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Conversation> conversations = aiService.getUserConversations(userId, limit);
            return ApiResponse.success(conversations);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除对话记录
     */
    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable Long id,
                                                Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            aiService.deleteConversation(userId, id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
