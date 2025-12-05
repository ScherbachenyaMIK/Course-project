package edu.controller;

import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@Import({SecurityConfig.class, NoKafkaConfig.class})
class UsersControllerTest {
    @MockBean
    private ResponseHandler responseHandler;

    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getProfile() {
        when(responseHandler.getResponse(anyString(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("user"))
                );

        mockMvc.perform(get("/users/anonymousUser"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> assertThat(result.getAsyncResult())
                        .isExactlyInstanceOf(ModelAndView.class))
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getProfileNoResourceException() {
        mockMvc.perform(get("/users/someUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checked-error?HttpCode=404"))
                .andDo(print());
    }
}