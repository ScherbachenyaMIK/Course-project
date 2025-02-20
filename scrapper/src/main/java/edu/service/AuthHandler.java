package edu.service;

import edu.model.web.request.AuthRequest;
import edu.model.web.response.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthHandler {
    static private final String NONE_ROLE = "NONE";
    static private final String NOT_CONFIRMED = "NOT_CONFIRMED";

    @Autowired
    private UsersService service;

    public AuthResponse handle(AuthRequest request) {
        String role;
        if (request.username().contains("@")) {
            role = service.checkAuthAndRoleByEmail(
                    request.username(),
                    request.password()
            );
        } else {
            role = service.checkAuthAndRoleByUsername(
                    request.username(),
                    request.password()
            );
        }
        if (role.equals(NONE_ROLE) || role.equals(NOT_CONFIRMED)) {
            return new AuthResponse(false, NONE_ROLE);
        }
        return new AuthResponse(true, role);
    }
}
