package com.studentcentral.ai.repository;

import com.studentcentral.ai.model.AiInsight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiInsightRepository extends MongoRepository<AiInsight, String> {
    Optional<AiInsight> findFirstByUserIdOrderByEvaluatedAtDesc(String userId);
}
