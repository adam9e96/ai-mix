package com.aimix_aimixapi.chat.dto;

import com.aimix_aimixapi.chat.entity.MessageSender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅 메시지 항목 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageItem {
    /**
     * 메시지 ID
     */
    private UUID messageId;

    /**
     * 발신자 (USER 또는 AI)
     */
    private MessageSender sender;

    /**
     * 메시지 내용
     */
    private String message;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;
}

