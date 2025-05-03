package edu.model.web.response;

import edu.model.web.AuthResponse;

public record RegisterResponse(
        boolean success
) implements AuthResponse {
}
