package com.studentcentral.ai.repository;

import com.studentcentral.ai.model.AiConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiConversationRepository extends MongoRepository<AiConversation, String> {
    Optional<AiConversation> findByConversationId(String conversationId);
    Optional<AiConversation> findFirstByUserIdOrderByUpdatedAtDesc(String userId);
    List<AiConversation> findByUserIdOrderByUpdatedAtDesc(String userId);
}
