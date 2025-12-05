package edu.service;

import edu.configuration.NoKafkaConfig;
import edu.model.web.dto.AIResponseDTO;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.UserDTO;
import edu.model.web.response.CheckAvailabilityResponse;
import java.util.List;
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
        ArticleFeedDTO articleFeedDTO = new ArticleFeedDTO(List.of());
        ModelAndView expected = new ModelAndView("");
        expected.addObject("articles", List.of());
        expected.addObject("isAuthenticated", false);

        CompletableFuture<ModelAndView> future = handler.getResponse(id, false);

        handler.completeResponseFeed(id, articleFeedDTO, "");

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
        ArticleFeedDTO articleFeedDTO = new ArticleFeedDTO(List.of());
        ModelAndView expected = new ModelAndView("");
        expected.addObject("articles", List.of());
        expected.addObject("isAuthenticated", true);

        CompletableFuture<ModelAndView> future = handler.getResponse(id, true);

        handler.completeResponseFeed(id, articleFeedDTO, "");

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
        CompletableFuture<ModelAndView> future = handler.getResponse(id, true);

        await()
                .atMost(11, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThatThrownBy(future::get)
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

    @SneakyThrows
    @Test
    void getResponseArticle() {
        ArticleDTO articleDTO = new ArticleDTO(
                null,
                "author",
                "title",
                "content",
                null,
                null,
                null
        );
        ModelAndView expected = new ModelAndView("");
        expected.addObject("article", articleDTO);
        expected.addObject("isAuthenticated", false);

        CompletableFuture<ModelAndView> future = handler.getResponse(id, false);

        handler.completeResponseArticle(id, articleDTO, "");

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
    void getResponseProfile() {
        UserDTO userDTO = new UserDTO(
                1L,
                "username",
                "name",
                "email",
                "05.05.2025",
                "description",
                "role",
                'M',
                "05.05.1975",
                List.of()
        );
        ModelAndView expected = new ModelAndView("user");
        expected.addObject("user", userDTO);
        expected.addObject("isAuthenticated", false);

        CompletableFuture<ModelAndView> future = handler.getResponse(id, false);

        handler.completeResponseProfile(id, userDTO, "user");

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
    void getResponseAI() {
        AIResponseDTO expectedResponse = new AIResponseDTO(
                "Response"
        );
        ResponseEntity<String> expected = new ResponseEntity<>(
                "Response",
                HttpStatus.OK
        );

        CompletableFuture<?> future = handler.getApiResponse(id);

        handler.completeResponseAI(id, expectedResponse);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .until(future::isDone);

        assertThat(future.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}