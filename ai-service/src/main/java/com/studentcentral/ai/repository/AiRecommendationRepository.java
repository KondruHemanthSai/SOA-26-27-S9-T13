package com.studentcentral.ai.repository;

import com.studentcentral.ai.model.AiRecommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiRecommendationRepository extends MongoRepository<AiRecommendation, String> {
    Optional<AiRecommendation> findFirstByUserIdOrderByGeneratedAtDesc(String userId);
    Optional<AiRecommendation> findByRecommendationId(String recommendationId);
}
