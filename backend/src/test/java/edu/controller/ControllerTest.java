package edu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.model.web.response.AuthenticationResponse;
import edu.model.web.response.LoginResponse;
import edu.model.web.response.RegisterResponse;
import edu.security.JwtProvider;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
@Import({SecurityConfig.class, NoKafkaConfig.class})
class ControllerTest {
    @Autowired
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
        when(responseHandler.getResponse(anyString(), anyBoolean()))
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
    void getLoginFragment() {
        ModelAndView result = mockMvc.perform(get("/login?fragment=true"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("Login :: login-form");
    }

    @SneakyThrows
    @Test
    void getLoginAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "username",
                        null,
                        List.of()
                )
        );

        ModelAndView result = mockMvc.perform(get("/login"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/");
    }

    @SneakyThrows
    @Test
    void getRegister() {
        ModelAndView result = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("Register");
    }

    @SneakyThrows
    @Test
    void getRegisterFragment() {
        ModelAndView result = mockMvc.perform(get("/register?fragment=true"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("Register :: register-form");
    }

    @SneakyThrows
    @Test
    void getRegisterAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "username",
                        null,
                        List.of()
                )
        );

        ModelAndView result = mockMvc.perform(get("/register"))
                .andExpect(status().isFound())
                .andReturn().getModelAndView();

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/");
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

        AuthenticationResponse result = new ObjectMapper().readValue(
                mockMvc.perform(post("/login")
                                .contentType("application/json")
                                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andExpect(cookie().exists("JWT_TOKEN"))
                        .andExpect(cookie().httpOnly("JWT_TOKEN", true))
                        .andExpect(cookie().secure("JWT_TOKEN", true))
                        .andExpect(cookie().path("JWT_TOKEN", "/"))
                        .andExpect(cookie().maxAge("JWT_TOKEN", 86400))
                        .andDo(print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                AuthenticationResponse.class
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCause()).isEqualTo("");
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

        AuthenticationResponse result = new ObjectMapper().readValue(
                mockMvc.perform(post("/login")
                                .contentType("application/json")
                                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                AuthenticationResponse.class
        );

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCause()).isEqualTo("BadCredentialsException");
    }

    @SneakyThrows
    @Test
    void postLoginAuthenticationServiceOffline() {
        when(authorizationListener.waitForResponse(anyString()))
                .thenThrow(new TimeoutException());

        LoginRequest loginRequest = new LoginRequest(
                "test",
                "test"
        );

        AuthenticationResponse result = new ObjectMapper().readValue(
                mockMvc.perform(post("/login")
                                .contentType("application/json")
                                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                AuthenticationResponse.class
        );

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCause()).isEqualTo("AuthenticationServiceException");
    }

    @SneakyThrows
    @Test
    void postRegister() {
        RegisterRequest request = new RegisterRequest(
                "test",
                "test",
                "test@example.com",
                "test",
                'M',
                LocalDate.of(2025, 11, 22)
        );

        when(authorizationListener.waitForResponse(anyString()))
                .thenReturn(new RegisterResponse(true));

        String result = mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content(
                                new ObjectMapper()
                                        .registerModule(new JavaTimeModule())
                                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                        .writeValueAsString(request)
                        ))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(result).isEqualTo("{\"success\":true}");
    }

    @SneakyThrows
    @Test
    void postRegisterException() {
        RegisterRequest request = new RegisterRequest(
                "test",
                "test",
                "test@example.com",
                "test",
                'M',
                LocalDate.of(2025, 11, 22)
        );

        when(authorizationListener.waitForResponse(anyString()))
                .thenThrow(new TimeoutException());

        String result = mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content(
                                new ObjectMapper()
                                        .registerModule(new JavaTimeModule())
                                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                        .writeValueAsString(request)
                        ))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(result).isEqualTo("{\"success\":false}");
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

    @SneakyThrows
    @Test
    void deleteLogin() {
        mockMvc.perform(delete("/login"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().value("JWT_TOKEN", ""))
                .andExpect(cookie().maxAge("JWT_TOKEN", 0))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}