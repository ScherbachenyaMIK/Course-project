package edu.controller;

import edu.configuration.SecurityConfig;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperProducer;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StyleController.class)
@Import({DefaultResourceLoader.class, SecurityConfig.class})
class StyleControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @MockBean
    private ScrapperProducer scrapperProducer;

    @MockBean
    private AuthorizationListener authorizationListener;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getHome() {
        MockHttpServletResponse result = mockMvc.perform(get("/styles/home.css"))
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
        MockHttpServletResponse result = mockMvc.perform(get("/styles/error.css"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/css");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Error.css\"");
    }

    @Test
    void getLogin() throws Exception {
        MockHttpServletResponse result = mockMvc.perform(get("/styles/login.css"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/css");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Login.css\"");
    }

    @Test
    void getRegister() throws Exception {
        MockHttpServletResponse result = mockMvc.perform(get("/styles/register.css"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/css");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Register.css\"");
    }
}