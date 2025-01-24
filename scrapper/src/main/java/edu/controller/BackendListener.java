package edu.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class BackendListener {
    @KafkaListener(topics = "identification")
    public void listen(String data) {
        log.debug("Received message: {}", data);
    }
}
