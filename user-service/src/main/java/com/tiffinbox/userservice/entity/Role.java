package com.tiffinbox.userservice.entity;

/**
 * Application roles. CUSTOMER places orders; OWNER manages the tiffin business.
 * Persisted as a string (see @Enumerated(EnumType.STRING) on the User entity).
 */
public enum Role {
    CUSTOMER,
    OWNER
}
