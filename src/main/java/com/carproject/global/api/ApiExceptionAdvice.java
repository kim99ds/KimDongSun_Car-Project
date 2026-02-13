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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * ✅ REST(API) 컨트롤러 전용 전역 예외 처리
 * - MVC(Thymeleaf) 에러 페이지 렌더링과 충돌하지 않게 @RestController에만 적용
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionAdvice {

    /**
     * 정적 리소스(예: /favicon.ico) 없을 때 나는 예외는
     * API 예외로 보지 않고 "조용히 404"만 내려보낸다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParse(
            HttpMessageNotReadableException e,
            HttpServletRequest req
    ) {
        return respond(ErrorCode.INVALID_REQUEST, "요청 본문(JSON) 형식이 올바르지 않습니다.", req, e);
    }

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(
            DataIntegrityViolationException e,
            HttpServletRequest req
    ) {
        return respond(
                ErrorCode.DATA_INTEGRITY_VIOLATION,
                "DB 제약조건 위반입니다. (중복/참조/필수값/허용값/허용범위를 확인하세요)",
                req, e
        );
    }

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
