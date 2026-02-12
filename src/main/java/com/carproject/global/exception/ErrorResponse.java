package com.carproject.global.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path, LocalDateTime.now(), null);
    }

    public static ErrorResponse of(
            String code,
            String message,
            String path,
            List<FieldError> fieldErrors
    ) {
        return new ErrorResponse(code, message, path, LocalDateTime.now(), fieldErrors);
    }

    public record FieldError(String field, String reason) {}
}
