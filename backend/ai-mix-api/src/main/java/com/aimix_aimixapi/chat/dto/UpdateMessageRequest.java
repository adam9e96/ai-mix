package com.aimix_aimixapi.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 메시지 수정 요청 DTO
 */
@Getter
@Setter
public class UpdateMessageRequest {
    /**
     * 수정할 메시지 내용
     */
    @NotBlank(message = "메시지는 필수입니다")
    private String message;
}

