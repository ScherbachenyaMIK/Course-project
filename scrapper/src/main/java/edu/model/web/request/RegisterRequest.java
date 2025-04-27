package edu.model.web.request;

import edu.model.web.AuthRequest;
import java.time.LocalDate;

public record RegisterRequest(
    String username,
    String name,
    String email,
    String passwordHash,
    Character sex,
    LocalDate date
) implements AuthRequest {
}
