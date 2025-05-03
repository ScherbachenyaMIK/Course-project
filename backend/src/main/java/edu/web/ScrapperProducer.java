package edu.web;

import edu.model.web.AuthRequest;
import edu.model.web.ScrapperRequest;
import edu.model.web.request.CheckAvailabilityRequest;
import edu.util.KafkaProducerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScrapperProducer {
    @Autowired
    private KafkaTemplate<String, ScrapperRequest> kafkaGetRequestsTemplate;

    @Autowired
    private KafkaTemplate<String, AuthRequest> kafkaAuthRequestsTemplate;

    @Autowired
    private KafkaTemplate<String, CheckAvailabilityRequest> kafkaAvailabilityRequestsTemplate;

    @Autowired
    private KafkaProducerLogger kafkaProducerLogger;

    public void sendGetRequest(String topic, String correlationId, ScrapperRequest request) {
        kafkaProducerLogger.logRequest(topic, correlationId + ": " + request);
        kafkaGetRequestsTemplate.send(topic, correlationId, request);
    }

    @SuppressWarnings("MultipleStringLiterals")
    public void sendAuthRequest(String correlationId, AuthRequest request) {
        kafkaProducerLogger.logRequest("identification", correlationId + ": " + request);
        kafkaAuthRequestsTemplate.send("identification", correlationId, request);
    }
}
