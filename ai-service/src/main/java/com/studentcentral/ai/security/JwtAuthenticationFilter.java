package com.studentcentral.ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String gatewayUserId = request.getHeader("X-User-Id");
        String gatewayUserRole = request.getHeader("X-User-Role");

        String userId = null;
        String email = null;
        String role = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isTokenValid(token)) {
                userId = jwtService.extractUserId(token);
                email = jwtService.extractEmail(token);
                role = jwtService.extractRole(token);
            }
        } else if (gatewayUserId != null && !gatewayUserId.isBlank()) {
            userId = gatewayUserId;
            email = gatewayUserId;
            role = gatewayUserRole != null ? gatewayUserRole : "STUDENT";
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, email, role);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    authenticatedUser, null, authenticatedUser.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
