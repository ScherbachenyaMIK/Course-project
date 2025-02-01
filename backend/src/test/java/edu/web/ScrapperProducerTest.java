package edu.web;

import edu.KafkaIntegrationTest;
import edu.model.web.ScrapperRequest;
import edu.model.web.request.ArticlesForFeedRequest;
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

    private static KafkaConsumer<String, ScrapperRequest> consumer;

    private final CompletableFuture<ConsumerRecord<String, ScrapperRequest>> future = new CompletableFuture<>();

    @BeforeAll
    static void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumers");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "edu.model.web.**");
        consumer = new KafkaConsumer<>(props);
    }

    @SneakyThrows
    @Test
    void sendGetRequest() {
        consumer.subscribe(Collections.singletonList("get_info"));

        ArticlesForFeedRequest request = new ArticlesForFeedRequest(5);

        producer.sendGetRequest("get_info", "id", request);

        await()
                .atMost(10, SECONDS)
                .until(() -> {
                    checkKafkaConsumer();
                    return future.isDone();
                });

        assertThat(future.get().topic()).isEqualTo("get_info");
        assertThat(future.get().key()).isEqualTo("id");
        assertThat(future.get().value()).isEqualTo(request);
    }

    private void checkKafkaConsumer() {
        while (true) {
            ConsumerRecords<String, ScrapperRequest> records =
                    consumer.poll(Duration.ofMillis(1000));
            if (!records.isEmpty()) {
                assert(records.count() == 1);
                future.complete(records.iterator().next());
                return;
            }
        }
    }
}