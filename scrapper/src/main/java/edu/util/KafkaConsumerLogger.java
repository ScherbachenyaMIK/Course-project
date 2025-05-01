package edu.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class KafkaConsumerLogger {
    @SuppressWarnings("MagicNumber")
    public void logRequest(String topicName, Object request) {
        log.debug("Message received from queue");
        log.debug("Topic: {}", topicName);
        int length = request.toString().length();
        log.debug("Message body: {}", request.toString().substring(0, length > 300 ? 300 : length - 1));
    }

    public void logRequestTimeout(String key, long age) {
        log.warn("Discarded stale message: {} (age: {} ms)", key, age);
    }
}
