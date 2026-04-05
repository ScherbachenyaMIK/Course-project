package edu.controller;

import edu.model.web.DTO;
import edu.model.web.request.ArticleEditRequest;
import edu.model.web.request.ArticleSetupRequest;
import edu.model.web.request.CommentRequest;
import edu.model.web.request.LikeRequest;
import edu.model.web.request.ViewRequest;
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
    private static final String ARTICLES_SHOWING_TOPIC = "articles_showing";
    private static final String COMMENTS_SHOWING_TOPIC = "comments_showing";
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
                        ARTICLES_SHOWING_TOPIC,
                        null,
                        record.key(),
                        handler.handleArticleSetupRequest(record.value())
                );
        backendProducer.sendDTOMessage(response);
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "articles_editing")
    public void listenEdit(ConsumerRecord<String, ArticleEditRequest> record) {
        kafkaConsumerLogger.logRequest("articles_editing", record);
        ProducerRecord<String, DTO> response =
                new ProducerRecord<>(
                        ARTICLES_SHOWING_TOPIC,
                        null,
                        record.key(),
                        handler.handleArticleEditRequest(record.value())
                );
        backendProducer.sendDTOMessage(response);
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "article_views")
    public void listenView(ConsumerRecord<String, ViewRequest> record) {
        kafkaConsumerLogger.logRequest("article_views", record);
        handler.handleViewRequest(record.value());
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "article_likes")
    public void listenLike(ConsumerRecord<String, LikeRequest> record) {
        kafkaConsumerLogger.logRequest("article_likes", record);
        handler.handleLikeRequest(record.value());
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "commenting")
    public void listenComment(ConsumerRecord<String, CommentRequest> record) {
        kafkaConsumerLogger.logRequest("commenting", record);
        ProducerRecord<String, DTO> response =
                new ProducerRecord<>(
                        COMMENTS_SHOWING_TOPIC,
                        null,
                        record.key(),
                        handler.handleCommentRequest(record.value())
                );
        backendProducer.sendDTOMessage(response);
    }
}
