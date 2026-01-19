package com.aimix_aimixapi.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 채팅방 제목 수정 요청 DTO
 */
@Getter
@Setter
public class UpdateSessionTitleRequest {
    /**
     * 수정할 채팅방 제목
     */
    @NotBlank(message = "제목은 필수입니다")
    private String title;
}

