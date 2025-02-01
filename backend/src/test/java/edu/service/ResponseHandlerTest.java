package edu.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
                .atMost(15, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThatThrownBy(() -> future.get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }
}