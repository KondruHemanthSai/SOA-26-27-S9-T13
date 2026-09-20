package com.studentcentral.schedule;

import com.studentcentral.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
class ScheduleServiceApplicationTests {

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
    }
}
