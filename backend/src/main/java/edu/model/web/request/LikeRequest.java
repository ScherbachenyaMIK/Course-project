package edu.model.web.request;

import edu.model.web.ScrapperPostRequest;

public record LikeRequest(
        Long articleId,
        String username
) implements ScrapperPostRequest {
}
