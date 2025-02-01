package edu.controller;

import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperProducer;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
@Import({StatusCodeDescriptor.class})
class ExceptionControllerTest {
    @MockBean
    private ScrapperProducer scrapperProducer;

    @MockBean
    private ResponseHandler responseHandler;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void handleTimeout() {
        when(responseHandler.getResponse(any()))
                .thenAnswer(invocation -> { throw new TimeoutException(); });

        MvcResult result = mockMvc.perform(get("/")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isFound())
                .andReturn();

        assertThat(result.getModelAndView().getViewName())
                .isEqualTo("redirect:/checked-error");
        assertThat(result.getResponse().getRedirectedUrl())
                .isEqualTo("/checked-error?HttpCode=408&ResponseDescription=%D0%97%D0%B0%D0%BF%D1%80%D0%BE%D1%81+%D0%BE%D0%B1%D1%80%D0%B0%D0%B1%D0%B0%D1%82%D1%8B%D0%B2%D0%B0%D0%BB%D1%81%D1%8F+%D1%81%D0%BB%D0%B8%D1%88%D0%BA%D0%BE%D0%BC+%D0%B4%D0%BE%D0%BB%D0%B3%D0%BE%2C+%D0%B8+%D1%81%D0%B5%D1%80%D0%B2%D0%B5%D1%80+%D0%BF%D1%80%D0%B5%D1%80%D0%B2%D0%B0%D0%BB+%D1%81%D0%BE%D0%B5%D0%B4%D0%B8%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5.+%D0%9F%D0%BE%D0%BF%D1%80%D0%BE%D0%B1%D1%83%D0%B9%D1%82%D0%B5+%D0%BF%D0%BE%D0%B2%D1%82%D0%BE%D1%80%D0%B8%D1%82%D1%8C+%D0%BF%D0%BE%D0%B7%D0%B6%D0%B5.");
    }

    @SneakyThrows
    @Test
    void handleNotFound() {
        MvcResult result = mockMvc.perform(get("/not-used-url")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isFound())
                .andReturn();

        assertThat(result.getModelAndView().getViewName())
                .isEqualTo("redirect:/checked-error");
        assertThat(result.getResponse().getRedirectedUrl())
                .isEqualTo("/checked-error?HttpCode=404&ResponseDescription=%D0%A0%D0%B5%D1%81%D1%83%D1%80%D1%81%2C+%D0%BA+%D0%BA%D0%BE%D1%82%D0%BE%D1%80%D0%BE%D0%BC%D1%83+%D0%B2%D1%8B+%D0%BF%D1%8B%D1%82%D0%B0%D0%B5%D1%82%D0%B5%D1%81%D1%8C+%D0%BF%D0%BE%D0%BB%D1%83%D1%87%D0%B8%D1%82%D1%8C+%D0%B4%D0%BE%D1%81%D1%82%D1%83%D0%BF%2C+%D0%BD%D0%B5+%D1%81%D1%83%D1%89%D0%B5%D1%81%D1%82%D0%B2%D1%83%D0%B5%D1%82.+%D0%9F%D1%80%D0%BE%D0%B2%D0%B5%D1%80%D1%8C%D1%82%D0%B5+URL+%D0%B8%D0%BB%D0%B8+%D0%B2%D0%B5%D1%80%D0%BD%D0%B8%D1%82%D0%B5%D1%81%D1%8C+%D0%BD%D0%B0+%D0%B3%D0%BB%D0%B0%D0%B2%D0%BD%D1%83%D1%8E.");
    }

    @SneakyThrows
    @Test
    void handleUnknownException() {
        when(responseHandler.getResponse(any()))
                .thenThrow(new RuntimeException());

        MvcResult result = mockMvc.perform(get("/")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isFound())
                .andReturn();

        assertThat(result.getModelAndView().getViewName())
                .isEqualTo("redirect:/checked-error");
        assertThat(result.getResponse().getRedirectedUrl())
                .isEqualTo("/checked-error");
    }
}