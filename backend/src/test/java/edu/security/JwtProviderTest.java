package edu.security;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {
    private static final JwtProvider provider = new JwtProvider(
            Keys.hmacShaKeyFor(
                    "TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest".getBytes()
            ),
            2000
    ) ;

    @Test
    void extractUsername() {
        String token = provider.generateToken("test", "USER");
        String result = provider.extractUsername(token);

        assertThat(result).isEqualTo("test");
    }

    @Test
    void extractUsernameBadToken() {
        String token = "test";

        assertThatThrownBy(() -> provider.extractUsername(token))
                .isExactlyInstanceOf(MalformedJwtException.class);
    }

    @Test
    void validateToken() {
        String token = provider.generateToken("test", "USER");
        boolean result = provider.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void validateTokenBadToken() {
        String token = "test";
        boolean result = provider.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void extractRoleFromAuthToken() {
        String token = provider.generateToken("test", "ADMIN");

        assertThat(provider.extractRole(token)).isEqualTo("ADMIN");
        assertThat(provider.extractPurpose(token)).isEqualTo(JwtProvider.PURPOSE_AUTH);
    }

    @Test
    void extractRoleReturnsNullForConfirmationToken() {
        String token = provider.generateEmailConfirmationToken("test", 60_000);

        assertThat(provider.extractRole(token)).isNull();
        assertThat(provider.extractPurpose(token)).isEqualTo(JwtProvider.PURPOSE_EMAIL_CONFIRM);
        assertThat(provider.extractUsername(token)).isEqualTo("test");
    }

    @Test
    void generateEmailConfirmationTokenValidates() {
        String token = provider.generateEmailConfirmationToken("alice", 60_000);

        assertThat(provider.validateToken(token)).isTrue();
    }
}