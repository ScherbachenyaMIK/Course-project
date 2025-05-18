package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ArticlesForFeedRequest(
        int count
) implements ScrapperGetRequest {
}
