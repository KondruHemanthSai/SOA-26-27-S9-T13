package com.studentcentral.admission;

import com.studentcentral.admission.repository.AdmissionApplicationRepository;
import com.studentcentral.admission.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
class AdmissionServiceApplicationTests {

    @MockBean
    private AdmissionApplicationRepository applicationRepository;

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void contextLoads() {
    }
}
