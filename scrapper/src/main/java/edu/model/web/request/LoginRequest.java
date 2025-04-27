package edu.model.web.request;

import edu.model.web.AuthRequest;

public record LoginRequest(
        String username,
        String password
) implements AuthRequest {
}
