package com.aimix_aimixapi.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 채팅 세션 목록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionListResponse {
    /**
     * 채팅 세션 목록
     */
    private List<ChatSessionItem> sessions;

    /**
     * 전체 세션 개수
     */
    private Long totalCount;
}

