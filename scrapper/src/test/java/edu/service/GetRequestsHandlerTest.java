package edu.service;

import edu.model.web.DTO;
import edu.model.web.ScrapperRequest;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import edu.model.web.request.ArticlesForFeedRequest;
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

class GetRequestsHandlerTest {
    @Mock
    GetRequestsHandler handler;
    @InjectMocks
    GetRequestsResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleArticlesForFeedRequest() {
        ConsumerRecord<String, ScrapperRequest> record = new ConsumerRecord<>(
                "topic",
                1,
                0,
                "key",
                new ArticlesForFeedRequest(
                        5
                )
        );
        List<DTO> listOfArticles = List.of(
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
        );
        ProducerRecord<String, List<DTO>> expected = new ProducerRecord<>(
                "articles_for_feed",
                record.key(),
                listOfArticles
        );

        when(handler
                .handleFindArticlesRequest(any()))
                .thenReturn(listOfArticles);

        ProducerRecord<String, List<DTO>> response = resolver.resolve(record);

        assertThat(response)
                .usingRecursiveComparison()
                .comparingOnlyFields(
                        "topic",
                        "key",
                        "value"
                ).isEqualTo(expected);
    }

    @Test
    void handleNullKey() {
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
    void handleNullValue() {
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