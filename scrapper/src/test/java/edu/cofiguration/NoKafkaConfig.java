package edu.cofiguration;

import edu.controller.AuthRequestsListener;
import edu.controller.GetRequestsListener;
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
        basePackages = "edu",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.configuration\\.KafkaConfig"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.controller\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.web\\..*")
        }
)
@TestConfiguration
public class NoKafkaConfig {
        @MockBean
        private AuthRequestsListener authRequestsListener;
        @MockBean
        private GetRequestsListener getRequestsListener;
        @MockBean
        private BackendProducer backendProducer;
}
