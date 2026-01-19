package com.aimix_aimixapi.common.exception.handler;

import com.aimix_aimixapi.auth.message.AuthMessage;
import com.aimix_aimixapi.common.exception.domain.ExpiredTokenException;
import com.aimix_aimixapi.common.exception.domain.InvalidTokenException;
import com.aimix_aimixapi.common.exception.domain.auth.*;
import com.aimix_aimixapi.common.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 인증(Auth) 도메인 예외 처리 핸들러
 * - 회원가입, 로그인, 토큰 관련 예외 처리
 */
@Log4j2
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * 중복 이메일 예외 처리
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
            DuplicateEmailException e, HttpServletRequest request) {
        log.warn("중복 이메일 예외: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.CONFLICT, "Duplicate Email", e.getMessage(), request);
    }

    /**
     * UsernameNotFoundException 처리
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException e, HttpServletRequest request) {
        log.warn("사용자를 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "User Not Found", e.getMessage(), request);
    }

    /**
     * UserDetailsNotFoundException 처리
     */
    @ExceptionHandler(UserDetailsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserDetailsNotFoundException(
            UserDetailsNotFoundException e, HttpServletRequest request) {
        log.warn("사용자 정보를 찾을 수 없음: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "User Details Not Found", e.getMessage(), request);
    }

    /**
     * BadCredentialsException 처리 (로그인 시 이메일/비밀번호 불일치)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException e, HttpServletRequest request) {
        log.warn("인증 실패: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED,
                "Authentication Failed",
                AuthMessage.AUTHENTICATION_FAILED.getMessage(),
                request);
    }

    /**
     * 인증 실패 예외 처리
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(
            AuthenticationFailedException e, HttpServletRequest request) {
        log.warn("인증 실패: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "Authentication Failed", e.getMessage(), request);
    }

    /**
     * 유효하지 않은 토큰 예외 처리
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException e, HttpServletRequest request) {
        log.warn("유효하지 않은 토큰: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "Invalid Token", e.getMessage(), request);
    }

    /**
     * 만료된 토큰 예외 처리
     */
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredTokenException(
            ExpiredTokenException e, HttpServletRequest request) {
        log.warn("만료된 토큰: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "Expired Token", e.getMessage(), request);
    }

    /**
     * RefreshToken 필수 예외 처리
     */
    @ExceptionHandler(RefreshTokenRequiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenRequiredException(
            RefreshTokenRequiredException e, HttpServletRequest request) {
        log.warn("RefreshToken 필수: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Refresh Token Required", e.getMessage(), request);
    }

    /**
     * 유효하지 않은 RefreshToken 예외 처리
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException e, HttpServletRequest request) {
        log.warn("유효하지 않은 RefreshToken: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.UNAUTHORIZED, "Invalid Refresh Token", e.getMessage(), request);
    }

    /**
     * 잘못된 요청 예외 처리
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException e, HttpServletRequest request) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Invalid Request", e.getMessage(), request);
    }

    /**
     * 요청 정보 필수 예외 처리
     */
    @ExceptionHandler(RequestRequiredException.class)
    public ResponseEntity<ErrorResponse> handleRequestRequiredException(
            RequestRequiredException e, HttpServletRequest request) {
        log.warn("요청 정보 필수: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Request Required", e.getMessage(), request);
    }

    /**
     * 이메일 필수 입력 예외 처리
     */
    @ExceptionHandler(EmailRequiredException.class)
    public ResponseEntity<ErrorResponse> handleEmailRequiredException(
            EmailRequiredException e, HttpServletRequest request) {
        log.warn("이메일 필수 입력: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Email Required", e.getMessage(), request);
    }

    /**
     * 비밀번호 필수 입력 예외 처리
     */
    @ExceptionHandler(PasswordRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePasswordRequiredException(
            PasswordRequiredException e, HttpServletRequest request) {
        log.warn("비밀번호 필수 입력: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Password Required", e.getMessage(), request);
    }

    /**
     * 닉네임 필수 입력 예외 처리
     */
    @ExceptionHandler(NicknameRequiredException.class)
    public ResponseEntity<ErrorResponse> handleNicknameRequiredException(
            NicknameRequiredException e, HttpServletRequest request) {
        log.warn("닉네임 필수 입력: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Nickname Required", e.getMessage(), request);
    }

    /**
     * 개인정보 동의 필수 예외 처리
     */
    @ExceptionHandler(AgreementRequiredException.class)
    public ResponseEntity<ErrorResponse> handleAgreementRequiredException(
            AgreementRequiredException e, HttpServletRequest request) {
        log.warn("개인정보 동의 필수: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "Agreement Required", e.getMessage(), request);
    }

    /**
     * User 엔티티 필수 예외 처리
     * - UserDetailsImpl 생성 시 User가 null인 경우 발생
     */
    @ExceptionHandler(UserRequiredException.class)
    public ResponseEntity<ErrorResponse> handleUserRequiredException(
            UserRequiredException e, HttpServletRequest request) {
        log.warn("User 엔티티 필수: {}", e.getMessage());
        return ExceptionHandlerUtils.buildResponseEntity(
                HttpStatus.BAD_REQUEST, "User Required", e.getMessage(), request);
    }
}
