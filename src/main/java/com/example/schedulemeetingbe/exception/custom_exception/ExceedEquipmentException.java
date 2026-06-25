package com.example.schedulemeetingbe.exception.custom_exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ExceedEquipmentException extends RuntimeException {
    private final List<String> messages;
}
