package edu.controller;

import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.exception.UnsupportedMediaTypeException;
import edu.model.web.dto.ImageUploadResponseDTO;
import edu.service.ImageStorageService;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageUploadController.class)
@Import({SecurityConfig.class, NoKafkaConfig.class})
class ImageUploadControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;

    @MockBean
    private ImageStorageService imageStorageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void uploadUserIconReturnsResponse() throws Exception {
        ImageUploadResponseDTO dto = new ImageUploadResponseDTO(
                3L, "/user_icon/1", "image/png", "i.png"
        );
        when(imageStorageService.uploadUserIcon(eq(1L), any()))
                .thenReturn(Mono.just(ResponseEntity.ok(dto)));

        MockMultipartFile file = new MockMultipartFile(
                "file", "i.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1}
        );

        MvcResult result = mockMvc.perform(multipart("/resources/user_icon/1").file(file))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageId").value(3));
    }

    @Test
    @WithMockUser
    void uploadPreviewReturnsResponse() throws Exception {
        ImageUploadResponseDTO dto = new ImageUploadResponseDTO(
                7L, "/preview/9", "image/jpeg", "p.jpg"
        );
        when(imageStorageService.uploadPreview(eq(9L), any()))
                .thenReturn(Mono.just(ResponseEntity.ok(dto)));

        MockMultipartFile file = new MockMultipartFile(
                "file", "p.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1}
        );

        MvcResult result = mockMvc.perform(multipart("/resources/preview/9").file(file))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("/preview/9"));
    }

    @Test
    @WithMockUser
    void uploadArticleImageReturnsCreatedWithLocation() throws Exception {
        ImageUploadResponseDTO dto = new ImageUploadResponseDTO(
                42L, "/article/5/images/42", "image/png", "x.png"
        );
        when(imageStorageService.uploadArticleImage(eq(5L), any()))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(dto)));

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1}
        );

        MvcResult result = mockMvc.perform(multipart("/resources/article/5/images").file(file))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/resources/article/5/images/42"))
                .andExpect(jsonPath("$.imageId").value(42));
    }

    @Test
    @WithAnonymousUser
    void uploadRequiresAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "i.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1}
        );

        mockMvc.perform(multipart("/resources/user_icon/1").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void uploadReturns415WhenServiceRejectsMimeType() throws Exception {
        when(imageStorageService.uploadUserIcon(eq(1L), any()))
                .thenThrow(new UnsupportedMediaTypeException("Unsupported MIME type: application/pdf"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1}
        );

        mockMvc.perform(multipart("/resources/user_icon/1").file(file))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser
    void uploadReturnsBadGatewayOnScrapperFailure() throws Exception {
        when(imageStorageService.uploadUserIcon(eq(1L), any()))
                .thenThrow(new ScrapperUploadException(500, "boom"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "i.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1}
        );

        mockMvc.perform(multipart("/resources/user_icon/1").file(file))
                .andExpect(status().isBadGateway());
    }
}
