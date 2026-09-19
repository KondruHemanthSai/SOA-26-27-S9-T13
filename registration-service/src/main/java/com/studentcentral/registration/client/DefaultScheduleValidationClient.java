package com.studentcentral.registration.client;

import com.studentcentral.registration.client.dto.ScheduleConflictResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback / Stub implementation for Schedule Validation.
 * Used when standalone mock execution is preferred.
 */
@Component
public class DefaultScheduleValidationClient implements ScheduleValidationClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultScheduleValidationClient.class);

    @Override
    public ScheduleConflictResult checkScheduleConflict(String studentId, String courseId, Integer semester, String jwtToken) {
        log.debug("DefaultScheduleValidationClient: Checking schedule conflict for student {} and course {} (semester {})",
                studentId, courseId, semester);
        return new ScheduleConflictResult(false, null);
    }
}
