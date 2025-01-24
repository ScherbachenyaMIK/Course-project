package edu.service;

import edu.configuration.KafkaConfig;
import edu.util.KafkaProducerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScrapperService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaProducerLogger kafkaProducerLogger;

    public void sendUpdate(String topicName, Object request) {
        kafkaProducerLogger.logRequest(topicName, request);
        kafkaTemplate.send(topicName, request.toString());
    }
}
