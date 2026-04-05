package edu.model.web.response;

import edu.model.web.AuthResponse;

public record ConfirmEmailResponse(
        boolean success
) implements AuthResponse {
}
