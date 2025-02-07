package edu.model.web.response;

public record AuthResponse(
        boolean success,
        String role
) {
}
