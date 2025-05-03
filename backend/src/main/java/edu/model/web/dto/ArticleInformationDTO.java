package edu.model.web.dto;

public record ArticleInformationDTO(
        String tags,
        String categories,
        int timeToRead,
        String creationDate,
        String status,
        int views,
        int likes,
        int comments
) {
}
