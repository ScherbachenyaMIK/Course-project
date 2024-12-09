package edu.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@ConfigurationProperties(prefix = "kafka-config", ignoreUnknownFields = false)
public record KafkaConfig(
        String topicName,
        Integer countOfPartitions,
        Integer countOfReplicas,
        String dlqTopicName,
        Integer dlqCountOfPartitions,
        Integer dlqCountOfReplicas
) {
    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topicName())
                .partitions(countOfPartitions())
                .replicas(countOfReplicas())
                .build();
    }

    @SuppressWarnings("MethodName")
    @Bean
    public NewTopic topic_dql() {
        return TopicBuilder.name(dlqTopicName())
                .partitions(dlqCountOfPartitions())
                .replicas(dlqCountOfReplicas())
                .build();
    }
}
