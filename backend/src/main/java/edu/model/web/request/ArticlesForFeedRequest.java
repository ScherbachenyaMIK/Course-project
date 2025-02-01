package edu.model.web.request;

import edu.model.web.ScrapperRequest;

public record ArticlesForFeedRequest(
        int count
) implements ScrapperRequest {
}
