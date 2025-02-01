package edu.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class KafkaProducerLogger {
    @SuppressWarnings("MagicNumber")
    public void logRequest(String topicName, Object request) {
        log.debug("Message pushed into queue");
        log.debug("Topic: {}", topicName);
        int length = request.toString().length();
        log.debug("Message body: {}", request.toString().substring(0, length > 300 ? 300 : length - 1));
    }
}
