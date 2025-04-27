package edu.service;

import edu.model.web.AuthRequest;
import edu.model.web.AuthResponse;
import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthResolver {
    private static final String TOPIC_NAME = "authorization";

    @Autowired
    private AuthHandler authHandler;

    @SuppressWarnings("IllegalIdentifierName")
    public ProducerRecord<String, AuthResponse> resolve(ConsumerRecord<String, AuthRequest> record) {
        Objects.requireNonNull(record.key(), "Key must not be null");
        Objects.requireNonNull(record.value(), "Value must not be null");
        String type = record.value().getClass().toString();
        type = type.substring(type.lastIndexOf('.') + 1);
        switch (type) {
            case "LoginRequest":
                return new ProducerRecord<>(
                        TOPIC_NAME,
                        record.key(),
                        authHandler
                                .handleLogin(
                                        (LoginRequest) record.value()
                                )
                );
            case "CheckAvailabilityRequest":
                return new ProducerRecord<>(
                        TOPIC_NAME,
                        record.key(),
                        authHandler
                                .handleAvailability(
                                        (CheckAvailabilityRequest) record.value()
                                )
                );
            case "RegisterRequest":
                return new ProducerRecord<>(
                        TOPIC_NAME,
                        record.key(),
                        authHandler
                                .handleRegister(
                                        (RegisterRequest) record.value()
                                )
                );
            default:
                throw new IllegalArgumentException("Unknown request type: " + type);
        }
    }
}
