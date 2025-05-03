package edu.model.web.response;

import edu.model.web.AuthResponse;

public record LoginResponse(
        boolean success,
        String role
) implements AuthResponse {
}
