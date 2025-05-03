package edu.service;

import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.model.web.response.LoginResponse;
import edu.model.web.response.RegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthHandler {
    static private final String NONE_ROLE = "NONE";
    static private final String NOT_CONFIRMED = "NOT_CONFIRMED";

    @Autowired
    private UsersService service;

    public LoginResponse handleLogin(LoginRequest request) {
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
            return new LoginResponse(false, NONE_ROLE);
        }
        return new LoginResponse(true, role);
    }

    public CheckAvailabilityResponse handleAvailability(CheckAvailabilityRequest request) {
        return new CheckAvailabilityResponse(
                !service.isExistsByUsername(request.username()),
                !service.isExistsByEmail(request.email())
        );
    }

    public RegisterResponse handleRegister(RegisterRequest request) {
        return new RegisterResponse(service.registerNewUser(request));
    }
}
