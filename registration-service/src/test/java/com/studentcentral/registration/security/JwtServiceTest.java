package com.studentcentral.registration.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "testSecretKeyWithSufficientLengthForHMACSHA256SecurityValidation!";
    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldExtractClaimsFromValidToken() {
        String token = Jwts.builder()
                .subject("student@example.com")
                .claims(Map.of(
                        "userId", "user-123",
                        "role", "STUDENT"
                ))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("student@example.com", jwtService.extractUsername(token));
        assertEquals("user-123", jwtService.extractUserId(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("student@example.com")
                .claims(Map.of("userId", "user-123", "role", "STUDENT"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(signingKey)
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken));
    }

    @Test
    void shouldRejectTamperedToken() {
        SecretKey otherKey = Keys.hmacShaKeyFor("differentSecretKeyWithSufficientLengthForHS256TestingOnly123!".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = Jwts.builder()
                .subject("student@example.com")
                .claims(Map.of("userId", "user-123", "role", "STUDENT"))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(otherKey)
                .compact();

        assertFalse(jwtService.isTokenValid(tamperedToken));
    }
}
