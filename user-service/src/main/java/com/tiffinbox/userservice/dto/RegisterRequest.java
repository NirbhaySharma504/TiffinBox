package com.tiffinbox.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming registration payload. Self-registration ALWAYS creates a CUSTOMER — the role
 * is not client-supplied (owners are provisioned via the seeder, never signed up). This
 * closes a privilege-escalation hole where a caller could request role=OWNER.
 */
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String password,
        String phone
) {
}
