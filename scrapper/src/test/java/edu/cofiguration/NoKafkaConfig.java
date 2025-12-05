package edu.cofiguration;

import edu.controller.AuthRequestsListener;
import edu.controller.GetRequestsListener;
import edu.controller.PostRequestsListener;
import edu.web.BackendProducer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class
})
@ComponentScan(
        basePackages = {"edu.service", "edu.model"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.service\\..*Handler"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.service\\..*Resolver")
        }
)
@TestConfiguration
public class NoKafkaConfig {
        @MockBean
        private AuthRequestsListener authRequestsListener;
        @MockBean
        private GetRequestsListener getRequestsListener;
        @MockBean
        private PostRequestsListener postRequestsListener;
        @MockBean
        private BackendProducer backendProducer;
}
