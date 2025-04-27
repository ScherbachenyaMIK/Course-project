package edu.controller;

import edu.KafkaIntegrationTest;
import edu.model.web.AuthResponse;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.model.web.response.LoginResponse;
import edu.model.web.response.RegisterResponse;
import edu.service.ResponseHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AuthorizationListenerTest extends KafkaIntegrationTest {
    @MockBean
    private ResponseHandler handler;

    @Autowired
    private AuthorizationListener listener;

    @Autowired
    private KafkaTemplate<String, AuthResponse> kafkaTemplate;

    @Test
    void listenLoginResponse() {
        String id = "id1";
        ProducerRecord<String, AuthResponse> message = new ProducerRecord<>(
                "authorization",
                id,
                new LoginResponse(true, "USER")
        );

        CompletableFuture<AuthResponse> result = CompletableFuture.supplyAsync(() -> {
            try {
                return listener.waitForResponse(id);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });

        kafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    assertThat(result.isDone())
                            .isTrue();
                    LoginResponse response = (LoginResponse) result.get();
                    assertThat(response.success())
                            .isTrue();
                    assertThat(response.role())
                            .isEqualTo("USER");
                });
    }

    @Test
    void listenCheckAvailabilityResponse() {
        String id = "id2";
        ProducerRecord<String, AuthResponse> message = new ProducerRecord<>(
                "authorization",
                id,
                new CheckAvailabilityResponse(
                        true,
                        true
                )
        );

        kafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() ->
                        verify(handler, times(1))
                                .completeAvailabilityResponse(anyString(), any())
                );
    }

    @Test
    void listenRegisterResponse() {
        String id = "id3";
        ProducerRecord<String, AuthResponse> message = new ProducerRecord<>(
                "authorization",
                id,
                new RegisterResponse(false)
        );

        CompletableFuture<AuthResponse> result = CompletableFuture.supplyAsync(() -> {
            try {
                return listener.waitForResponse(id);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });

        kafkaTemplate.send(message);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    assertThat(result.isDone())
                            .isTrue();
                    RegisterResponse response = (RegisterResponse) result.get();
                    assertThat(response.success())
                            .isFalse();
                });
    }

    @SneakyThrows
    @Test
    void listenTimeout() {
        await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThatThrownBy(() -> listener.waitForResponse("id4"))
                                .isInstanceOf(ExecutionException.class)
                                .hasCauseInstanceOf(TimeoutException.class)
                );
    }
}