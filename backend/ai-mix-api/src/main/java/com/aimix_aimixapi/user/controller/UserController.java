package com.aimix_aimixapi.user.controller;

import com.aimix_aimixapi.user.dto.UserResponse;
import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.user.dto.*;
import com.aimix_aimixapi.user.service.UserApiKeyService;
import com.aimix_aimixapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 컨트롤러
 * - 사용자 정보 조회 API
 * - JWT 토큰 인증 필요
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserApiKeyService userApiKeyService;

    /**
     * 현재 로그인한 사용자 정보 조회 (단순 프로필 표시용)
     * GET /api/v1/user/me
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return 사용자 정보 (UserInfo)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // 서비스를 통해 사용자 정보 조회
        UserResponse userResponse = userService.getUser(userDetails.getUser());
        
        return ResponseEntity.ok(userResponse);
    }

    /**
     * 마이페이지용 사용자 정보 조회
     * GET /api/v1/user/mypage
     * - 기본 사용자 정보
     * - 통계 정보 (배틀 참여 횟수, 채팅 세션 수)
     * - 사용자 설정 정보
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return 마이페이지 응답 (MyPageResponse)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @GetMapping("/mypage")
    public ResponseEntity<MyPageResponse> getMyPage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // 서비스를 통해 마이페이지 정보 조회
        MyPageResponse myPageResponse = userService.getMyPage(userDetails.getUser());
        
        return ResponseEntity.ok(myPageResponse);
    }

    /**
     * 비밀번호 확인
     * POST /api/v1/user/verify-password
     * - 개인정보 수정 전 비밀번호 검증용
     * - JWT 토큰 인증 필요
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request 비밀번호 확인 요청
     * @return 비밀번호 확인 응답
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @PostMapping("/verify-password")
    public ResponseEntity<PasswordCheckResponse> verifyPassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PasswordVerifyRequest request) {
        
        // 서비스를 통해 비밀번호 확인
        PasswordCheckResponse response = userService.verifyPassword(
                userDetails.getUser(), 
                request
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 정보 수정
     * PUT /api/v1/user
     * - 닉네임, 생년월일, 자기소개, 설정, 아바타 이미지 수정 가능
     * - multipart/form-data 형식 지원
     * - JWT 토큰 인증 필요
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request 사용자 정보 수정 요청
     * @return 수정된 사용자 정보
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @ModelAttribute UserUpdateRequest request) {

        // 서비스를 통해 사용자 정보 수정
        UserResponse updatedUserResponse = userService.updateUser(userDetails.getUser(), request);
        
        return ResponseEntity.ok(updatedUserResponse);
    }

    /**
     * 프로필 수정
     * PUT /api/v1/user/profile
     * - 프로필 정보만 수정 (자기소개, 아바타 이미지, 설정)
     * - multipart/form-data 형식 지원
     * - JWT 토큰 인증 필요
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request 프로필 수정 요청
     * @return 수정된 사용자 정보
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @ModelAttribute UserProfileUpdateRequest request) {

        // 서비스를 통해 프로필 수정
        UserResponse updatedUserResponse = userService.updateProfile(userDetails.getUser(), request);
        
        return ResponseEntity.ok(updatedUserResponse);
    }

    /**
     * API 키 등록/수정
     * PUT /api/v1/user/api-key
     * - 사용자의 OpenAI API 키를 등록하거나 수정
     * - JWT 토큰 인증 필요
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request API 키 등록 요청
     * @return API 키 정보 응답
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @PutMapping("/api-key")
    public ResponseEntity<ApiKeyResponse> saveApiKey(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ApiKeyRequest request) {
        
        // API 키 저장
        userApiKeyService.saveApiKey(userDetails.getUser(), request.getApiKey());
        
        // 저장된 API 키 정보 조회
        boolean hasApiKey = userApiKeyService.hasApiKey(userDetails.getUser());
        String maskedKey = hasApiKey 
                ? userApiKeyService.maskApiKey(userApiKeyService.getApiKey(userDetails.getUser()))
                : null;
        
        ApiKeyResponse response = ApiKeyResponse.builder()
                .hasApiKey(hasApiKey)
                .maskedKey(maskedKey)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 키 조회
     * GET /api/v1/user/api-key
     * - 사용자의 API 키 존재 여부 및 마스킹된 키 정보 조회
     * - JWT 토큰 인증 필요
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return API 키 정보 응답
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @GetMapping("/api-key")
    public ResponseEntity<ApiKeyResponse> getApiKey(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        boolean hasApiKey = userApiKeyService.hasApiKey(userDetails.getUser());
        String maskedKey = null;
        
        if (hasApiKey) {
            String apiKey = userApiKeyService.getApiKey(userDetails.getUser());
            maskedKey = userApiKeyService.maskApiKey(apiKey);
        }
        
        ApiKeyResponse response = ApiKeyResponse.builder()
                .hasApiKey(hasApiKey)
                .maskedKey(maskedKey)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 키 삭제
     * DELETE /api/v1/user/api-key
     * - 사용자의 API 키를 삭제
     * - JWT 토큰 인증 필요
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return 204 No Content
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @DeleteMapping("/api-key")
    public ResponseEntity<Void> deleteApiKey(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        userApiKeyService.deleteApiKey(userDetails.getUser());
        
        return ResponseEntity.noContent().build();
    }
}

