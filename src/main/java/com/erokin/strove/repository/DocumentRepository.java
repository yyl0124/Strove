package com.erokin.strove.repository;

import com.erokin.strove.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<Document> findByUserIdAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(Long userId, String title);
}
