package edu.controller;

import edu.KafkaIntegrationTest;
import edu.cofiguration.NoJpaConfig;
import edu.model.web.AuthRequest;
import edu.model.web.ScrapperGetRequest;
import edu.model.web.request.ArticleSetupRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.LoginRequest;
import edu.service.AuthResolver;
import edu.service.GetRequestsResolver;
import edu.service.PostRequestsHandler;
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
class ListenerTest extends KafkaIntegrationTest {
    @MockBean
    private GetRequestsResolver getRequestsResolver;

    @MockBean
    private AuthResolver authResolver;

    @MockBean
    private BackendProducer backendProducer;

    @MockBean
    private PostRequestsHandler postRequestsHandler;

    @Autowired
    private GetRequestsListener getRequestsListener;

    @Autowired
    private AuthRequestsListener authRequestsListener;

    @Autowired
    private KafkaTemplate<String, ScrapperGetRequest> scrapperKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, AuthRequest> authKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, ArticleSetupRequest> articleSetupKafkaTemplate;

    @Test
    void listenScrapper() {
        ProducerRecord<String, ScrapperGetRequest> message = new ProducerRecord<>(
                "get_info",
                "id",
                new ArticlesForFeedRequest(5)
        );

        scrapperKafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(getRequestsResolver, times(1))
                                .resolve(any())
                );

        verify(backendProducer, times(1))
                .sendDTOMessage(any());
    }

    @Test
    void listenAuth() {
        ProducerRecord<String, AuthRequest> message = new ProducerRecord<>(
                "identification",
                "id",
                new LoginRequest("test", "test")
        );

        authKafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(authResolver, times(1))
                                .resolve(any())
                );

        verify(backendProducer, times(1))
                .sendAuthResponse(any());
    }

    @Test
    void listenArticleSetup() {
        ProducerRecord<String, ArticleSetupRequest> message = new ProducerRecord<>(
                "articles_setup",
                "id",
                new ArticleSetupRequest(1L, "test")
        );

        articleSetupKafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(postRequestsHandler, times(1))
                                .handleArticleSetupRequest(any())
                );

        verify(backendProducer, times(1))
                .sendDTOMessage(any());
    }
}