package edu.configuration;

import edu.controller.AuthorizationListener;
import edu.controller.GetResponsesListener;
import edu.web.ScrapperProducer;
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
        basePackages = {"edu"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.controller\\..*Listener"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.web\\..*Producer"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.configuration\\.KafkaConfig")
        }
)
@TestConfiguration
public class NoKafkaConfig {
        @MockBean
        private ScrapperProducer scrapperProducer;
        @MockBean
        private AuthorizationListener authorizationListener;
        @MockBean
        private GetResponsesListener getResponsesListener;
}
