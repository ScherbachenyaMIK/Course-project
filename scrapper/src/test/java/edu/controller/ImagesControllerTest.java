package edu.controller;

import edu.model.db.entity.Image;
import edu.model.db.entity.MimeType;
import edu.service.ImagesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImagesController.class)
class ImagesControllerTest {
    @MockBean
    private ImagesService imagesService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getImage() throws Exception {
        byte[] imageBytes = new byte[]{1, 2, 3, 4};
        Image image = new Image(
                1L,
                "/preview/1",
                "ImageName",
                3,
                imageBytes
        );
        MimeType mimeType = new MimeType(
                3L,
                "image/png"
        );

        when(imagesService.findImageByPath("/preview/1")).thenReturn(image);
        when(imagesService.findImageType(image)).thenReturn(mimeType.getType());

        mockMvc.perform(get("/images/1")
                        .param("query", "/preview/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Cache-Control", "max-age=604800, public"))
                .andExpect(header().string("Content-Length", String.valueOf(imageBytes.length)))
                .andExpect(content().bytes(imageBytes))
                .andDo(print());
    }

    @Test
    void getImageNotFound() throws Exception {
        when(imagesService.findImageByPath("/preview/1")).thenReturn(null);

        mockMvc.perform(get("/images/1")
                        .param("query", "/preview/"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}