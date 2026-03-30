package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ArticleRequest(
        Long id,
        int commentCount,
        String currentUsername
) implements ScrapperGetRequest {
}
