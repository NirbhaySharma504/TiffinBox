package com.tiffinbox.menuservice.exception;

/** Thrown when the caller's role (from the gateway-forwarded X-User-Role) is insufficient. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
