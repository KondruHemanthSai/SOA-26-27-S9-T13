package com.studentcentral.notification;

import com.studentcentral.notification.repository.NotificationRepository;
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
class NotificationServiceApplicationTests {

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
    }
}
