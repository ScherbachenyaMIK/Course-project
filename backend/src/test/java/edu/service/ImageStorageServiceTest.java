package edu.service;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.web.dto.ImageUploadResponseDTO;
import edu.web.ScrapperClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageStorageServiceTest {
    @Mock
    private ScrapperClient scrapperClient;

    @InjectMocks
    private ImageStorageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateAcceptsKnownMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "i.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{1});

        assertThat(service.validateAndGetMimeType(file)).isEqualTo("image/png");
    }

    @Test
    void validateRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "i.png",
                MediaType.IMAGE_PNG_VALUE, new byte[0]);

        assertThatThrownBy(() -> service.validateAndGetMimeType(file))
                .isInstanceOf(UnsupportedMediaTypeException.class);
    }

    @Test
    void validateRejectsMissingMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "i.png",
                null, new byte[]{1});

        assertThatThrownBy(() -> service.validateAndGetMimeType(file))
                .isInstanceOf(UnsupportedMediaTypeException.class);
    }

    @Test
    void validateRejectsUnknownMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "i.pdf",
                "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> service.validateAndGetMimeType(file))
                .isInstanceOf(UnsupportedMediaTypeException.class)
                .hasMessageContaining("application/pdf");
    }

    @Test
    void uploadUserIconForwardsToClient() {
        MockMultipartFile file = new MockMultipartFile("file", "icon.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        ImageUploadResponseDTO dto = new ImageUploadResponseDTO(7L, "/user_icon/3", "image/png", "icon.png");
        when(scrapperClient.uploadUserIcon(eq(3L), any(), eq("icon.png"), eq("image/png")))
                .thenReturn(Mono.just(ResponseEntity.ok(dto)));

        StepVerifier.create(service.uploadUserIcon(3L, file))
                .assertNext(resp -> assertThat(resp.getBody()).isEqualTo(dto))
                .verifyComplete();

        verify(scrapperClient).uploadUserIcon(eq(3L), any(), eq("icon.png"), eq("image/png"));
    }

    @Test
    void uploadPreviewUsesFallbackFilename() {
        MockMultipartFile file = new MockMultipartFile("file", "",
                MediaType.IMAGE_JPEG_VALUE, new byte[]{4});
        ImageUploadResponseDTO dto = new ImageUploadResponseDTO(8L, "/preview/2", "image/jpeg", "unnamed");
        when(scrapperClient.uploadPreview(eq(2L), any(), eq("unnamed"), eq("image/jpeg")))
                .thenReturn(Mono.just(ResponseEntity.ok(dto)));

        StepVerifier.create(service.uploadPreview(2L, file))
                .assertNext(resp -> assertThat(resp.getBody().filename()).isEqualTo("unnamed"))
                .verifyComplete();
    }

    @Test
    void uploadArticleImageInvokesClient() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{9});
        ImageUploadResponseDTO dto = new ImageUploadResponseDTO(15L, "/article/4/images/15",
                "image/png", "x.png");
        when(scrapperClient.uploadArticleImage(eq(4L), any(), eq("x.png"), eq("image/png")))
                .thenReturn(Mono.just(ResponseEntity.ok(dto)));

        StepVerifier.create(service.uploadArticleImage(4L, file))
                .assertNext(resp -> assertThat(resp.getBody().imageId()).isEqualTo(15L))
                .verifyComplete();
    }

    @Test
    void uploadFailsWhenValidationFails() {
        MultipartFile file = new MockMultipartFile("file", "doc.pdf",
                "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> service.uploadUserIcon(1L, file))
                .isInstanceOf(UnsupportedMediaTypeException.class);
    }
}
