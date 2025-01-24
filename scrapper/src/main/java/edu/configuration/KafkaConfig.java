package edu.configuration;

import edu.util.TopicProperties;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Getter
@Setter
@EnableKafka
@ConfigurationProperties(prefix = "kafka-config", ignoreUnknownFields = false)
public class KafkaConfig {
    private Map<String, TopicProperties> topics;

    @Autowired
    private GenericApplicationContext applicationContext;

    @Bean
    public Map<String, TopicProperties> topicsProperties() {
        topics.forEach((key, value) -> {
            NewTopic newTopic = TopicBuilder.name(key)
                    .partitions(value.partitions())
                    .replicas(value.replicas())
                    .build();
            applicationContext.registerBean(key, NewTopic.class, () -> newTopic);
        });
        return topics;
    }
}
