package edu.web;

import edu.KafkaIntegrationTest;
import edu.cofiguration.NoJpaConfig;
import edu.model.web.DTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import java.time.Duration;
import java.time.ZonedDateTime;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(NoJpaConfig.class)
class BackendProducerTest extends KafkaIntegrationTest {
    @Autowired
    private BackendProducer producer;

    private static KafkaConsumer<String, List<DTO>> consumer;

    private final CompletableFuture<ConsumerRecord<String, List<DTO>>> future = new CompletableFuture<>();

    @BeforeAll
    static void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumers");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new KafkaConsumer<>(props);
    }

    @SneakyThrows
    @Test
    void sendDTOMessage() {
        consumer.subscribe(Collections.singletonList("articles_for_feed"));

        ProducerRecord<String, List<DTO>> message = new ProducerRecord<>(
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

        producer.sendDTOMessage(message);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer();
                    return future.isDone();
                        });

        assertThat(future.get().topic()).isEqualTo(message.topic());
        assertThat(future.get().key()).isEqualTo(message.key());
    }

    private void checkKafkaConsumer() {
        while (true) {
            ConsumerRecords<String, List<DTO>> records =
                    consumer.poll(Duration.ofMillis(1000));
            if (!records.isEmpty()) {
                assert(records.count() == 1);
                future.complete(records.iterator().next());
                return;
            }
        }
    }

    @AfterAll
    static void tearDown() {
        consumer.close();
    }
}