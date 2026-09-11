package com.studentcentral.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationGatewayFilterTest {

    private static final String TEST_SECRET = "testSecretKeyForStudentCentralApplicationWith256BitsMinimumLength!";
    private JwtAuthenticationGatewayFilter filter;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationGatewayFilter(TEST_SECRET);
        secretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generateTestToken(String userId, String email, String role, long expirationOffsetMs) {
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
    void shouldAllowPublicEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/register").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] chainExecuted = {false};
        GatewayFilterChain chain = ex -> {
            chainExecuted[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted[0]);
    }

    @Test
    void shouldAllowCorsPreflightOptionsRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/students/123").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] chainExecuted = {false};
        GatewayFilterChain chain = ex -> {
            chainExecuted[0] = true;
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted[0]);
    }

    @Test
    void shouldRejectProtectedEndpointWhenTokenMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/students/profile").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectProtectedEndpointWhenTokenExpired() {
        String expiredToken = generateTestToken("user123", "student@example.com", "STUDENT", -10000);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/students/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectProtectedEndpointWhenTokenSignatureIsInvalid() {
        SecretKey otherKey = Keys.hmacShaKeyFor("differentSecretKeyForSigningVerificationTesting256Bits!".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = Jwts.builder()
                .subject("user123")
                .claim("role", "STUDENT")
                .claim("email", "student@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/students/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamperedToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldPropagateIdentityHeadersOnValidToken() {
        String validToken = generateTestToken("user-456", "jane@example.com", "ADMIN", 3600000);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/courses/manage")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        boolean[] chainExecuted = {false};
        GatewayFilterChain chain = mutatedExchange -> {
            chainExecuted[0] = true;
            String forwardedUserId = mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id");
            String forwardedUserRole = mutatedExchange.getRequest().getHeaders().getFirst("X-User-Role");

            assertEquals("user-456", forwardedUserId);
            assertEquals("ADMIN", forwardedUserRole);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted[0]);
    }
}
