package edu.model.web.request;

import edu.model.web.ScrapperPostRequest;

public record ArticleSetupRequest(
        String username,
        String title,
        String content,
        String tags,
        String categories
) implements ScrapperPostRequest {
}
