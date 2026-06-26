package com.tiffinbox.paymentservice.exception;

/** Thrown on an invalid state transition, e.g. marking an already-paid payment as paid. */
public class PaymentStateException extends RuntimeException {
    public PaymentStateException(String message) {
        super(message);
    }
}
