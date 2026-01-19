package com.aimix_aimixapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * API 키 등록/수정 요청 DTO
 */
@Getter
@Setter
public class ApiKeyRequest {

    /**
     * OpenAI API 키 (평문)
     */
    @NotBlank(message = "API 키는 필수입니다")
    private String apiKey;
}
