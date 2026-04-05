package edu.controller;

import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                .andDo(print());
    }
}
