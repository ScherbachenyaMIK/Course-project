package edu.controller;

import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.security.JwtProvider;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import jakarta.servlet.http.Cookie;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleInteractionController.class)
@Import({SecurityConfig.class, NoKafkaConfig.class})
class ArticleInteractionControllerTest {
    @MockBean
    private ResponseHandler responseHandler;

    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @SneakyThrows
    @Test
    void recordView() {
        mockMvc.perform(post("/api/articles/1/view"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void likeArticleUnauthenticated() {
        mockMvc.perform(post("/api/articles/1/like"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void addCommentUnauthenticated() {
        mockMvc.perform(post("/api/articles/1/comments")
                        .param("text", "Nice article!"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void likeArticleAuthenticated() {
        String jwtToken = jwtProvider.generateToken("testUser", "USER");

        mockMvc.perform(post("/api/articles/1/like")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void addCommentAuthenticated() {
        String jwtToken = jwtProvider.generateToken("testUser", "USER");

        when(responseHandler.getApiResponse(anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        ResponseEntity.ok("comment")));

        mockMvc.perform(post("/api/articles/1/comments")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken))
                        .param("text", "Nice article!"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(print());
    }
}
