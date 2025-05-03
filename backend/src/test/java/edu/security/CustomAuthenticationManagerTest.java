package edu.security;

import edu.controller.AuthorizationListener;
import edu.model.web.response.LoginResponse;
import edu.web.ScrapperProducer;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CustomAuthenticationManagerTest {
    @Mock
    private ScrapperProducer scrapperProducer;
    @Mock
    private AuthorizationListener authorizationListener;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomAuthenticationManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SneakyThrows
    @Test
    void authenticate() {
        LoginResponse response = new LoginResponse(
                true,
                "User"
        );
        Authentication expected = new UsernamePasswordAuthenticationToken(
                "test",
                "test",
                List.of()
        );

        when(authentication.getName()).thenReturn("test");
        when(authentication.getCredentials()).thenReturn("test");
        when(authorizationListener.waitForResponse(anyString()))
                .thenReturn(response);

        Authentication result = manager.authenticate(authentication);

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void authenticateBadCredentials() {
        LoginResponse response = new LoginResponse(
                false,
                "User"
        );

        when(authentication.getName()).thenReturn("test");
        when(authentication.getCredentials()).thenReturn("test");
        when(authorizationListener.waitForResponse(anyString()))
                .thenReturn(response);

        assertThatThrownBy(() -> manager.authenticate(authentication))
                .isExactlyInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @SneakyThrows
    @Test
    void authenticateServiceUnavailable() {
        when(authentication.getName()).thenReturn("test");
        when(authentication.getCredentials()).thenReturn("test");
        when(authorizationListener.waitForResponse(anyString()))
                .thenThrow(new InterruptedException());

        assertThatThrownBy(() -> manager.authenticate(authentication))
                .isExactlyInstanceOf(AuthenticationServiceException.class)
                .hasCauseExactlyInstanceOf(InterruptedException.class)
                .hasMessage("Authentication service unavailable");
    }
}