package edu.web;

import edu.model.web.AuthRequest;
import edu.model.web.ScrapperGetRequest;
import edu.model.web.ScrapperPostRequest;
import edu.util.KafkaProducerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScrapperProducer {
    @Autowired
    private KafkaTemplate<String, ScrapperGetRequest> kafkaGetRequestsTemplate;

    @Autowired
    private KafkaTemplate<String, ScrapperPostRequest> kafkaPostRequestsTemplate;

    @Autowired
    private KafkaTemplate<String, AuthRequest> kafkaAuthRequestsTemplate;

    @Autowired
    private KafkaProducerLogger kafkaProducerLogger;

    public void sendGetRequest(String topic, String correlationId, ScrapperGetRequest request) {
        kafkaProducerLogger.logRequest(topic, correlationId + ": " + request);
        kafkaGetRequestsTemplate.send(topic, correlationId, request);
    }

    @SuppressWarnings("MultipleStringLiterals")
    public void sendAuthRequest(String correlationId, AuthRequest request) {
        kafkaProducerLogger.logRequest("identification", correlationId + ": " + request);
        kafkaAuthRequestsTemplate.send("identification", correlationId, request);
    }

    public void sendPostRequest(String topic, String correlationId, ScrapperPostRequest request) {
        kafkaProducerLogger.logRequest(topic, correlationId + ": " + request);
        kafkaPostRequestsTemplate.send(topic, correlationId, request);
    }
}
