package com.example.schedulemeetingbe.dto.common;

import com.example.schedulemeetingbe.constant.StringCommon;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public class ApiResponse {
    private static final String CODE = "code";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";

    private ApiResponse() {
    }

    public static <T> ResponseEntity<ApiResult<T>> success(T data, String message, int code) {
        return ResponseEntity.ok(
                new ApiResult<>(
                        code,
                        StringCommon.SUCCESS,
                        message,
                        data
                )
        );
    }

    public static ResponseEntity<Object> error(String message, int code, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(Map.of(
                        CODE, code,
                        STATUS, StringCommon.ERROR,
                        MESSAGE, message)
                );
    }

    public static ResponseEntity<Object> error(List<String> errorData, int code, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(Map.of(
                        CODE, code,
                        STATUS, StringCommon.ERROR,
                        MESSAGE, errorData)
                );
    }

}
