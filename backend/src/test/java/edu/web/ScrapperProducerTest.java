package edu.web;

import edu.KafkaIntegrationTest;
import edu.model.web.AuthRequest;
import edu.model.web.ScrapperGetRequest;
import edu.model.web.ScrapperPostRequest;
import edu.model.web.request.ArticleSetupRequest;
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
import org.springframework.test.annotation.DirtiesContext;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ScrapperProducerTest extends KafkaIntegrationTest {
    @Autowired
    private ScrapperProducer producer;

    private static KafkaConsumer<String, ScrapperGetRequest> scrapperConsumer;
    private static KafkaConsumer<String, AuthRequest> authConsumer;
    private static KafkaConsumer<String, ScrapperPostRequest> postConsumer;

    private final CompletableFuture<ConsumerRecord<String, ScrapperGetRequest>> scrapperFuture = new CompletableFuture<>();
    private final CompletableFuture<ConsumerRecord<String, AuthRequest>> authFuture = new CompletableFuture<>();
    private final CompletableFuture<ConsumerRecord<String, ScrapperPostRequest>> postFuture = new CompletableFuture<>();

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
        postConsumer = new KafkaConsumer<>(props);
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

    @SneakyThrows
    @Test
    void sendPostRequest() {
        postConsumer.subscribe(Collections.singletonList("articles_setup"));

        ArticleSetupRequest request = new ArticleSetupRequest(
                1L,
                "Title"
        );

        producer.sendPostRequest("articles_setup", "id", request);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer(postConsumer, postFuture);
                    return postFuture.isDone();
                });

        assertThat(postFuture.get().topic()).isEqualTo("articles_setup");
        assertThat(postFuture.get().key()).isEqualTo("id");
        assertThat(postFuture.get().value()).isEqualTo(request);
        postConsumer.close();
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