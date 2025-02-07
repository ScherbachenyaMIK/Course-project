package edu.model.web.request;

public record AuthRequest(
        String username,
        String password
) {
}
