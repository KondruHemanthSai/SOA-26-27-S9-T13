package com.studentcentral.ai.repository;

import com.studentcentral.ai.model.AiAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAuditLogRepository extends MongoRepository<AiAuditLog, String> {
    List<AiAuditLog> findByUserIdOrderByTimestampDesc(String userId);
}
