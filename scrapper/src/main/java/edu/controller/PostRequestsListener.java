package edu.controller;

import edu.model.web.DTO;
import edu.model.web.request.ArticleSetupRequest;
import edu.service.PostRequestsHandler;
import edu.util.KafkaConsumerLogger;
import edu.web.BackendProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PostRequestsListener {
    @Autowired
    private KafkaConsumerLogger kafkaConsumerLogger;

    @Autowired
    private PostRequestsHandler handler;

    @Autowired
    private BackendProducer backendProducer;

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "articles_setup")
    public void listen(ConsumerRecord<String, ArticleSetupRequest> record) {
        kafkaConsumerLogger.logRequest("articles_setup", record);
        ProducerRecord<String, DTO> response =
                new ProducerRecord<>(
                        "articles_showing",
                        null,
                        record.key(),
                        handler.handleArticleSetupRequest(record.value())
                );
        backendProducer.sendDTOMessage(response);
    }
}
