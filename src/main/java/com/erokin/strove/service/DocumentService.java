package com.erokin.strove.service;

import com.erokin.strove.dto.DocumentRequest;
import com.erokin.strove.entity.Document;
import com.erokin.strove.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档服务
 */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * 创建文档
     */
    @Transactional
    public Document createDocument(Long userId, DocumentRequest request) {
        Document document = new Document();
        document.setUserId(userId);
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setWordCount(calculateWordCount(request.getContent()));

        return documentRepository.save(document);
    }

    /**
     * 更新文档
     */
    @Transactional
    public Document updateDocument(Long userId, Long documentId, DocumentRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此文档");
        }

        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setWordCount(calculateWordCount(request.getContent()));

        return documentRepository.save(document);
    }

    /**
     * 获取用户所有文档
     */
    public List<Document> getUserDocuments(Long userId) {
        return documentRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    /**
     * 获取文档详情
     */
    public Document getDocument(Long userId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此文档");
        }

        return document;
    }

    /**
     * 删除文档
     */
    @Transactional
    public void deleteDocument(Long userId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此文档");
        }

        documentRepository.delete(document);
    }

    /**
     * 计算字数
     */
    private int calculateWordCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // 简单字数统计，去除空白字符
        return content.replaceAll("\\s+", "").length();
    }
}
