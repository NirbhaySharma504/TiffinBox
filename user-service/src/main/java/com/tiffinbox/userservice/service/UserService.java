package com.tiffinbox.userservice.service;

import com.tiffinbox.userservice.dto.LoginRequest;
import com.tiffinbox.userservice.dto.RegisterRequest;
import com.tiffinbox.userservice.entity.Role;
import com.tiffinbox.userservice.entity.User;
import com.tiffinbox.userservice.exception.EmailAlreadyExistsException;
import com.tiffinbox.userservice.exception.InvalidCredentialsException;
import com.tiffinbox.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a user with a BCrypt-hashed password. Email must be unique.
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Self-registration is always a CUSTOMER; OWNER accounts are seeded, never signed up.
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(Role.CUSTOMER)
                .build();

        return userRepository.save(user);
    }

    /**
     * Verifies credentials against the stored BCrypt hash.
     * Returns the user on success; throws on any mismatch.
     */
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
}
