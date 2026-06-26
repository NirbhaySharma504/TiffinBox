package com.tiffinbox.menuservice.exception;

/** Thrown when an order/validation is attempted against a menu that is not OPEN. */
public class MenuClosedException extends RuntimeException {
    public MenuClosedException(String message) {
        super(message);
    }
}
