package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ArticleEditRequest(
        Long articleId,
        String username,
        String title,
        String content,
        String tags,
        String categories,
        String status,
        Integer timeToRead
) implements ScrapperGetRequest {
}
