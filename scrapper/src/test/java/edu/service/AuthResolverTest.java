package edu.service;

import edu.configuration.ApplicationConfig;
import edu.model.web.AuthRequest;
import edu.model.web.AuthResponse;
import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.model.web.response.LoginResponse;
import edu.model.web.response.RegisterResponse;
import edu.util.KafkaConsumerLogger;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
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
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field field = AuthResolver.class.getDeclaredField("kafkaConsumerLogger");
        field.setAccessible(true);
        field.set(authResolver, new KafkaConsumerLogger());
        field = AuthResolver.class.getDeclaredField("applicationConfig");
        field.setAccessible(true);
        field.set(authResolver, new ApplicationConfig(10));
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
                System.currentTimeMillis(),
                TimestampType.CREATE_TIME,
                4,
                4,
                key,
                value,
                new RecordHeaders(),
                Optional.empty()
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

    @Test
    void resolveRegisterTimeout() {
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
                System.currentTimeMillis() - 20000,
                TimestampType.CREATE_TIME,
                4,
                4,
                key,
                value,
                new RecordHeaders(),
                Optional.empty()
        );
        RegisterResponse expectedResponse = new RegisterResponse(
                false
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