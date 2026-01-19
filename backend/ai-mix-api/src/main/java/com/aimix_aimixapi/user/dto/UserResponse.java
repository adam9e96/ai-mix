package com.aimix_aimixapi.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 사용자 정보 DTO
 * - 로그인/회원가입 응답에 사용
 * - 마이페이지 정보 조회에 사용
 * - 보안: id(PK)는 노출하지 않음
 * - null 필드는 JSON 응답에서 제외됨 (@JsonInclude)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 필드중 null은 보내지않음
public class UserResponse {

    /**
     * 사용자 이메일 (로그인 ID)
     * - 민감 정보이지만 로그인 ID로 사용되므로 필요
     */
    private String email;

    /**
     * 사용자 닉네임 (공개 정보)
     */
    private String nickname;

    /**
     * 사용자 권한 (USER, ADMIN)
     */
    private String role;

    /**
     * 프로필 아바타 이미지 URL
     * - 프로필 이미지가 없으면 null
     */
    private String avatarUrl;

    /**
     * 가입일 (선택적)
     * - 사용자 경험 향상 (가입 기간 표시 등)
     */
    private LocalDateTime createdAt;

    /**
     * 생년월일
     * - 마이페이지에서 사용
     */
    private LocalDate birthDate;

    /**
     * 자기소개
     * - 마이페이지에서 사용
     */
    private String bio;

    /**
     * 마지막 로그인 시간
     * - 마이페이지에서 사용
     */
    private LocalDateTime lastLoginAt;

    /**
     * 사용자 설정 (UserProfile.settings)
     * - 다크모드, 알림 설정 등
     * - 없으면 null
     */
    private Map<String, Object> settings;
}

