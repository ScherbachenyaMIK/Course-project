package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record ProfileRequest(
        String username
) implements ScrapperGetRequest {
}
