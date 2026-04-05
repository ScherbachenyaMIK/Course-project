package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ViewRequest(
        Long articleId
) implements ScrapperGetRequest {
}
