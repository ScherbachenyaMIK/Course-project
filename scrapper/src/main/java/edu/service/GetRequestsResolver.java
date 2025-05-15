package edu.service;

import edu.model.web.DTO;
import edu.model.web.ScrapperRequest;
import edu.model.web.request.ArticleRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetRequestsResolver {
    @Autowired
    private GetRequestsHandler getRequestsHandler;

    @SuppressWarnings("IllegalIdentifierName")
    public ProducerRecord<String, DTO> resolve(ConsumerRecord<String, ScrapperRequest> record) {
        Objects.requireNonNull(record.key(), "Key must not be null");
        Objects.requireNonNull(record.value(), "Value must not be null");
        String type = record.value().getClass().toString();
        type = type.substring(type.lastIndexOf('.') + 1);
        switch (type) {
            case "ArticlesForFeedRequest" -> {
                return new ProducerRecord<>(
                        "articles_for_feed",
                        record.key(),
                        getRequestsHandler
                                .handleFindArticlesRequest(
                                        (ArticlesForFeedRequest) record.value()
                                )
                );
            }
            case "ArticleRequest" -> {
                return new ProducerRecord<>(
                        "articles_showing",
                        record.key(),
                        getRequestsHandler
                                .handleArticleRequest(
                                        (ArticleRequest) record.value()
                                )
                );
            }
            default -> throw new IllegalArgumentException("Unknown request type: " + type);
        }
    }
}
