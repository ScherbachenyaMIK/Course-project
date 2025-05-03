package edu.controller;

import edu.model.web.AuthRequest;
import edu.model.web.AuthResponse;
import edu.service.AuthResolver;
import edu.util.KafkaConsumerLogger;
import edu.web.BackendProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthRequestsListener {
    @Autowired
    private KafkaConsumerLogger kafkaConsumerLogger;

    @Autowired
    private BackendProducer backendProducer;

    @Autowired
    private AuthResolver resolver;

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "identification")
    public void listen(ConsumerRecord<String, AuthRequest> record) {
        kafkaConsumerLogger.logRequest("identification", record);
        ProducerRecord<String, AuthResponse> response = resolver.resolve(record);
        backendProducer.sendAuthResponse(response);
    }
}
