package edu.service;

import edu.model.web.DTO;
import edu.model.web.ScrapperRequest;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import edu.model.web.request.ArticleRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GetRequestsResolverTest {
    @Mock
    private GetRequestsHandler handler;
    @InjectMocks
    private GetRequestsResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void resolveArticlesForFeedRequest() {
        ConsumerRecord<String, ScrapperRequest> record = new ConsumerRecord<>(
                "topic",
                1,
                0,
                "key",
                new ArticlesForFeedRequest(
                        5
                )
        );
        DTO listOfArticles = new ArticleFeedDTO(
                List.of(
                        new ArticlePreviewDTO(
                                null,
                                "Author 1",
                                "Title 1",
                                new ArticleInformationDTO(
                                        "tags",
                                        "categories",
                                        30,
                                        ZonedDateTime.now(),
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
        ProducerRecord<String, DTO> expected = new ProducerRecord<>(
                "articles_for_feed",
                record.key(),
                listOfArticles
        );

        when(handler
                .handleFindArticlesRequest(any()))
                .thenReturn(listOfArticles);

        ProducerRecord<String, DTO> response = resolver.resolve(record);

        assertThat(response)
                .usingRecursiveComparison()
                .comparingOnlyFields(
                        "topic",
                        "key",
                        "value"
                ).isEqualTo(expected);
    }

    @Test
    void resolveArticleRequest() {
        ConsumerRecord<String, ScrapperRequest> record = new ConsumerRecord<>(
                "topic",
                1,
                0,
                "key",
                new ArticleRequest(
                        1L,
                        5
                )
        );
        DTO article = new ArticleDTO(
                URI.create("/resources/standard_icon.png"),
                "Author 1",
                "«Змейка» и «Тетрис»: почему они до сих пор с нами?",
                "Как получилось, что простая игра, созданная советским программистом, и примитивная «Змейка» из телефонов 1990-х до сих пор удерживают игроков лучше, чем современные мультимиллионные блокбастеры? Ответ кроется в самой природе игрового удовольствия.",
                new ArticleInformationDTO(
                        "#Programming",
                        "Programming",
                        30,
                        null,
                        "Finished",
                        15,
                        5,
                        5
                ),
                null,
                null
        );
        ProducerRecord<String, DTO> expected = new ProducerRecord<>(
                "articles_showing",
                record.key(),
                article
        );

        when(handler
                .handleArticleRequest(any()))
                .thenReturn(article);

        ProducerRecord<String, DTO> response = resolver.resolve(record);

        assertThat(response)
                .usingRecursiveComparison()
                .comparingOnlyFields(
                        "topic",
                        "key",
                        "value"
                ).isEqualTo(expected);
    }

    @Test
    void resolveNullKey() {
        ConsumerRecord<String, ScrapperRequest> record = new ConsumerRecord<>(
                "topic",
                1,
                0,
                null,
                new ArticlesForFeedRequest(
                        5
                )
        );

        assertThatThrownBy(() -> resolver.resolve(record))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Key must not be null");

    }

    @Test
    void resolveNullValue() {
        ConsumerRecord<String, ScrapperRequest> record = new ConsumerRecord<>(
                "topic",
                1,
                0,
                "key",
                null
        );

        assertThatThrownBy(() -> resolver.resolve(record))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Value must not be null");

    }
}