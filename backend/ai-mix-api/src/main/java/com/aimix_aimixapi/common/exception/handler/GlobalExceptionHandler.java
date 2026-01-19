package com.aimix_aimixapi.common.exception.handler;

import com.aimix_aimixapi.common.exception.domain.AccessDeniedException;
import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import com.aimix_aimixapi.common.exception.message.RequestMessage;
import com.aimix_aimixapi.common.exception.message.ServerMessage;
import com.aimix_aimixapi.common.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * - 공통 예외 처리 (Validation, 일반 Exception 등)
 * - 도메인별 예외는 각 도메인 핸들러에서 처리
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 리소스를 찾을 수 없을 때 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "Resource Not Found", e.getMessage(), request);
    }

    /**
     * 접근 권한이 없을 때 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.FORBIDDEN, "Access Denied", e.getMessage(), request);
    }

    /**
     * Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation 예외: {}", e.getMessage());

        Map<String, Object> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", RequestMessage.VALIDATION_FAILED.getMessage());
        response.put("errors", errors);
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * ConstraintViolationException 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        log.warn("Constraint 위반: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Constraint Violation", e.getMessage(), request);
    }

    /**
     * Content-Type 미지원 예외 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("지원하지 않는 Content-Type: {}", e.getContentType());
        String message = RequestMessage.UNSUPPORTED_MEDIA_TYPE.format(e.getContentType().toString());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", message, request);
    }

    /**
     * 요청 본문 파싱 실패 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());

        String message = RequestMessage.MESSAGE_NOT_READABLE.getMessage();
        if (e.getMessage() != null && e.getMessage().contains("Content-Type")) {
            message = RequestMessage.MESSAGE_NOT_READABLE_CONTENT_TYPE.getMessage();
        }

        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ServerMessage.INTERNAL_ERROR.getMessage(),
                request);
    }
}
