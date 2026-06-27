package com.tiffinbox.feedbackservice.exception;

public class DuplicateFeedbackException extends RuntimeException {
    public DuplicateFeedbackException(String message) {
        super(message);
    }
}
