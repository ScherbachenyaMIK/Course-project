package edu.util;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationCheckerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsernameReturnsNullWhenContextEmpty() {
        SecurityContextHolder.clearContext();

        assertThat(AuthenticationChecker.getCurrentUsername()).isNull();
        assertThat(AuthenticationChecker.checkAuthorities()).isFalse();
    }

    @Test
    void getCurrentUsernameReturnsNullForAnonymousToken() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThat(AuthenticationChecker.getCurrentUsername()).isNull();
        assertThat(AuthenticationChecker.checkAuthorities()).isFalse();
    }

    @Test
    void getCurrentUsernameReturnsNameWhenAuthenticated() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "alice", "pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(AuthenticationChecker.getCurrentUsername()).isEqualTo("alice");
        assertThat(AuthenticationChecker.checkAuthorities()).isTrue();
    }

    @Test
    void hasRoleReturnsTrueWhenAuthorityPresent() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "bob", "pw",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(AuthenticationChecker.hasRole("ADMIN")).isTrue();
        assertThat(AuthenticationChecker.hasRole("USER")).isFalse();
    }

    @Test
    void hasRoleReturnsFalseWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThat(AuthenticationChecker.hasRole("USER")).isFalse();
    }

    @Test
    void hasRoleReturnsFalseForAnonymous() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThat(AuthenticationChecker.hasRole("ANONYMOUS")).isFalse();
    }
}
