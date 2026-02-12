package com.carproject.global.api;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * API 표준 에러 응답 포맷.
 */
@Getter
@Builder
public class ErrorResponse {

    private final String code;        // ErrorCode.name()
    private final String message;     // 사람이 읽을 메시지
    private final String path;        // 요청 경로
    private final OffsetDateTime timestamp;

    private final List<FieldError> fieldErrors; // validation 에러용(없으면 null)

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String reason;
    }

    public static ErrorResponse of(ErrorCode code, String message, String path) {
        return ErrorResponse.builder()
                .code(code.name())
                .message(message)
                .path(path)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
