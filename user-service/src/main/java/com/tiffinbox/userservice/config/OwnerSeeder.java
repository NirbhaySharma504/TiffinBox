package com.tiffinbox.userservice.config;

import com.tiffinbox.userservice.entity.Role;
import com.tiffinbox.userservice.entity.User;
import com.tiffinbox.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Provisions the owner (admin) account on startup if none exists. Owners are never
 * created through the public register endpoint — they are seeded here, with credentials
 * supplied via env vars (OWNER_EMAIL / OWNER_PASSWORD). Idempotent: does nothing if an
 * OWNER already exists, so it is safe to run on every boot.
 */
@Component
@RequiredArgsConstructor
public class OwnerSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OwnerSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${owner.seed.name:Tiffin Owner}")
    private String ownerName;

    @Value("${owner.seed.email:owner@tiffinbox.com}")
    private String ownerEmail;

    @Value("${owner.seed.password:owner123}")
    private String ownerPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.OWNER)) {
            return; // an owner already exists — nothing to do
        }
        User owner = User.builder()
                .name(ownerName)
                .email(ownerEmail)
                .password(passwordEncoder.encode(ownerPassword))
                .role(Role.OWNER)
                .build();
        userRepository.save(owner);
        log.info("Seeded initial OWNER account: {}", ownerEmail);
    }
}
