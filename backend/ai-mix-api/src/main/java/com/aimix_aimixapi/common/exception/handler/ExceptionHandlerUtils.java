package com.aimix_aimixapi.common.exception.handler;

import com.aimix_aimixapi.common.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * 예외 핸들러 공통 유틸리티
 * - ErrorResponse 생성 로직 중복 제거
 */
@Log4j2
public class ExceptionHandlerUtils {

    /**
     * ErrorResponse 생성 (공통 메서드)
     *
     * @param status  HTTP 상태 코드
     * @param error   에러 타입
     * @param message 에러 메시지
     * @param request HTTP 요청
     * @return ErrorResponse
     */
    public static ErrorResponse buildErrorResponse(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    /**
     * ResponseEntity 생성 (공통 메서드)
     *
     * @param status  HTTP 상태 코드
     * @param error   에러 타입
     * @param message 에러 메시지
     * @param request HTTP 요청
     * @return ResponseEntity<ErrorResponse>
     */
    public static ResponseEntity<ErrorResponse> buildResponseEntity(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(status, error, message, request);
        return ResponseEntity.status(status.value()).body(errorResponse);
    }
}
