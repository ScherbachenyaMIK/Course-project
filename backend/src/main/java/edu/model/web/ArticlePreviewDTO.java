package edu.model.web;

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
