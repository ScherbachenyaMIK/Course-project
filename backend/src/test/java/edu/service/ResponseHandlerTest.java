package edu.service;

import edu.model.web.response.CheckAvailabilityResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class ResponseHandlerTest {
    private final String id = "id";

    @Autowired
    private ResponseHandler handler;

    @SneakyThrows
    @Test
    void getResponseFeed() {
        ModelAndView expected = new ModelAndView("");
        expected.addObject("articles", null);

        CompletableFuture<ModelAndView> future = handler.getResponse(id);

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
        CompletableFuture<ModelAndView> future = handler.getResponse(id);

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