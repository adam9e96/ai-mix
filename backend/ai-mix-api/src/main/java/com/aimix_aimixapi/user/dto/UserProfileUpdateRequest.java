package com.aimix_aimixapi.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 수정 요청 DTO
 * multipart/form-data로 전송되는 필드들
 * - UserProfile의 필드만 수정 (bio, avatar, settings)
 */
@Getter
@Setter
public class UserProfileUpdateRequest {

    /**
     * 자기소개 (선택)
     */
    private String bio;

    /**
     * 설정 JSON 문자열 (선택)
     * 예: {"darkMode": false, "notifications": true}
     */
    private String settings;

    /**
     * 프로필 아바타 이미지 파일 (선택)
     * 새 파일을 업로드하면 기존 아바타는 삭제됨
     */
    private MultipartFile avatar;
}
