package com.erokin.strove.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文档请求DTO
 */
public class DocumentRequest {

    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题不能超过200字符")
    private String title;

    private String content;

    public DocumentRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
