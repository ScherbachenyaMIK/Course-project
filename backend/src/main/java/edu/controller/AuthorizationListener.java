package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.response.AuthResponse;
import edu.util.KafkaConsumerLogger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthorizationListener {
    @Autowired
    private KafkaConsumerLogger kafkaConsumerLogger;

    @Autowired
    private ApplicationConfig applicationConfig;

    private final ConcurrentHashMap<String, CompletableFuture<AuthResponse>> pendingResponses =
            new ConcurrentHashMap<>();

    public AuthResponse waitForResponse(String correlationId)
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        pendingResponses.put(correlationId, future);
        future.orTimeout(applicationConfig.timeout(), TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Response timeout with id {}", correlationId);
                    pendingResponses.remove(correlationId);
                    return null;
                });
        return future.get();
    }

    @SuppressWarnings("IllegalIdentifierName")
    @KafkaListener(topics = "authorization")
    public void listen(ConsumerRecord<String, AuthResponse> record) {
        kafkaConsumerLogger.logRequest("authorization", record);
        completeResponse(record.key(), record.value());
    }

    private void completeResponse(String correlationId, AuthResponse response) {
        CompletableFuture<AuthResponse> future = pendingResponses.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }
}
