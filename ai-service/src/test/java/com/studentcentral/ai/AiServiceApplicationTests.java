package com.studentcentral.ai;

import com.studentcentral.ai.repository.AiAuditLogRepository;
import com.studentcentral.ai.repository.AiConversationRepository;
import com.studentcentral.ai.repository.AiInsightRepository;
import com.studentcentral.ai.repository.AiRecommendationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
class AiServiceApplicationTests {

    @MockBean
    private AiConversationRepository conversationRepository;

    @MockBean
    private AiRecommendationRepository recommendationRepository;

    @MockBean
    private AiInsightRepository insightRepository;

    @MockBean
    private AiAuditLogRepository auditLogRepository;

    @MockBean
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
    }
}
