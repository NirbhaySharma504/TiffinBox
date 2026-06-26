package com.tiffinbox.userservice.dto;

/**
 * Returned on successful login/registration: a signed JWT plus the user profile.
 */
public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}
