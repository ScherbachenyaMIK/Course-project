package edu.web;

import edu.model.web.AuthResponse;
import edu.model.web.DTO;
import edu.util.KafkaProducerLogger;
import java.util.List;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BackendProducer {
    @Autowired
    private KafkaTemplate<String, List<DTO>> kafkaDTOTemplate;

    @Autowired
    private KafkaTemplate<String, AuthResponse> kafkaAuthTemplate;

    @Autowired
    private KafkaProducerLogger kafkaProducerLogger;

    @SuppressWarnings("IllegalIdentifierName")
    public void sendDTOMessage(ProducerRecord<String, List<DTO>> record) {
        kafkaProducerLogger.logRequest(record.topic(), record.key() + ": " + record.value());
        kafkaDTOTemplate.send(record);
    }

    @SuppressWarnings("IllegalIdentifierName")
    public void sendAuthResponse(ProducerRecord<String, AuthResponse> record) {
        kafkaProducerLogger.logRequest(
                record.topic(), record.key() + ": " + record);
        kafkaAuthTemplate.send(record);
    }
}
