package edu.controller;

import edu.KafkaIntegrationTest;
import edu.model.web.ArticleInformationDTO;
import edu.model.web.ArticlePreviewDTO;
import edu.service.ResponseHandler;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class GetResponsesListenerTest extends KafkaIntegrationTest {
    @MockBean
    private ResponseHandler handler;

    @Autowired
    private GetResponsesListener listener;

    @Autowired
    private KafkaTemplate<String, List<ArticlePreviewDTO>> kafkaTemplate;

    @Test
    void listen() {
        ProducerRecord<String, List<ArticlePreviewDTO>> message = new ProducerRecord<>(
                "articles_for_feed",
                "id",
                List.of(
                        new ArticlePreviewDTO(
                                null,
                                "Author 1",
                                "Title 1",
                                new ArticleInformationDTO(
                                        "tags",
                                        "categories",
                                        30,
                                        ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                                        "status",
                                        0,
                                        0,
                                        0
                                ),
                                null,
                                "Article 1",
                                null
                        )
                )
        );

        kafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(handler, times(1))
                                .completeResponseFeed(any(), any(), any())
                );
    }
}