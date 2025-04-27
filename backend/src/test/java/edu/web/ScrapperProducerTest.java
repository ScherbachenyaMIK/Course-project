package edu.web;

import edu.KafkaIntegrationTest;
import edu.model.web.AuthRequest;
import edu.model.web.ScrapperRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.LoginRequest;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class ScrapperProducerTest extends KafkaIntegrationTest {
    @Autowired
    private ScrapperProducer producer;

    private static KafkaConsumer<String, ScrapperRequest> scrapperConsumer;
    private static KafkaConsumer<String, AuthRequest> authConsumer;

    private final CompletableFuture<ConsumerRecord<String, ScrapperRequest>> scrapperFuture = new CompletableFuture<>();
    private final CompletableFuture<ConsumerRecord<String, AuthRequest>> authFuture = new CompletableFuture<>();

    @BeforeAll
    static void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumers");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "edu.model.web.**");
        scrapperConsumer = new KafkaConsumer<>(props);
        authConsumer = new KafkaConsumer<>(props);
    }

    @SneakyThrows
    @Test
    void sendGetRequest() {
        scrapperConsumer.subscribe(Collections.singletonList("get_info"));

        ArticlesForFeedRequest request = new ArticlesForFeedRequest(5);

        producer.sendGetRequest("get_info", "id", request);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer(scrapperConsumer, scrapperFuture);
                    return scrapperFuture.isDone();
                });

        assertThat(scrapperFuture.get().topic()).isEqualTo("get_info");
        assertThat(scrapperFuture.get().key()).isEqualTo("id");
        assertThat(scrapperFuture.get().value()).isEqualTo(request);
        scrapperConsumer.close();
    }

    @SneakyThrows
    @Test
    void sendAuthRequest() {
        authConsumer.subscribe(Collections.singletonList("identification"));

        LoginRequest request = new LoginRequest("test", "test");

        producer.sendAuthRequest("id", request);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer(authConsumer, authFuture);
                    return authFuture.isDone();
                });

        assertThat(authFuture.get().topic()).isEqualTo("identification");
        assertThat(authFuture.get().key()).isEqualTo("id");
        assertThat(authFuture.get().value()).isEqualTo(request);
        authConsumer.close();
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