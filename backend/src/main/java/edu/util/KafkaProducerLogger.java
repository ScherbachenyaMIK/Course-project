package edu.util;

import edu.configuration.KafkaConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class KafkaProducerLogger {
    @Autowired
    private KafkaConfig kafkaConfig;

    public void logRequest(String topicName, Object request) {
        log.debug("Message pushed into queue");
        log.debug("Topic: {}", topicName);
        log.debug("Message body: {}", request);
    }
}
