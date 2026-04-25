package edu.service;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.web.dto.ImageUploadResponseDTO;
import edu.web.ScrapperClient;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
public class ImageStorageService {
    /**
     * MIME types that the scrapper {@code mime_types} table is seeded with
     * (see migrations/13-create-table-Images.sql). Kept as a fast pre-check
     * at the API boundary; the scrapper performs the authoritative validation
     * against the database before persisting.
     */
    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/pjpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
            "image/x-windows-bmp",
            "image/vnd.microsoft.icon",
            "image/x-icon",
            "image/svg+xml",
            "image/heic",
            "image/heif",
            "image/tiff",
            "image/avif"
    );

    @Autowired
    private ScrapperClient scrapperClient;

    public String validateAndGetMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UnsupportedMediaTypeException("Uploaded file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new UnsupportedMediaTypeException("MIME type is missing");
        }
        String normalized = contentType.toLowerCase();
        if (!SUPPORTED_MIME_TYPES.contains(normalized)) {
            throw new UnsupportedMediaTypeException("Unsupported MIME type: " + contentType);
        }
        return normalized;
    }

    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadUserIcon(Long userId, MultipartFile file) {
        return forward(file, (bytes, name, type) ->
                scrapperClient.uploadUserIcon(userId, bytes, name, type));
    }

    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadPreview(Long articleId, MultipartFile file) {
        return forward(file, (bytes, name, type) ->
                scrapperClient.uploadPreview(articleId, bytes, name, type));
    }

    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadArticleImage(Long articleId, MultipartFile file) {
        return forward(file, (bytes, name, type) ->
                scrapperClient.uploadArticleImage(articleId, bytes, name, type));
    }

    private Mono<ResponseEntity<ImageUploadResponseDTO>> forward(MultipartFile file, UploadCall call) {
        String contentType = validateAndGetMimeType(file);
        String filename = file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename()
                : "unnamed";
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException ex) {
            return Mono.error(new UnsupportedMediaTypeException("Failed to read upload: " + ex.getMessage()));
        }
        return call.invoke(content, filename, contentType);
    }

    @FunctionalInterface
    private interface UploadCall {
        Mono<ResponseEntity<ImageUploadResponseDTO>> invoke(byte[] bytes, String filename, String contentType);
    }
}
