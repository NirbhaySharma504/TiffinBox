package com.tiffinbox.userservice.dto;

import com.tiffinbox.userservice.entity.Role;
import com.tiffinbox.userservice.entity.User;

/**
 * Outgoing user view. Never exposes the password hash.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}
