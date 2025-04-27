package edu.service;

import edu.model.web.AuthRequest;
import edu.model.web.AuthResponse;
import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.model.web.response.LoginResponse;
import edu.model.web.response.RegisterResponse;
import java.time.LocalDate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthResolverTest {
    @Mock
    private AuthHandler authHandler;

    @InjectMocks
    private AuthResolver authResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void resolveLogin() {
        String key = "id";
        LoginRequest value = new LoginRequest("test", "test");
        ConsumerRecord<String, AuthRequest> request = new ConsumerRecord<>(
                "identification",
                2,
                0,
                key,
                value
        );
        LoginResponse expectedResponse = new LoginResponse(true, "User");
        ProducerRecord<String, AuthResponse> expected = new ProducerRecord<>(
                "authorization",
                key,
                expectedResponse
        );

        when(authHandler.handleLogin(value)).thenReturn(expectedResponse);

        ProducerRecord<String, AuthResponse> result = authResolver.resolve(request);

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void resolveCheckAvailability() {
        String key = "id";
        CheckAvailabilityRequest value = new CheckAvailabilityRequest("test", "test@mail.ru");
        ConsumerRecord<String, AuthRequest> request = new ConsumerRecord<>(
                "identification",
                2,
                0,
                key,
                value
        );
        CheckAvailabilityResponse expectedResponse = new CheckAvailabilityResponse(
                true,
                false
        );
        ProducerRecord<String, AuthResponse> expected = new ProducerRecord<>(
                "authorization",
                key,
                expectedResponse
        );

        when(authHandler.handleAvailability(value)).thenReturn(expectedResponse);

        ProducerRecord<String, AuthResponse> result = authResolver.resolve(request);

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void resolveRegister() {
        String key = "id";
        RegisterRequest value = new RegisterRequest(
                "test",
                "test",
                "test@mail.ru",
                "test",
                'M',
                LocalDate.now()
        );
        ConsumerRecord<String, AuthRequest> request = new ConsumerRecord<>(
                "identification",
                2,
                0,
                key,
                value
        );
        RegisterResponse expectedResponse = new RegisterResponse(
                true
        );
        ProducerRecord<String, AuthResponse> expected = new ProducerRecord<>(
                "authorization",
                key,
                expectedResponse
        );

        when(authHandler.handleRegister(value)).thenReturn(expectedResponse);

        ProducerRecord<String, AuthResponse> result = authResolver.resolve(request);

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }
}