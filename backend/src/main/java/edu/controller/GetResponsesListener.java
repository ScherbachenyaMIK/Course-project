package edu.controller;

import edu.model.web.dto.AIResponseDTO;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.CommentDTO;
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
    @KafkaListener(topics = "articles_searching")
    public void listenSearch(ConsumerRecord<String, ArticleFeedDTO> record) {
        kafkaConsumerLogger.logRequest("articles_searching", record);
        responseHandler.completeResponseFeed(record.key(), record.value(), "Search");
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

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "ai_responses")
    public void listenAI(ConsumerRecord<String, AIResponseDTO> record) {
        kafkaConsumerLogger.logRequest("ai_responses", record);
        responseHandler.completeResponseAI(record.key(), record.value());
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "comments_showing")
    public void listenComment(ConsumerRecord<String, CommentDTO> record) {
        kafkaConsumerLogger.logRequest("comments_showing", record);
        responseHandler.completeCommentResponse(record.key(), record.value());
    }
}
