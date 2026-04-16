package edu.controller;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.db.entity.Image;
import edu.model.db.entity.MimeType;
import edu.service.ImageProcessingService;
import edu.service.ImagesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalImagesController.class)
class InternalImagesControllerTest {
    @MockBean
    private ImagesService imagesService;

    @MockBean
    private ImageProcessingService imageProcessingService;

    @Autowired
    private MockMvc mockMvc;

    private final MimeType pngMimeType = new MimeType(3L, "image/png");

    @Test
    void uploadUserIcon() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "icon.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3}
        );
        when(imagesService.validateAndGetMimeType("image/png")).thenReturn(pngMimeType);
        Image saved = Image.builder().id(11L).path("/user_icon/5").filename("icon.png")
                .mimeTypeId(3).content(file.getBytes()).build();
        when(imagesService.replaceImage(eq("/user_icon/5"), eq("icon.png"), eq(pngMimeType), any()))
                .thenReturn(saved);

        mockMvc.perform(multipart("/internal/images/user-icon/5").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageId").value(11))
                .andExpect(jsonPath("$.path").value("/user_icon/5"))
                .andExpect(jsonPath("$.mimeType").value("image/png"));

        verify(imageProcessingService).processAsync(11L, "/user_icon/5", "image/png");
    }

    @Test
    void uploadPreviewReplacesExisting() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "p.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{4, 5}
        );
        MimeType jpeg = new MimeType(1L, "image/jpeg");
        when(imagesService.validateAndGetMimeType("image/jpeg")).thenReturn(jpeg);
        Image saved = Image.builder().id(22L).path("/preview/12").filename("p.jpg")
                .mimeTypeId(1).content(file.getBytes()).build();
        when(imagesService.replaceImage(eq("/preview/12"), eq("p.jpg"), eq(jpeg), any()))
                .thenReturn(saved);

        mockMvc.perform(multipart("/internal/images/preview/12").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("/preview/12"));

        verify(imagesService).replaceImage(eq("/preview/12"), eq("p.jpg"), eq(jpeg), any());
    }

    @Test
    void uploadArticleImageReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", MediaType.IMAGE_PNG_VALUE, new byte[]{7}
        );
        when(imagesService.validateAndGetMimeType("image/png")).thenReturn(pngMimeType);
        Image saved = Image.builder().id(101L).path("/article/4/images/101").filename("a.png")
                .mimeTypeId(3).content(file.getBytes()).build();
        when(imagesService.saveArticleImage(eq(4L), eq("a.png"), eq(pngMimeType), any()))
                .thenReturn(saved);

        mockMvc.perform(multipart("/internal/images/article/4/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(101))
                .andExpect(jsonPath("$.path").value("/article/4/images/101"));
    }

    @Test
    void uploadRejectsUnsupportedMimeType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1}
        );
        when(imagesService.validateAndGetMimeType("application/pdf"))
                .thenThrow(new UnsupportedMediaTypeException("Unsupported MIME type: application/pdf"));

        mockMvc.perform(multipart("/internal/images/user-icon/5").file(file))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void uploadEmptyFilenameDefaultsToUnnamed() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", MediaType.IMAGE_PNG_VALUE, new byte[]{0}
        );
        when(imagesService.validateAndGetMimeType("image/png")).thenReturn(pngMimeType);
        Image saved = Image.builder().id(1L).path("/user_icon/9").filename("unnamed")
                .mimeTypeId(3).content(file.getBytes()).build();
        when(imagesService.replaceImage(eq("/user_icon/9"), eq("unnamed"), eq(pngMimeType), any()))
                .thenReturn(saved);

        mockMvc.perform(multipart("/internal/images/user-icon/9").file(file))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"));

        verify(imagesService).replaceImage(eq("/user_icon/9"), eq("unnamed"), eq(pngMimeType), any());
    }
}
