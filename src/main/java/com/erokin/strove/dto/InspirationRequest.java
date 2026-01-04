package com.erokin.strove.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 灵感生成请求DTO
 */
public class InspirationRequest {

    @NotBlank(message = "关键词不能为空")
    private String keyword;

    private String apiKey;
    private String provider;
    private String model;
    private String apiUrl; // 自定义API URL地址

    public InspirationRequest() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
