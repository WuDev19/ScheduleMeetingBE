package com.example.schedulemeetingbe.exception.custom_exception;

public class CooldownResendException extends RuntimeException {
    public CooldownResendException(String message) {
        super(message);
    }
}
