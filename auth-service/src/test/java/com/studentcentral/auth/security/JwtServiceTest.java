package com.studentcentral.auth.security;

import com.studentcentral.auth.exception.InvalidTokenException;
import com.studentcentral.auth.exception.TokenExpiredException;
import com.studentcentral.auth.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "testSecretKeyForStudentCentralApplicationWith256BitsMinimumLength!";
    private static final long EXPIRATION_MILLIS = 3600000; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MILLIS);
    }

    @Test
    void shouldGenerateValidTokenAndExtractClaims() {
        String userId = "user123";
        String email = "student@example.com";
        Role role = Role.STUDENT;

        String token = jwtService.generateToken(userId, email, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(role, jwtService.extractRole(token));
        assertTrue(jwtService.validateToken(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void shouldGenerateAdminTokenAndExtractRole() {
        String userId = "admin999";
        String email = "admin@studentcentral.com";
        Role role = Role.ADMIN;

        String token = jwtService.generateToken(userId, email, role);

        assertEquals(Role.ADMIN, jwtService.extractRole(token));
        assertEquals("admin999", jwtService.extractUserId(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        // Create JwtService with 0 ms expiration
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, -1000);
        String expiredToken = shortLivedJwtService.generateToken("user123", "student@example.com", Role.STUDENT);

        assertFalse(shortLivedJwtService.validateToken(expiredToken));
        assertTrue(shortLivedJwtService.isTokenExpired(expiredToken));
        assertThrows(TokenExpiredException.class, () -> shortLivedJwtService.extractAllClaims(expiredToken));
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        String token = jwtService.generateToken("user123", "student@example.com", Role.STUDENT);

        JwtService differentSecretJwtService = new JwtService(
                "completelyDifferentSecretKeyForSigningVerificationTesting256Bits!",
                EXPIRATION_MILLIS
        );

        assertFalse(differentSecretJwtService.validateToken(token));
        assertThrows(InvalidTokenException.class, () -> differentSecretJwtService.extractAllClaims(token));
    }

    @Test
    void shouldRejectMalformedToken() {
        String malformedToken = "not.a.valid.jwt.token";

        assertFalse(jwtService.validateToken(malformedToken));
        assertThrows(InvalidTokenException.class, () -> jwtService.extractAllClaims(malformedToken));
    }
}
