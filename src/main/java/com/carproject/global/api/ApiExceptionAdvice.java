package com.carproject.global.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * /api/** 전역 예외 처리:
 * - 예외 -> (HTTP Status + ErrorResponse)로 통일
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionAdvice {

    /**
     * ✅ 정적 리소스(예: /favicon.ico) 없을 때 나는 예외는
     * API 예외로 보지 않고 "조용히 404"만 내려보낸다.
     *
     * - 이렇게 하면 handleEtc(Exception)로 떨어지지 않아서
     *   [API] unhandled error 로그가 ERROR로 찍히는 문제가 사라짐.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest req
    ) {
        // 원하면 완전 무로그로 두어도 됨. 필요시 debug 정도만.
        // log.debug("[STATIC] not found uri={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * @Valid 검증 실패 -> 400 + fieldErrors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest req
    ) {
        List<ErrorResponse.FieldError> fields = e.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.name())
                .message("요청 값이 올바르지 않습니다.")
                .path(req.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .fieldErrors(fields)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * JSON 파싱 실패(형식 깨짐/타입 불일치) -> 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParse(
            HttpMessageNotReadableException e,
            HttpServletRequest req
    ) {
        return respond(ErrorCode.INVALID_REQUEST, "요청 본문(JSON) 형식이 올바르지 않습니다.", req, e);
    }

    /**
     * 엔티티 없음 -> 404
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException e,
            HttpServletRequest req
    ) {
        String msg = (e.getMessage() == null || e.getMessage().isBlank())
                ? "대상을 찾을 수 없습니다."
                : e.getMessage();
        return respond(ErrorCode.NOT_FOUND, msg, req, e);
    }

    /**
     * 도메인 검증 실패 -> 400
     * (현재 프로젝트에서 trimColor 불일치, 허용되지 않은 옵션 등은 IllegalArgumentException으로 던지는 흐름이 많음)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException e,
            HttpServletRequest req
    ) {
        String msg = (e.getMessage() == null || e.getMessage().isBlank())
                ? "요청 값이 올바르지 않습니다."
                : e.getMessage();
        return respond(ErrorCode.INVALID_REQUEST, msg, req, e);
    }

    /**
     * DB 제약 위반 -> 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(
            DataIntegrityViolationException e,
            HttpServletRequest req
    ) {
        return respond(
                ErrorCode.DATA_INTEGRITY_VIOLATION,
                "DB 제약조건 위반입니다. (중복/참조/필수값/허용값을 확인하세요)",
                req, e
        );
    }

    /**
     * ResponseStatusException -> 해당 status 유지 + 표준 JSON으로 변환
     * (예: SecurityMemberResolver가 401 던지는 경우도 여기로)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException e,
            HttpServletRequest req
    ) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        ErrorCode code = mapStatusToCode(status);

        String msg = (e.getReason() == null || e.getReason().isBlank())
                ? status.getReasonPhrase()
                : e.getReason();

        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code, msg, req.getRequestURI()));
    }

    /**
     * Spring 내부에서 ErrorResponseException 계열이 나올 수 있어 방어적으로 처리
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponseException(
            ErrorResponseException e,
            HttpServletRequest req
    ) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        ErrorCode code = mapStatusToCode(status);

        String msg = (e.getMessage() == null || e.getMessage().isBlank())
                ? status.getReasonPhrase()
                : e.getMessage();

        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code, msg, req.getRequestURI()));
    }

    /**
     * 그 외 -> 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(
            Exception e,
            HttpServletRequest req
    ) {
        log.error("[API] unhandled error uri={}", req.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "서버 오류가 발생했습니다.", req.getRequestURI()));
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return ErrorResponse.FieldError.builder()
                .field(fe.getField())
                .reason(fe.getDefaultMessage())
                .build();
    }

    private ResponseEntity<ErrorResponse> respond(
            ErrorCode code,
            String message,
            HttpServletRequest req,
            Exception e
    ) {
        // 예상 가능한 예외는 warn 정도로 남김(500은 handleEtc에서 error)
        log.warn("[API] {} uri={} msg={}", code.name(), req.getRequestURI(), message, e);
        return ResponseEntity.status(code.status())
                .body(ErrorResponse.of(code, message, req.getRequestURI()));
    }

    private ErrorCode mapStatusToCode(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) return ErrorCode.UNAUTHORIZED;
        if (status == HttpStatus.FORBIDDEN) return ErrorCode.FORBIDDEN;
        if (status == HttpStatus.NOT_FOUND) return ErrorCode.NOT_FOUND;
        if (status == HttpStatus.BAD_REQUEST) return ErrorCode.INVALID_REQUEST;
        return ErrorCode.INTERNAL_ERROR;
    }
}
