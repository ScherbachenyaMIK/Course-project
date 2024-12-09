package edu.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class BackendListener {
    @KafkaListener(topics = "${kafka-config.topic-name}")
    public void listen(String data) {
        log.debug("Received message: {}", data);
    }
}
