package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ArticleSetupRequest(
        Long authorId,
        String title
) implements ScrapperGetRequest {
}
