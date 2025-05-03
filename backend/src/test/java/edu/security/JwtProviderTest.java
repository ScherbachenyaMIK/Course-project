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
        String token = provider.generateToken("test");
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
        String token = provider.generateToken("test");
        boolean result = provider.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void validateTokenBadToken() {
        String token = "test";
        boolean result = provider.validateToken(token);

        assertThat(result).isFalse();
    }
}