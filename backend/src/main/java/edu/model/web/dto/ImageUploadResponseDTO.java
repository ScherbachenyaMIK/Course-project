package edu.model.web.dto;

public record ImageUploadResponseDTO(
        Long imageId,
        String path,
        String mimeType,
        String filename
) {
}
