package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ArticleSearchRequest(
        String query,
        Integer minLikes,
        Integer minViews,
        Integer minComments,
        String sort,
        int limit
) implements ScrapperGetRequest {
}
