package com.aimix_aimixapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 비밀번호 확인 요청 DTO
 * - 개인정보 수정 전 비밀번호 검증용
 */
@Getter
@Setter
public class PasswordVerifyRequest {

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
