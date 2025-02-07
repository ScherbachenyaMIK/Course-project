package edu.security;

import edu.controller.AuthorizationListener;
import edu.model.web.request.AuthRequest;
import edu.model.web.response.AuthResponse;
import edu.web.ScrapperProducer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class CustomAuthenticationManager implements AuthenticationManager {
    @Autowired
    ScrapperProducer scrapperProducer;
    @Autowired
    AuthorizationListener authorizationListener;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendAuthRequest(correlationId, new AuthRequest(username, password));

        try {
            AuthResponse response = authorizationListener.waitForResponse(correlationId);

            if (response.success()) {
                return new UsernamePasswordAuthenticationToken(username, password, List.of());
            } else {
                throw new BadCredentialsException("Invalid credentials");
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AuthenticationServiceException("Authentication service unavailable", e);
        }
    }
}
