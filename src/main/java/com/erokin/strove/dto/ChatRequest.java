package com.erokin.strove.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AI聊天请求DTO
 */
public class ChatRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String apiKey;
    private String provider; // openai, qianwen, wenxin
    private String model;
    private String apiUrl; // 自定义API URL地址

    public ChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
