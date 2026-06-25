package com.example.schedulemeetingbe.exception.custom_exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class OverlapBookingException extends RuntimeException {
    private final List<String> reasons;
}
