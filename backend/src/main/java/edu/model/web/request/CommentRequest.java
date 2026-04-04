package edu.model.web.request;

import edu.model.web.ScrapperPostRequest;

public record CommentRequest(
        Long articleId,
        String username,
        String text
) implements ScrapperPostRequest {
}
