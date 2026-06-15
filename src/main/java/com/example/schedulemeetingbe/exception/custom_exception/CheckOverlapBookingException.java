package com.example.schedulemeetingbe.exception.custom_exception;

public class CheckOverlapBookingException extends RuntimeException {
    public CheckOverlapBookingException(String message) {
        super(message);
    }
}
