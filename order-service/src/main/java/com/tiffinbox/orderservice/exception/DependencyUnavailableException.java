package com.tiffinbox.orderservice.exception;

/** A required downstream dependency (e.g. menu-service) is unavailable. Maps to 503. */
public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }
}
