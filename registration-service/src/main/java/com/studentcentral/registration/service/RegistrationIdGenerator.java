package com.studentcentral.registration.service;

import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.repository.RegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistrationIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(RegistrationIdGenerator.class);
    private static final Pattern REG_ID_PATTERN = Pattern.compile("^SC-REG-(\\d{4})-(\\d{5})$");

    private final RegistrationRepository registrationRepository;
    private final AtomicLong sequence = new AtomicLong(0);
    private boolean initialized = false;

    public RegistrationIdGenerator(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public synchronized String generateRegistrationId(Integer year) {
        int targetYear = (year != null && year >= 2000) ? year : Year.now().getValue();

        if (!initialized) {
            initSequence();
            initialized = true;
        }

        while (true) {
            long nextSeq = sequence.incrementAndGet();
            String candidateId = String.format("SC-REG-%d-%05d", targetYear, nextSeq);

            if (registrationRepository.findByRegistrationId(candidateId).isEmpty()) {
                log.debug("Generated unique registration ID: {}", candidateId);
                return candidateId;
            }
            log.warn("Collision detected for registration ID: {}, generating next in sequence", candidateId);
        }
    }

    private void initSequence() {
        Optional<Registration> topRegistration = registrationRepository.findTopByOrderByIdDesc();
        if (topRegistration.isPresent() && topRegistration.get().getRegistrationId() != null) {
            Matcher matcher = REG_ID_PATTERN.matcher(topRegistration.get().getRegistrationId());
            if (matcher.matches()) {
                try {
                    long lastSeq = Long.parseLong(matcher.group(2));
                    sequence.set(lastSeq);
                    log.info("Initialized RegistrationIdGenerator sequence from DB at: {}", lastSeq);
                    return;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        sequence.set(0);
        log.info("Initialized RegistrationIdGenerator sequence at 0");
    }
}
