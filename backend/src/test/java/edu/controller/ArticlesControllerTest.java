package edu.controller;

import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.security.JwtProvider;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticlesController.class)
@Import({SecurityConfig.class, NoKafkaConfig.class})
class ArticlesControllerTest {
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
    void getArticle() {
        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("Article"))
                );

        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> assertThat(result.getAsyncResult())
                        .isExactlyInstanceOf(ModelAndView.class))
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getArticleCreateForm() {
        mockMvc.perform(get("/articles/new"))
                .andExpect(status().is3xxRedirection())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void createArticle() {
        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("Article"))
                );

        mockMvc.perform(post("/articles/new")
                        .param("title", "Test Title")
                        .param("content", "Test Content")
                        .param("tags", "java, spring")
                        .param("categories", "Tech"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getArticleEditFormUnauthenticated() {
        mockMvc.perform(get("/articles/1/edit"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> {
                    ModelAndView mav = (ModelAndView) result.getAsyncResult();
                    assertThat(mav.getViewName()).isEqualTo("redirect:/login");
                })
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void editArticleUnauthenticated() {
        mockMvc.perform(post("/articles/1/edit")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content")
                        .param("tags", "java")
                        .param("categories", "Tech"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> {
                    ModelAndView mav = (ModelAndView) result.getAsyncResult();
                    assertThat(mav.getViewName()).isEqualTo("redirect:/login");
                })
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getArticleCreateFormAuthenticated() {
        String jwtToken = jwtProvider.generateToken("testUser", "USER");

        ModelAndView result = mockMvc.perform(get("/articles/new")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken)))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("ArticleCreate");
    }

    @SneakyThrows
    @Test
    void createArticleAuthenticated() {
        String jwtToken = jwtProvider.generateToken("testUser", "USER");

        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("Article"))
                );

        mockMvc.perform(post("/articles/new")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken))
                        .param("title", "Test Title")
                        .param("content", "Test Content")
                        .param("tags", "java, spring")
                        .param("categories", "Tech"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getArticleEditFormAuthenticated() {
        String jwtToken = jwtProvider.generateToken("testUser", "USER");
        ArticleDTO articleDTO = new ArticleDTO(
                URI.create("/resources/standard_icon.png"),
                "testUser",
                "Title",
                "Content",
                new ArticleInformationDTO(
                        "#tag", "cat", 30, "date", "draft", 0, 0, 0
                ),
                "date",
                new ArrayList<>()
        );
        ModelAndView mav = new ModelAndView("Article");
        mav.addObject("article", articleDTO);

        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(mav));

        mockMvc.perform(get("/articles/1/edit")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> {
                    ModelAndView resultMav = (ModelAndView) result.getAsyncResult();
                    assertThat(resultMav).isNotNull();
                    assertThat(resultMav.getViewName()).isEqualTo("ArticleEdit");
                })
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getArticleEditFormNotAuthor() {
        String jwtToken = jwtProvider.generateToken("otherUser", "USER");
        ArticleDTO articleDTO = new ArticleDTO(
                URI.create("/resources/standard_icon.png"),
                "testUser",
                "Title",
                "Content",
                new ArticleInformationDTO(
                        "#tag", "cat", 30, "date", "draft", 0, 0, 0
                ),
                "date",
                new ArrayList<>()
        );
        ModelAndView mav = new ModelAndView("Article");
        mav.addObject("article", articleDTO);

        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(mav));

        mockMvc.perform(get("/articles/1/edit")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> {
                    ModelAndView resultMav = (ModelAndView) result.getAsyncResult();
                    assertThat(resultMav).isNotNull();
                    assertThat(resultMav.getViewName()).isEqualTo("redirect:/articles/1");
                })
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void editArticleAuthenticated() {
        String jwtToken = jwtProvider.generateToken("testUser", "USER");

        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("Article"))
                );

        mockMvc.perform(post("/articles/1/edit")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken))
                        .param("title", "Updated Title")
                        .param("content", "Updated Content")
                        .param("tags", "java")
                        .param("categories", "Tech")
                        .param("status", "published")
                        .param("timeToRead", "15"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(print());
    }
}
