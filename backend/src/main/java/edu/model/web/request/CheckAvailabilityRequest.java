package edu.model.web.request;

import edu.model.web.AuthRequest;

public record CheckAvailabilityRequest(
    String username,
    String email
) implements AuthRequest {
}
