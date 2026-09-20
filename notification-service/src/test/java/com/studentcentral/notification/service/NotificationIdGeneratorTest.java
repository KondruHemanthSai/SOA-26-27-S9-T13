package com.studentcentral.notification.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationIdGeneratorTest {

    private final NotificationIdGenerator generator = new NotificationIdGenerator();

    @Test
    void shouldGenerateIdWithCorrectFormat() {
        String id = generator.generateNotificationId();

        assertNotNull(id);
        assertTrue(id.startsWith("NOTIF-"), "ID should start with NOTIF-");
        assertEquals(14, id.length(), "ID should be 14 characters: NOTIF- (6) + 8 hex chars");
    }

    @Test
    void shouldGenerateUppercaseHexChars() {
        String id = generator.generateNotificationId();
        String hexPart = id.substring(6);

        assertTrue(hexPart.matches("[0-9A-F]{8}"),
                "Hex part should contain only uppercase hex characters: " + hexPart);
    }

    @Test
    void shouldGenerateUniqueIds() {
        String id1 = generator.generateNotificationId();
        String id2 = generator.generateNotificationId();
        String id3 = generator.generateNotificationId();

        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }

    @Test
    void shouldGenerateManyUniqueIds() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(generator.generateNotificationId());
        }
        assertEquals(1000, ids.size(), "All 1000 generated IDs should be unique");
    }
}
