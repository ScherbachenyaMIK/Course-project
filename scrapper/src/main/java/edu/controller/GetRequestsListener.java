package edu.controller;

import edu.model.web.DTO;
import edu.model.web.ScrapperRequest;
import edu.service.GetRequestsResolver;
import edu.util.KafkaConsumerLogger;
import edu.web.BackendProducer;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GetRequestsListener {
    @Autowired
    private KafkaConsumerLogger kafkaConsumerLogger;

    @Autowired
    private GetRequestsResolver resolver;

    @Autowired
    private BackendProducer backendProducer;

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "get_info")
    public void listen(ConsumerRecord<String, ScrapperRequest> record) {
        kafkaConsumerLogger.logRequest("get_info", record);
        ProducerRecord<String, List<DTO>> response = resolver.resolve(record);
        backendProducer.sendDTOMessage(response);
    }
}
