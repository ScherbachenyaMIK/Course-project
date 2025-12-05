package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record AIRequest(
        String prompt,
        String requestType
) implements ScrapperGetRequest {
}
