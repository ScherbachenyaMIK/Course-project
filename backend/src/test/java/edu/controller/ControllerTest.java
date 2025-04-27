package edu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.configuration.SecurityConfig;
import edu.model.web.request.LoginRequest;
import edu.model.web.response.LoginResponse;
import edu.security.JwtProvider;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperProducer;
import jakarta.servlet.http.Cookie;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
@Import(SecurityConfig.class)
class ControllerTest {
    @MockBean
    private ScrapperProducer scrapperProducer;

    @MockBean
    private AuthorizationListener authorizationListener;

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
    void getHome() {
        when(responseHandler.getResponse(any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("Home"))
                );

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> assertThat(result.getAsyncResult())
                        .isExactlyInstanceOf(ModelAndView.class));
    }

    @SneakyThrows
    @Test
    void getLogin() {
        ModelAndView result = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("Login");
    }

    @SneakyThrows
    @Test
    void getRegister() {
        ModelAndView result = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("Register");
        assertThat(result.getModelMap().getAttribute("registerRequest"))
                .isNotNull();
    }

    @SneakyThrows
    @Test
    void postLogin() {
        when(authorizationListener.waitForResponse(anyString()))
                .thenReturn(new LoginResponse(true, "USER"));

        LoginRequest loginRequest = new LoginRequest(
                "test",
                "test"
        );

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void postLoginBadCredentials() {
        when(authorizationListener.waitForResponse(anyString()))
                .thenReturn(new LoginResponse(false, "NONE"));

        LoginRequest loginRequest = new LoginRequest(
                "test",
                "test"
        );

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/checked-error"))
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void postRegister() {
        mockMvc.perform(post("/register")
                        .contentType("application/x-www-form-urlencoded")
                        .param("username", "test")
                        .param("name", "test")
                        .param("email", "test@example.com")
                        .param("passwordHash", "test")
                        .param("sex", "M")
                        .param("date", "2025-11-22"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void postRegisterBadCredentials() {
        mockMvc.perform(post("/register")
                        .contentType("application/x-www-form-urlencoded")
                        .param("username", "test")
                        .param("name", "test")
                        .param("email", "test@example.com")
                        .param("passwordHash", "test")
                        .param("sex", "AaAaA")
                        .param("date", "2025-11-22"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/checked-error"))
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getPage() {
        mockMvc.perform(get("/authorized"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getPageWithAuthority() {
        String jwtToken = jwtProvider.generateToken("test");

        mockMvc.perform(get("/authorized")
                        .cookie(new Cookie("JWT_TOKEN", jwtToken)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void handleError() {
        ModelAndView result = mockMvc.perform(get("/checked-error"))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView();
        assertThat(result.getViewName())
                .isEqualTo("Error");
        assertThat(result.getModel().get("HttpCode"))
                .isEqualTo(500);
        assertThat(result.getModel().get("ResponseDescription"))
                .isEqualTo("Неизвестная ошибка.");
    }
}