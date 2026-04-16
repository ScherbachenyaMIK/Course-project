package edu.service;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.db.entity.Image;
import edu.model.db.entity.MimeType;
import edu.model.db.repository.ImagesRepository;
import edu.model.db.repository.MimeTypesRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImagesServiceTest {
    @Mock
    private ImagesRepository repository;

    @Mock
    private MimeTypesRepository mimeTypesRepository;

    @InjectMocks
    private ImagesService imagesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findImageByPath() {
        Image image = Image.builder()
                .id(1L)
                .path("/images/test.png")
                .filename("test.png")
                .mimeTypeId(1)
                .content(new byte[]{1, 2, 3})
                .build();

        when(repository.findImageByPath("/images/test.png"))
                .thenReturn(Optional.of(image));

        Image result = imagesService.findImageByPath("/images/test.png");

        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo("test.png");
    }

    @Test
    void findImageByPathNotFound() {
        when(repository.findImageByPath("/images/missing.png"))
                .thenReturn(Optional.empty());

        Image result = imagesService.findImageByPath("/images/missing.png");

        assertThat(result).isNull();
    }

    @Test
    void findImageType() {
        Image image = Image.builder()
                .id(1L)
                .path("/images/test.png")
                .filename("test.png")
                .mimeTypeId(1)
                .content(new byte[]{1, 2, 3})
                .build();

        MimeType mimeType = new MimeType(1L, "image/png");

        when(mimeTypesRepository.findMimeTypeById(1))
                .thenReturn(mimeType);

        String result = imagesService.findImageType(image);

        assertThat(result).isEqualTo("image/png");
    }

    @Test
    void validateAndGetMimeTypeReturnsMimeType() {
        MimeType mimeType = new MimeType(2L, "image/png");
        when(mimeTypesRepository.findByType("image/png")).thenReturn(Optional.of(mimeType));

        MimeType result = imagesService.validateAndGetMimeType("image/png");

        assertThat(result).isSameAs(mimeType);
    }

    @Test
    void validateAndGetMimeTypeNormalizesCase() {
        MimeType mimeType = new MimeType(2L, "image/png");
        when(mimeTypesRepository.findByType("image/png")).thenReturn(Optional.of(mimeType));

        MimeType result = imagesService.validateAndGetMimeType("Image/PNG");

        assertThat(result).isSameAs(mimeType);
    }

    @Test
    void validateAndGetMimeTypeThrowsWhenMissing() {
        assertThatThrownBy(() -> imagesService.validateAndGetMimeType(null))
                .isInstanceOf(UnsupportedMediaTypeException.class);
        assertThatThrownBy(() -> imagesService.validateAndGetMimeType(""))
                .isInstanceOf(UnsupportedMediaTypeException.class);
    }

    @Test
    void validateAndGetMimeTypeThrowsWhenUnknown() {
        when(mimeTypesRepository.findByType("application/pdf")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imagesService.validateAndGetMimeType("application/pdf"))
                .isInstanceOf(UnsupportedMediaTypeException.class)
                .hasMessageContaining("application/pdf");
    }

    @Test
    void saveImagePersistsEntity() {
        MimeType mimeType = new MimeType(3L, "image/png");
        byte[] content = {9, 8, 7};
        Image saved = Image.builder().id(42L).path("/p").filename("a").mimeTypeId(3).content(content).build();
        when(repository.save(any(Image.class))).thenReturn(saved);

        Image result = imagesService.saveImage("/p", "a", mimeType, content);

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(repository).save(captor.capture());
        Image passed = captor.getValue();
        assertThat(passed.getPath()).isEqualTo("/p");
        assertThat(passed.getFilename()).isEqualTo("a");
        assertThat(passed.getMimeTypeId()).isEqualTo(3);
        assertThat(passed.getContent()).isEqualTo(content);
        assertThat(result).isSameAs(saved);
    }

    @Test
    void replaceImageDeletesThenSaves() {
        MimeType mimeType = new MimeType(4L, "image/jpeg");
        byte[] content = {1};
        when(repository.save(any(Image.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        imagesService.replaceImage("/preview/5", "x.jpg", mimeType, content);

        verify(repository).deleteByPath("/preview/5");
        verify(repository).save(any(Image.class));
    }

    @Test
    void saveArticleImageUpdatesPathWithGeneratedId() {
        MimeType mimeType = new MimeType(5L, "image/png");
        byte[] content = {1, 2};
        when(repository.save(any(Image.class)))
                .thenAnswer(invocation -> {
                    Image image = invocation.getArgument(0);
                    if (image.getId() == null) {
                        image.setId(99L);
                    }
                    return image;
                });

        Image result = imagesService.saveArticleImage(7L, "img.png", mimeType, content);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getPath()).isEqualTo("/article/7/images/99");
        verify(repository, times(2)).save(any(Image.class));
    }
}
