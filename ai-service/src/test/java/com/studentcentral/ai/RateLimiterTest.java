package com.studentcentral.ai;

import com.studentcentral.ai.security.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(5); // 5 requests max for test
    }

    @Test
    void allowsRequestsUnderLimit() {
        String user = "user-101";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.isAllowed(user), "Request " + i + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsOverLimit() {
        String user = "user-102";
        for (int i = 0; i < 5; i++) {
            rateLimiterService.isAllowed(user);
        }
        assertFalse(rateLimiterService.isAllowed(user), "6th request should be rejected");
    }

    @Test
    void tracksRemainingRequestsAccurately() {
        String user = "user-103";
        assertEquals(5, rateLimiterService.getRemainingRequests(user));
        rateLimiterService.isAllowed(user);
        rateLimiterService.isAllowed(user);
        assertEquals(3, rateLimiterService.getRemainingRequests(user));
    }

    @Test
    void isolatesUsersFromEachOther() {
        String userA = "user-a";
        String userB = "user-b";

        for (int i = 0; i < 5; i++) {
            rateLimiterService.isAllowed(userA);
        }
        assertFalse(rateLimiterService.isAllowed(userA));
        assertTrue(rateLimiterService.isAllowed(userB), "User B should not be affected by User A");
    }
}
