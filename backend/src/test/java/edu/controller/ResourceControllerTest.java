package edu.controller;

import edu.configuration.FakeResourceLoaderConfiguration;
import edu.util.StatusCodeDescriptor;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
@Import({FakeResourceLoaderConfiguration.class})
class ResourceControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @Autowired
    private MockMvc mockMvc;

    private final List<String> listOfPngResources = List.of(
            "/icon_p.png",
            "/sign_up_icon.png",
            "/sign_up_icon_g.png",
            "/log_in_icon.png",
            "/log_in_icon_g.png",
            "/magnifier.png",
            "/clock_icon.png",
            "/created_icon.png",
            "/status_icon.png",
            "/views_icon.png",
            "/likes_icon.png",
            "/comments_icon.png",
            "/standard_preview.png",
            "/standard_icon.png"
    );

    @SneakyThrows
    @Test
    void getIconSvg() {
        MockHttpServletResponse result = mockMvc.perform(get("/resources/icon.svg")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("image/svg+xml");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Icon.svg\"");
    }

    @SneakyThrows
    @Test
    void getIconPng() {
        for (String resource : listOfPngResources) {
            MockHttpServletResponse result =
                    mockMvc.perform(get("/resources" + resource)
                            .with(httpBasic("test", "test")))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            assertThat(result.getContentType())
                    .isEqualTo(MediaType.IMAGE_PNG.toString());
            assertThat(result.getHeader("Content-Disposition"))
                    .isEqualTo("inline; filename=\"" +
                            resource
                                    .substring(1, 2)
                                    .toUpperCase() +
                                    resource.substring(2)
                            + "\""
                    );
        }
    }
}