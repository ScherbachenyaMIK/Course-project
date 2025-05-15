package edu.model.web.dto;

import edu.model.web.DTO;
import java.util.List;

public record ArticleFeedDTO(
        List<ArticlePreviewDTO> articlePreviewDTOList
) implements DTO {
}
