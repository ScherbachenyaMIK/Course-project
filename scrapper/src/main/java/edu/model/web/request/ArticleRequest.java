package edu.model.web.request;

import edu.model.web.ScrapperRequest;

public record ArticleRequest(
        Long id,
        int commentCount
) implements ScrapperRequest {
}
