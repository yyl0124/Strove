package com.erokin.strove.repository;

import com.erokin.strove.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Conversation> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
    List<Conversation> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}
