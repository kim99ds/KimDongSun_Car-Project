package com.carproject.global.api;

import org.springframework.http.HttpStatus;

/**
 * API 에러 코드 표준.
 * - name(): 응답 JSON의 code 값으로 사용
 * - status(): 응답 HTTP Status로 사용
 */
public enum ErrorCode {

    // 공통
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT),

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
