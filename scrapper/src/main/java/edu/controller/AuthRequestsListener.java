package edu.controller;

import edu.model.web.request.AuthRequest;
import edu.model.web.response.AuthResponse;
import edu.util.KafkaConsumerLogger;
import edu.web.BackendProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthRequestsListener {
    @Autowired
    private KafkaConsumerLogger kafkaConsumerLogger;

    @Autowired
    BackendProducer backendProducer;

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "identification")
    public void listen(ConsumerRecord<String, AuthRequest> record) {
        kafkaConsumerLogger.logRequest("identification", record);
        backendProducer.sendAuthResponse(record.key(), new AuthResponse(true,  "USER"));
    }
}
