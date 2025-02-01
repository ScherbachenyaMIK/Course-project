package edu.controller;

import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperProducer;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
class ControllerTest {
    @MockBean
    private ScrapperProducer scrapperProducer;

    @MockBean
    private ResponseHandler responseHandler;

    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getHome() {
        when(responseHandler.getResponse(any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ModelAndView("Home"))
                );

        mockMvc.perform(get("/")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(result -> assertThat(result.getAsyncResult())
                        .isExactlyInstanceOf(ModelAndView.class));
    }

    @SneakyThrows
    @Test
    void handleError() {
        ModelAndView result = mockMvc.perform(get("/checked-error")
                        .with(httpBasic("test", "test")))
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