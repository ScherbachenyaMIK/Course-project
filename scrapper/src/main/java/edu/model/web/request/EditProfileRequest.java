package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;

public record EditProfileRequest(
        String username,
        String nativeName,
        String description,
        Character sex,
        String birthDate
) implements ScrapperGetRequest {
}
