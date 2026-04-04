package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ArticleSetupRequest(
        String username,
        String title,
        String content,
        String tags,
        String categories
) implements ScrapperGetRequest {
}
