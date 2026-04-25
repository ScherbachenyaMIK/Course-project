package edu.controller;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.db.entity.Image;
import edu.model.db.entity.MimeType;
import edu.model.web.response.ImageUploadResponse;
import edu.service.ImageProcessingService;
import edu.service.ImagesService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/internal/images")
public class InternalImagesController {
    @Autowired
    private ImagesService imagesService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    @PostMapping("/user-icon/{userId}")
    public ResponseEntity<ImageUploadResponse> uploadUserIcon(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Image saved = saveOrReplace("/user_icon/" + userId, file, true);
        triggerProcessing(saved, file.getContentType());
        return ResponseEntity.ok(toResponse(saved, file.getContentType()));
    }

    @PostMapping("/preview/{articleId}")
    public ResponseEntity<ImageUploadResponse> uploadPreview(
            @PathVariable Long articleId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Image saved = saveOrReplace("/preview/" + articleId, file, true);
        triggerProcessing(saved, file.getContentType());
        return ResponseEntity.ok(toResponse(saved, file.getContentType()));
    }

    @PostMapping("/article/{articleId}/images")
    public ResponseEntity<ImageUploadResponse> uploadArticleImage(
            @PathVariable Long articleId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        MimeType mimeType = imagesService.validateAndGetMimeType(file.getContentType());
        String filename = resolveFilename(file);
        Image saved = imagesService.saveArticleImage(articleId, filename, mimeType, file.getBytes());
        triggerProcessing(saved, file.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(saved, file.getContentType()));
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<String> handleUnsupportedMediaType(UnsupportedMediaTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ex.getMessage());
    }

    private Image saveOrReplace(String path, MultipartFile file, boolean replace) throws IOException {
        MimeType mimeType = imagesService.validateAndGetMimeType(file.getContentType());
        String filename = resolveFilename(file);
        if (replace) {
            return imagesService.replaceImage(path, filename, mimeType, file.getBytes());
        }
        return imagesService.saveImage(path, filename, mimeType, file.getBytes());
    }

    private void triggerProcessing(Image image, String mimeType) {
        imageProcessingService.processAsync(image.getId(), image.getPath(), mimeType);
    }

    private ImageUploadResponse toResponse(Image image, String mimeType) {
        return new ImageUploadResponse(
                image.getId(),
                image.getPath(),
                mimeType,
                image.getFilename()
        );
    }

    private String resolveFilename(MultipartFile file) {
        return file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename()
                : "unnamed";
    }
}
