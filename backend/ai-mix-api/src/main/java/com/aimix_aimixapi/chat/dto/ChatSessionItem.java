package com.aimix_aimixapi.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅 세션 목록 항목 DTO
 * 채팅방 목록에서 각 세션의 정보를 나타냄
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionItem {
    /**
     * 세션 ID
     */
    private UUID sessionId;

    /**
     * 채팅방 제목
     */
    private String title;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 마지막 메시지 시각 (선택)
     */
    private LocalDateTime lastMessageAt;

    /**
     * 메시지 개수
     */
    private Long messageCount;
}

