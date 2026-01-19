package com.aimix_aimixapi.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * 채팅 메시지 목록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageListResponse {
    /**
     * 세션 ID
     */
    private UUID sessionId;

    /**
     * 세션 제목
     */
    private String title;

    /**
     * 채팅 메시지 목록 (생성일 오름차순)
     */
    private List<ChatMessageItem> messages;

    /**
     * 전체 메시지 개수
     */
    private Long totalCount;
}

