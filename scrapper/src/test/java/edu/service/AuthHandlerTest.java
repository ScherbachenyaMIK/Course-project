package edu.service;

import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.model.web.response.LoginResponse;
import edu.model.web.response.RegisterResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthHandlerTest {
    @Mock
    private UsersService service;
    @InjectMocks
    private AuthHandler authHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleEmailWithCorrectPassword() {
        LoginRequest request =
                new LoginRequest(
                        "test@mail.ru",
                        "VerySecretPassword"
                );
        LoginResponse response =
                new LoginResponse(
                        true,
                        "USER"
                );

        when(service.checkAuthAndRoleByEmail(any(), any()))
                .thenReturn("USER");

        LoginResponse result = authHandler.handleLogin(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleUsernameWithCorrectPassword() {
        LoginRequest request =
                new LoginRequest(
                        "testUser",
                        "VerySecretPassword"
                );
        LoginResponse response =
                new LoginResponse(
                        true,
                        "USER"
                );

        when(service.checkAuthAndRoleByUsername(any(), any()))
                .thenReturn("USER");

        LoginResponse result = authHandler.handleLogin(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleEmailWithIncorrectPassword() {
        LoginRequest request =
                new LoginRequest(
                        "test@mail.ru",
                        "VerySecretPassword"
                );
        LoginResponse response =
                new LoginResponse(
                        false,
                        "NONE"
                );

        when(service.checkAuthAndRoleByEmail(any(), any()))
                .thenReturn("NONE");

        LoginResponse result = authHandler.handleLogin(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleNotConfirmed() {
        LoginRequest request =
                new LoginRequest(
                        "testUser",
                        "VerySecretPassword"
                );
        LoginResponse response =
                new LoginResponse(
                        false,
                        "NONE"
                );

        when(service.checkAuthAndRoleByUsername(any(), any()))
                .thenReturn("NOT_CONFIRMED");

        LoginResponse result = authHandler.handleLogin(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleAvailability() {
        CheckAvailabilityRequest request =
                new CheckAvailabilityRequest(
                        "test",
                        "test@mail.ru"
                );
        CheckAvailabilityResponse response =
                new CheckAvailabilityResponse(
                        false,
                        true
                );

        when(service.isExistsByUsername(anyString()))
                .thenReturn(true);
        when(service.isExistsByEmail(anyString()))
                .thenReturn(false);

        CheckAvailabilityResponse result = authHandler.handleAvailability(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleRegister() {
        RegisterRequest request =
                new RegisterRequest(
                        "test",
                        "test",
                        "test@mail.ru",
                        "VerySecretPassword",
                        'M',
                        LocalDate.now()
                );
        RegisterResponse response =
                new RegisterResponse(
                        true
                );

        when(service.registerNewUser(any()))
                .thenReturn(true);

        RegisterResponse result = authHandler.handleRegister(request);

        assertThat(result).isEqualTo(response);
    }
}