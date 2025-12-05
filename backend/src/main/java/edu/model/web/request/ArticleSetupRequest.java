package edu.model.web.request;

import edu.model.web.ScrapperPostRequest;

public record ArticleSetupRequest(
        Long authorId,
        String title
) implements ScrapperPostRequest {
}
