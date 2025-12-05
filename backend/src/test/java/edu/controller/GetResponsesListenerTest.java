package edu.controller;

import edu.KafkaIntegrationTest;
import edu.model.web.dto.AIResponseDTO;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import edu.model.web.dto.UserDTO;
import edu.service.ResponseHandler;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private KafkaTemplate<String, ArticleFeedDTO> kafkaFeedTemplate;

    @Autowired
    private KafkaTemplate<String, ArticleDTO> kafkaArticleTemplate;

    @Autowired
    private KafkaTemplate<String, UserDTO> kafkaUserTemplate;

    @Autowired
    private KafkaTemplate<String, AIResponseDTO> kafkaAITemplate;

    @Test
    void listenFeed() {
        ProducerRecord<String, ArticleFeedDTO> message = new ProducerRecord<>(
                "articles_for_feed",
                "id",
                new ArticleFeedDTO(
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
                )
        );

        kafkaFeedTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(handler, times(1))
                                .completeResponseFeed(any(), any(), any())
                );
    }

    @Test
    void listenArticle() {
        ArrayList<String> comments = new ArrayList<>(List.of("Wow", "Amazing"));
        ProducerRecord<String, ArticleDTO> message = new ProducerRecord<>(
                "articles_showing",
                "id",
                new ArticleDTO(
                        URI.create("/resources/standard_icon.png"),
                        "Author 1",
                        "«Змейка» и «Тетрис»: почему они до сих пор с нами?",
                        "Как получилось, что простая игра, созданная советским программистом, и примитивная «Змейка» из телефонов 1990-х до сих пор удерживают игроков лучше, чем современные мультимиллионные блокбастеры? Ответ кроется в самой природе игрового удовольствия.",
                        new ArticleInformationDTO(
                                "#Programming",
                                "Programming",
                                30,
                                "05.05.2025 20:15",
                                "Finished",
                                15,
                                5,
                                5
                        ),
                        "05.05.2025 20:15",
                        comments
                )
        );

        kafkaArticleTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(handler, times(1))
                                .completeResponseArticle(any(), any(), any())
                );
    }

    @Test
    void listenProfile() {
        ProducerRecord<String, UserDTO> message = new ProducerRecord<>(
                "profile_showing",
                "id",
                new UserDTO(
                        1L,
                        "username",
                        "name",
                        "email",
                        "05.05.2025 20:15",
                        "description",
                        "role",
                        'M',
                        "05.05.1975 20:15",
                        List.of()
                )
        );

        kafkaUserTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(handler, times(1))
                                .completeResponseProfile(any(), any(), any())
                );
    }

    @Test
    void listenAI() {
        ProducerRecord<String, AIResponseDTO> message = new ProducerRecord<>(
                "ai_responses",
                "id",
                new AIResponseDTO(
                        "AI response"
                )
        );

        kafkaAITemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> verify(handler, times(1))
                                .completeResponseAI(any(), any())
                );
    }
}