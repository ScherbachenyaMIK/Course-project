package edu.model.web.request;

import edu.model.web.ScrapperPostRequest;

public record ViewRequest(
        Long articleId
) implements ScrapperPostRequest {
}
