package com.example.schedulemeetingbe.exception.custom_exception;

import com.example.schedulemeetingbe.exception.ErrorResponse;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public BusinessException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }
}