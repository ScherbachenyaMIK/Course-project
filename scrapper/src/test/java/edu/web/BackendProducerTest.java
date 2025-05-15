package edu.web;

import edu.KafkaIntegrationTest;
import edu.cofiguration.NoJpaConfig;
import edu.model.web.AuthResponse;
import edu.model.web.DTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import edu.model.web.response.LoginResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.annotation.DirtiesContext;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(NoJpaConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BackendProducerTest extends KafkaIntegrationTest {
    @Autowired
    private BackendProducer producer;

    private static KafkaConsumer<String, DTO> DTOConsumer;
    private static KafkaConsumer<String, AuthResponse> AuthConsumer;

    private final CompletableFuture<ConsumerRecord<String, DTO>> DTOFuture = new CompletableFuture<>();
    private final CompletableFuture<ConsumerRecord<String, AuthResponse>> AuthFuture = new CompletableFuture<>();

    @BeforeAll
    static void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumers");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "edu.model.web.**");
        DTOConsumer = new KafkaConsumer<>(props);
        AuthConsumer = new KafkaConsumer<>(props);
    }

    @SneakyThrows
    @Test
    void sendDTOMessage() {
        DTOConsumer.subscribe(Collections.singletonList("articles_for_feed"));

        ProducerRecord<String, DTO> message = new ProducerRecord<>(
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
                                                null,
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

        producer.sendDTOMessage(message);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer(DTOConsumer, DTOFuture);
                    return DTOFuture.isDone();
                        });

        assertThat(DTOFuture.get().topic()).isEqualTo(message.topic());
        assertThat(DTOFuture.get().key()).isEqualTo(message.key());
        DTOConsumer.close();
    }

    @SneakyThrows
    @Test
    void sendAuthResponse() {
        AuthConsumer.subscribe(Collections.singletonList("authorization"));

        ProducerRecord<String, AuthResponse> message = new ProducerRecord<>(
                "authorization",
                "id",
                new LoginResponse(true, "USER")
        );

        producer.sendAuthResponse(message);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer(AuthConsumer, AuthFuture);
                    return AuthFuture.isDone();
                });

        assertThat(AuthFuture.get().topic()).isEqualTo(message.topic());
        assertThat(AuthFuture.get().key()).isEqualTo(message.key());
        AuthConsumer.close();
    }

    private <K, V> void checkKafkaConsumer(KafkaConsumer<K, V> consumer,
                                           CompletableFuture<ConsumerRecord<K, V>> future) {
        while (true) {
            ConsumerRecords<K, V> records =
                    consumer.poll(Duration.ofMillis(1000));
            if (!records.isEmpty()) {
                assert(records.count() == 1);
                future.complete(records.iterator().next());
                return;
            }
        }
    }
}