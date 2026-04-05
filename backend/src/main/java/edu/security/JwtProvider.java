package edu.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtProvider {
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_PURPOSE = "purpose";
    public static final String PURPOSE_AUTH = "auth";
    public static final String PURPOSE_EMAIL_CONFIRM = "email_confirm";

    private final SecretKey secretKey;
    private final MacAlgorithm algorithm = Jwts.SIG.HS256;
    private final long expiration;

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_PURPOSE, PURPOSE_AUTH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, algorithm)
                .compact();
    }

    public String generateEmailConfirmationToken(String username, long ttlMillis) {
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_PURPOSE, PURPOSE_EMAIL_CONFIRM)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(secretKey, algorithm)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = parseClaims(token).get(CLAIM_ROLE);
        return role == null ? null : role.toString();
    }

    public String extractPurpose(String token) {
        Object purpose = parseClaims(token).get(CLAIM_PURPOSE);
        return purpose == null ? null : purpose.toString();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
