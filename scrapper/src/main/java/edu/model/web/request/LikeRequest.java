package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record LikeRequest(
        Long articleId,
        String username
) implements ScrapperGetRequest {
}
