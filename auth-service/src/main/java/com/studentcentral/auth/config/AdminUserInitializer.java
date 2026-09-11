package com.studentcentral.auth.config;

import com.studentcentral.auth.model.Role;
import com.studentcentral.auth.model.User;
import com.studentcentral.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Safe development seeder for bootstrap ADMIN account.
 * Allows testing ADMIN role capabilities without exposing any public admin creation endpoint.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapEnabled;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap.enabled:true}") boolean bootstrapEnabled,
            @Value("${app.admin.bootstrap.email:admin@studentcentral.com}") String adminEmail,
            @Value("${app.admin.bootstrap.password:Admin@12345}") String adminPassword,
            @Value("${app.admin.bootstrap.name:System Administrator}") String adminName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEnabled = bootstrapEnabled;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapEnabled) {
            log.info("Admin bootstrap is disabled.");
            return;
        }

        try {
            String normalizedEmail = adminEmail.toLowerCase().trim();
            if (!userRepository.existsByEmail(normalizedEmail)) {
                User admin = new User();
                admin.setName(adminName);
                admin.setEmail(normalizedEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                admin.setCreatedAt(Instant.now());
                admin.setUpdatedAt(Instant.now());

                userRepository.save(admin);
                log.info("Bootstrap ADMIN user created successfully: email={}", normalizedEmail);
            } else {
                log.info("Admin user '{}' already exists in database.", normalizedEmail);
            }
        } catch (Exception ex) {
            log.warn("Could not execute admin bootstrap initialization: {}", ex.getMessage());
        }
    }
}
