package edu.controller;

import edu.model.web.request.AuthRequest;
import edu.service.AuthHandler;
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
    private BackendProducer backendProducer;

    @Autowired
    private AuthHandler handler;

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "identification")
    public void listen(ConsumerRecord<String, AuthRequest> record) {
        kafkaConsumerLogger.logRequest("identification", record);
        backendProducer.sendAuthResponse(record.key(), handler.handle(record.value()));
    }
}
