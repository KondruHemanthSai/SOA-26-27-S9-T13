package com.studentcentral.ai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final int maxRequestsPerHour;
    private final Map<String, List<Long>> requestLog = new ConcurrentHashMap<>();

    public RateLimiterService(@Value("${ai.rate-limit.requests-per-hour:20}") int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    /**
     * Checks if a request from userId is allowed.
     * If allowed, records the timestamp and returns true.
     * If exceeded, returns false.
     */
    public synchronized boolean isAllowed(String userId) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (60 * 60 * 1000L); // 1 hour ago

        List<Long> timestamps = requestLog.computeIfAbsent(userId, k -> new ArrayList<>());

        // Purge timestamps older than 1 hour
        timestamps.removeIf(t -> t < windowStart);

        if (timestamps.size() >= maxRequestsPerHour) {
            return false;
        }

        timestamps.add(now);
        return true;
    }

    public int getRemainingRequests(String userId) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (60 * 60 * 1000L);
        List<Long> timestamps = requestLog.getOrDefault(userId, List.of());
        long validCount = timestamps.stream().filter(t -> t >= windowStart).count();
        return Math.max(0, maxRequestsPerHour - (int) validCount);
    }
}
