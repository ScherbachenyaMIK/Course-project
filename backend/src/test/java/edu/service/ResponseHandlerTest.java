package edu.service;

import edu.configuration.NoKafkaConfig;
import edu.model.web.response.CheckAvailabilityResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(NoKafkaConfig.class)
class ResponseHandlerTest {
    private final String id = "id";
    private final String role = "USER";

    @Autowired
    private ResponseHandler handler;

    @SneakyThrows
    @Test
    void getResponseFeed() {
        ModelAndView expected = new ModelAndView("");
        expected.addObject("articles", null);
        expected.addObject("isAuthenticated", false);

        CompletableFuture<ModelAndView> future = handler.getResponse(id, "NONE");

        handler.completeResponseFeed(id, null, "");

        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThat(future.get())
                .usingRecursiveComparison()
                .comparingOnlyFields(
                        "model",
                        "view")
                .isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void getResponseFeedAuthenticated() {
        ModelAndView expected = new ModelAndView("");
        expected.addObject("articles", null);
        expected.addObject("isAuthenticated", true);

        CompletableFuture<ModelAndView> future = handler.getResponse(id, "USER");

        handler.completeResponseFeed(id, null, "");

        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThat(future.get())
                .usingRecursiveComparison()
                .comparingOnlyFields(
                        "model",
                        "view")
                .isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void getResponseTimeout() {
        CompletableFuture<ModelAndView> future = handler.getResponse(id, "USER");

        await()
                .atMost(11, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThatThrownBy(() -> future.get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }

    @SneakyThrows
    @Test
    void getApiResponse() {
        CheckAvailabilityResponse expectedResponse = new CheckAvailabilityResponse(
                true,
                true
        );
        ResponseEntity<CheckAvailabilityResponse> expected = new ResponseEntity<>(
                expectedResponse,
                HttpStatus.OK
        );

        CompletableFuture<?> future = handler.getApiResponse(id);

        handler.completeAvailabilityResponse(id, expectedResponse);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThat(future.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void getApiResponseTimeout() {
        CompletableFuture<?> future = handler.getApiResponse(id);

        await()
                .atMost(11, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }
}