package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record CommentRequest(
        Long articleId,
        String username,
        String text
) implements ScrapperGetRequest {
}
