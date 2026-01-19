package com.aimix_aimixapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 회원가입 요청 DTO
 * multipart/form-data로 전송되는 필드들
 */
@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    private String nickname;

    /**
     * 생년월일 (선택)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 개인정보 동의 여부 (필수)
     */
    @NotNull(message = "개인정보 동의는 필수입니다")
    private Boolean isAgreed;

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
     */
    private MultipartFile avatar;
}

