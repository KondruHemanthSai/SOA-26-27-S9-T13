package com.studentcentral.course;

import com.studentcentral.course.repository.CourseRepository;
import com.studentcentral.course.repository.PrerequisiteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
class CourseServiceApplicationTests {

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private PrerequisiteRepository prerequisiteRepository;

    @Test
    void contextLoads() {
    }
}
