package edu.model;

public record ArticlePreviewDTO(
        String author,
        String title,
        String information,
        String previewImageUrl,
        String fragment,
        String articleUrl
) {
}
