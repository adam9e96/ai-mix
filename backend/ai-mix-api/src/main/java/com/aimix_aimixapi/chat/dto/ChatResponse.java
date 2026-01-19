package com.aimix_aimixapi.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    /**
     * 세션 ID
     */
    private UUID sessionId;

    /**
     * AI 응답 메시지
     */
    private String answer;

    /**
     * 응답 생성 시각
     */
    private LocalDateTime createdAt;
}

