package edu.model.web.dto;

import java.net.URI;

public record ArticlePreviewDTO(
        URI authorIconUri,
        String author,
        String title,
        ArticleInformationDTO information,
        URI previewImageUri,
        String fragment,
        URI articleUri
) {
}
