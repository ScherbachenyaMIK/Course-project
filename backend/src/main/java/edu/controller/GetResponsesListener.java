package edu.controller;

import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.UserDTO;
import edu.service.ResponseHandler;
import edu.util.KafkaConsumerLogger;
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
    public void listenFeed(ConsumerRecord<String, ArticleFeedDTO> record) {
        kafkaConsumerLogger.logRequest("articles_for_feed", record);
        responseHandler.completeResponseFeed(record.key(), record.value(), "Home");
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "articles_showing")
    public void listenArticle(ConsumerRecord<String, ArticleDTO> record) {
        kafkaConsumerLogger.logRequest("articles_showing", record);
        responseHandler.completeResponseArticle(record.key(), record.value(), "Article");
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "profile_showing")
    public void listenProfile(ConsumerRecord<String, UserDTO> record) {
        kafkaConsumerLogger.logRequest("profile_showing", record);
        responseHandler.completeResponseProfile(record.key(), record.value(), "Profile");
    }
}
