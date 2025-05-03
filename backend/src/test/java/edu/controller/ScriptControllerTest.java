package edu.controller;

import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.util.StatusCodeDescriptor;
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

@WebMvcTest(ScriptController.class)
@Import({DefaultResourceLoader.class, SecurityConfig.class, NoKafkaConfig.class})
class ScriptControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPageScript() throws Exception {
        MockHttpServletResponse result = mockMvc.perform(get("/scripts/page.js"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/javascript");
    }

    @Test
    void getLoginScript() throws Exception {
        MockHttpServletResponse result = mockMvc.perform(get("/scripts/login.js"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/javascript");
    }

    @Test
    void getRegisterScript() throws Exception {
        MockHttpServletResponse result = mockMvc.perform(get("/scripts/register.js"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("text/javascript");
    }
}