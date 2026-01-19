package com.aimix_aimixapi.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 사용자 정보 수정 요청 DTO
 * multipart/form-data로 전송되는 필드들
 */
@Getter
@Setter
public class UserUpdateRequest {

    /**
     * 닉네임 (선택, 수정 시에만 전송)
     */
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    private String nickname;

    /**
     * 생년월일 (선택)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

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
