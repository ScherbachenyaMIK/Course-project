package edu.model.web.response;

public record ImageUploadResponse(
        Long imageId,
        String path,
        String mimeType,
        String filename
) {
}
