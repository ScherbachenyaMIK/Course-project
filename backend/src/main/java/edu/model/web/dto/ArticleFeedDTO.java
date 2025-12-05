package edu.model.web.dto;

import java.util.List;

public record ArticleFeedDTO(
        List<ArticlePreviewDTO> articlePreviewDTOList
) {
}
