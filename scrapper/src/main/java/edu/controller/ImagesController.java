package edu.controller;

import edu.model.db.entity.Image;
import edu.service.ImagesService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
public class ImagesController {
    @Autowired
    private ImagesService imagesService;

    @SuppressWarnings("MagicNumber")
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id,
            @RequestParam String query
    ) {
        String fullPath = (query != null ? query : "") + id;

        Image image = imagesService.findImageByPath(fullPath);

        if (image != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagesService.findImageType(image)))
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                    .body(image.getContent());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
