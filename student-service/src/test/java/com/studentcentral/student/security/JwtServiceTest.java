package com.studentcentral.student.security;

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

    private static final String TEST_SECRET = "testSecretKeyForStudentCentralApplicationWith256BitsMinimumLength!";
    private JwtService jwtService;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
        secretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(String userId, String email, String role, long expirationOffsetMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationOffsetMs);
        return Jwts.builder()
                .claims(Map.of("email", email, "role", role))
                .subject(userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    @Test
    void shouldExtractClaimsFromValidToken() {
        String token = generateToken("user-101", "student@example.com", "STUDENT", 3600000);

        assertTrue(jwtService.validateToken(token));
        assertEquals("user-101", jwtService.extractUserId(token));
        assertEquals("student@example.com", jwtService.extractEmail(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = generateToken("user-101", "student@example.com", "STUDENT", -5000);

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        SecretKey otherKey = Keys.hmacShaKeyFor("differentSecretKeyForSigningVerificationTesting256Bits!".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = Jwts.builder()
                .subject("user-101")
                .claim("role", "STUDENT")
                .claim("email", "student@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.validateToken(tamperedToken));
    }
}
