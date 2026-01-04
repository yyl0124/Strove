package com.erokin.strove.controller;

import com.erokin.strove.dto.ApiResponse;
import com.erokin.strove.dto.DocumentRequest;
import com.erokin.strove.entity.Document;
import com.erokin.strove.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档控制器
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 创建文档
     */
    @PostMapping
    public ApiResponse<Document> createDocument(@Valid @RequestBody DocumentRequest request,
                                                Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Document document = documentService.createDocument(userId, request);
            return ApiResponse.success("创建成功", document);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新文档
     */
    @PutMapping("/{id}")
    public ApiResponse<Document> updateDocument(@PathVariable Long id,
                                                @Valid @RequestBody DocumentRequest request,
                                                Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Document document = documentService.updateDocument(userId, id, request);
            return ApiResponse.success("更新成功", document);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户所有文档
     */
    @GetMapping
    public ApiResponse<List<Document>> getUserDocuments(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Document> documents = documentService.getUserDocuments(userId);
            return ApiResponse.success(documents);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Document> getDocument(@PathVariable Long id,
                                             Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Document document = documentService.getDocument(userId, id);
            return ApiResponse.success(document);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id,
                                            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            documentService.deleteDocument(userId, id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
