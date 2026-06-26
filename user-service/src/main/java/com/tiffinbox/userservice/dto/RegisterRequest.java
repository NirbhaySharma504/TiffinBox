package com.tiffinbox.userservice.dto;

import com.tiffinbox.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming registration payload. {@code role} is optional and defaults to CUSTOMER
 * if omitted (see UserService).
 */
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String password,
        String phone,
        Role role
) {
}
