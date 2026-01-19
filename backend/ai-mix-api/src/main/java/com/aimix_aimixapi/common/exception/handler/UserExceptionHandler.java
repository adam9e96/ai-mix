package com.aimix_aimixapi.common.exception.handler;

import com.aimix_aimixapi.common.exception.domain.user.*;
import com.aimix_aimixapi.common.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 사용자(User) 도메인 예외 처리 핸들러
 * - 사용자 조회, 수정, API 키 관련 예외 처리
 */
@Log4j2
@RestControllerAdvice
public class UserExceptionHandler {

    /**
     * 중복 닉네임 예외 처리
     */
    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNicknameException(
            DuplicateNicknameException e, HttpServletRequest request) {
        log.warn("중복 닉네임 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.CONFLICT, "Duplicate Nickname", e.getMessage(), request);
    }

    /**
     * 사용자를 찾을 수 없을 때 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException e, HttpServletRequest request) {
        log.warn("사용자를 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "User Not Found", e.getMessage(), request);
    }

    /**
     * 이메일로 사용자를 찾지 못했을 때 예외 처리
     */
    @ExceptionHandler(UserEmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserEmailNotFoundException(
            UserEmailNotFoundException e, HttpServletRequest request) {
        log.warn("이메일로 사용자를 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.NOT_FOUND, "User Email Not Found", e.getMessage(), request);
    }

    /**
     * 비밀번호 불일치 예외 처리
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(
            PasswordMismatchException e, HttpServletRequest request) {
        log.warn("비밀번호 불일치: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "Password Mismatch", e.getMessage(), request);
    }

    /**
     * API 키 형식이 올바르지 않을 때 예외 처리
     */
    @ExceptionHandler(InvalidApiKeyFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApiKeyFormatException(
            InvalidApiKeyFormatException e, HttpServletRequest request) {
        log.warn("API 키 형식 오류: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Invalid API Key Format", e.getMessage(), request);
    }

    /**
     * API 키 저장 실패 예외 처리
     */
    @ExceptionHandler(ApiKeySaveFailedException.class)
    public ResponseEntity<ErrorResponse> handleApiKeySaveFailedException(
            ApiKeySaveFailedException e, HttpServletRequest request) {
        log.error("API 키 저장 실패: {}", e.getMessage(), e);
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR, "API Key Save Failed", e.getMessage(), request);
    }

    /**
     * API 키 복호화 실패 예외 처리
     */
    @ExceptionHandler(ApiKeyDecryptionFailedException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyDecryptionFailedException(
            ApiKeyDecryptionFailedException e, HttpServletRequest request) {
        log.error("API 키 복호화 실패: {}", e.getMessage(), e);
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR, "API Key Decryption Failed", e.getMessage(), request);
    }
}
