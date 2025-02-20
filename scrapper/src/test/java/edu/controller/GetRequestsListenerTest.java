package edu.controller;

import edu.KafkaIntegrationTest;
import edu.cofiguration.NoJpaConfig;
import edu.model.web.ScrapperRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.service.GetRequestsResolver;
import edu.web.BackendProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(NoJpaConfig.class)
class GetRequestsListenerTest extends KafkaIntegrationTest {
    @MockBean
    private GetRequestsResolver resolver;

    @MockBean
    private BackendProducer backendProducer;

    @Autowired
    private GetRequestsListener getRequestsListener;

    @Autowired
    private KafkaTemplate<String, ScrapperRequest> kafkaTemplate;

    @Test
    void listen() {
        ProducerRecord<String, ScrapperRequest> message = new ProducerRecord<>(
                "get_info",
                "id",
                new ArticlesForFeedRequest(5)
        );

        kafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(resolver, times(1))
                                .resolve(any())
                );

        verify(backendProducer, times(1))
                .sendDTOMessage(any());
    }
}