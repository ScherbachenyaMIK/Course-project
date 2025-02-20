package edu.service;

import edu.model.web.request.AuthRequest;
import edu.model.web.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthHandlerTest {
    @Mock
    UsersService service;
    @InjectMocks
    private AuthHandler authHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleEmailWithCorrectPassword() {
        AuthRequest request =
                new AuthRequest(
                        "test@mail.ru",
                        "VerySecretPassword"
                );
        AuthResponse response =
                new AuthResponse(
                        true,
                        "USER"
                );

        when(service.checkAuthAndRoleByEmail(any(), any()))
                .thenReturn("USER");

        AuthResponse result = authHandler.handle(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleUsernameWithCorrectPassword() {
        AuthRequest request =
                new AuthRequest(
                        "testUser",
                        "VerySecretPassword"
                );
        AuthResponse response =
                new AuthResponse(
                        true,
                        "USER"
                );

        when(service.checkAuthAndRoleByUsername(any(), any()))
                .thenReturn("USER");

        AuthResponse result = authHandler.handle(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleEmailWithIncorrectPassword() {
        AuthRequest request =
                new AuthRequest(
                        "test@mail.ru",
                        "VerySecretPassword"
                );
        AuthResponse response =
                new AuthResponse(
                        false,
                        "NONE"
                );

        when(service.checkAuthAndRoleByEmail(any(), any()))
                .thenReturn("NONE");

        AuthResponse result = authHandler.handle(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleNotConfirmed() {
        AuthRequest request =
                new AuthRequest(
                        "testUser",
                        "VerySecretPassword"
                );
        AuthResponse response =
                new AuthResponse(
                        false,
                        "NONE"
                );

        when(service.checkAuthAndRoleByUsername(any(), any()))
                .thenReturn("NOT_CONFIRMED");

        AuthResponse result = authHandler.handle(request);

        assertThat(result).isEqualTo(response);
    }
}