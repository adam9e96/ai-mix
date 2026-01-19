package com.aimix_aimixapi.auth.controller;

import com.aimix_aimixapi.auth.dto.*;
import com.aimix_aimixapi.auth.service.AuthService;
import com.aimix_aimixapi.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러
 * - 회원가입, 로그인, 토큰 재발급, 로그아웃 API
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 이메일 중복 체크
     * GET /api/v1/auth/check-email
     */
    @GetMapping("/check-email")
    public ResponseEntity<DuplicateCheckResponse> checkEmail(@RequestParam String email) {
        log.info("이메일 중복 체크 요청: email={}", email);
        DuplicateCheckResponse response = authService.checkEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 중복 체크
     * GET /api/v1/auth/check-nickname
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<DuplicateCheckResponse> checkNickname(@RequestParam String nickname) {
        log.info("닉네임 중복 체크 요청: nickname={}", nickname);
        DuplicateCheckResponse response = authService.checkNickname(nickname);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입 (multipart/form-data 지원)
     * POST /api/v1/auth/signup
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> signup(
            @Valid @ModelAttribute SignupRequest request) {
        log.info("회원가입 요청: email={}, nickname={}", request.getEmail(), request.getNickname());

        UserResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     * POST /api/v1/auth/login
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {
        log.info("로그인 요청: email={}", request.getEmail());
        UserResponse response = authService.login(request, httpResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 재발급
     * POST /api/v1/auth/refresh
     */
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse) {
        log.info("토큰 재발급 요청");
        TokenResponse response = authService.refreshToken(refreshToken, httpResponse);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping(value = "/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse) {
        log.info("로그아웃 요청");
        authService.logout(refreshToken, httpResponse);
        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 인증 코드 발송
     * POST /api/v1/auth/email/send
     */
    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmailVerification(@Valid @RequestBody EmailRequest request) {
        log.info("이메일 인증 코드 발송 요청: email={}", request.getEmail());
        authService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 인증 코드 검증
     * POST /api/v1/auth/email/verify
     */
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        log.info("이메일 인증 코드 검증 요청: email={}, code={}", request.getEmail(), request.getCode());
        boolean verified = authService.verifyEmail(request.getEmail(), request.getCode());
        if (verified) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
