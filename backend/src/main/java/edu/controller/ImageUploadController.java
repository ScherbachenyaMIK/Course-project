package edu.controller;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.web.dto.ImageUploadResponseDTO;
import edu.service.ImageStorageService;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperUploadException;
import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/resources")
public class ImageUploadController {
    private static final String ERROR_KEY = "error";

    @Autowired
    private ImageStorageService imageStorageService;

    @PostMapping("/user_icon/{userId}")
    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadUserIcon(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        requireAuthenticated();
        return imageStorageService.uploadUserIcon(userId, file);
    }

    @PostMapping("/preview/{articleId}")
    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadPreview(
            @PathVariable Long articleId,
            @RequestParam("file") MultipartFile file
    ) {
        requireAuthenticated();
        return imageStorageService.uploadPreview(articleId, file);
    }

    @PostMapping("/article/{articleId}/images")
    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadArticleImage(
            @PathVariable Long articleId,
            @RequestParam("file") MultipartFile file
    ) {
        requireAuthenticated();
        return imageStorageService.uploadArticleImage(articleId, file)
                .map(resp -> {
                    ImageUploadResponseDTO body = resp.getBody();
                    if (body == null) {
                        return ResponseEntity.status(HttpStatus.CREATED).<ImageUploadResponseDTO>build();
                    }
                    URI location = URI.create(
                            "/resources/article/" + articleId + "/images/" + body.imageId()
                    );
                    return ResponseEntity.created(location).body(body);
                });
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedMediaType(UnsupportedMediaTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(ScrapperUploadException.class)
    public ResponseEntity<Map<String, String>> handleScrapperFailure(ScrapperUploadException ex) {
        HttpStatus status = ex.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
                ? HttpStatus.UNSUPPORTED_MEDIA_TYPE
                : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    private void requireAuthenticated() {
        if (!AuthenticationChecker.checkAuthorities()) {
            throw new AccessDeniedException("Authentication required to upload images");
        }
    }
}
