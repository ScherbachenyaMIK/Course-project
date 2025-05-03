package edu.model.web.response;

import edu.model.web.AuthResponse;

public record CheckAvailabilityResponse(
    boolean usernameAvailable,
    boolean emailAvailable
) implements AuthResponse {
}
