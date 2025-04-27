package edu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.configuration.SecurityConfig;
import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.service.ResponseHandler;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperProducer;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
@Import(SecurityConfig.class)
class ApiControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @MockBean
    private ScrapperProducer scrapperProducer;

    @MockBean
    private AuthorizationListener authorizationListener;

    @MockBean
    private ResponseHandler responseHandler;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void checkEmailAndUsername() throws Exception {
        when(responseHandler.getApiResponse(anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ResponseEntity<>(
                                new CheckAvailabilityResponse(
                                        true,
                                        true
                                ),
                                HttpStatus.OK
                        )
                ));

        CheckAvailabilityRequest request = new CheckAvailabilityRequest(
                "testUsername",
                "test@example.com"
        );

        mockMvc.perform(post("/api/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(print())
                .andDo(result -> assertThat(result.getAsyncResult())
                        .isExactlyInstanceOf(ResponseEntity.class)
                );
    }
}