package edu.controller;

import edu.util.StatusCodeDescriptor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StyleController.class)
@Import({DefaultResourceLoader.class})
class StyleControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getHome() {
        MockHttpServletResponse result = mockMvc.perform(get("/styles/home.css")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/css");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Home.css\"");
    }

    @SneakyThrows
    @Test
    void getError() {
        MockHttpServletResponse result = mockMvc.perform(get("/styles/error.css")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/css");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Error.css\"");
    }
}