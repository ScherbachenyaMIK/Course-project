package edu.controller;

import edu.model.web.ArticlePreviewDTO;
import edu.service.ResponseHandler;
import edu.util.KafkaConsumerLogger;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GetResponsesListener {
    @Autowired
    private KafkaConsumerLogger kafkaConsumerLogger;

    @Autowired
    private ResponseHandler responseHandler;

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "articles_for_feed")
    public void listen(ConsumerRecord<String, List<ArticlePreviewDTO>> record) {
        kafkaConsumerLogger.logRequest("articles_for_feed", record);
        responseHandler.completeResponseFeed(record.key(), record.value(), "Home");
    }
}
