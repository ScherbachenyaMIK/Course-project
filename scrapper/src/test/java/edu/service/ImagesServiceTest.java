package edu.service;

import edu.model.db.entity.Image;
import edu.model.db.entity.MimeType;
import edu.model.db.repository.ImagesRepository;
import edu.model.db.repository.MimeTypesRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
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
}
