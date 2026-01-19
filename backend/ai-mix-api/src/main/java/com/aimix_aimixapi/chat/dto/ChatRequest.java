package com.aimix_aimixapi.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 채팅 요청 DTO
 */
@Getter
@Setter
public class ChatRequest {
    /**
     * 세션 ID (선택)
     * 없으면 새 세션 생성
     */
    private UUID sessionId;

    /**
     * 사용자 질문 메시지
     */
//    @NotBlank(message = "메시지는 필수입니다")
    private String message;

    /**
     * 세션 제목 (선택, 새 세션 생성 시 사용)
     * 없으면 첫 메시지의 일부를 제목으로 사용
     */
    private String title;
}

