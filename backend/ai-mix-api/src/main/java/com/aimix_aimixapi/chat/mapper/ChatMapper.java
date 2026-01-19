package com.aimix_aimixapi.chat.mapper;

import com.aimix_aimixapi.chat.dto.ChatResponse;
import com.aimix_aimixapi.chat.dto.ChatSessionItem;
import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    /**
     * 채팅방만 생성된 경우(메시지 없음) 응답 DTO 변환
     * - answer는 항상 null
     * - createdAt은 세션 생성 시각
     */
    @Mapping(source = "id", target = "sessionId")
    @Mapping(target = "answer", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt")
    ChatResponse toChatSessionOnly(ChatSession session);

    /**
     * 사용자 메시지 + AI 응답까지 완료된 경우 응답 DTO 변환
     * - session.id      → sessionId
     * - answer 파라미터 → answer
     * - aiMessage.createdAt → createdAt
     */
    @Mapping(source = "session.id", target = "sessionId")
    @Mapping(source = "answer", target = "answer")
    @Mapping(source = "aiMessage.createdAt", target = "createdAt")
    ChatResponse toChatResponse(ChatSession session, String answer, ChatMessage aiMessage);

    /**
     * 채팅 세션 엔티티 + 통계 정보를 채팅 세션 목록용 DTO로 변환
     *
     * @param session      채팅 세션 엔티티
     * @param messageCount 메시지 개수
     * @param lastMessageAt 마지막 메시지 시각 (없으면 null)
     */
    @Mapping(source = "session.id", target = "sessionId")
    @Mapping(source = "session.title", target = "title")
    @Mapping(source = "session.createdAt", target = "createdAt")
    @Mapping(source = "lastMessageAt", target = "lastMessageAt")
    @Mapping(source = "messageCount", target = "messageCount")
    ChatSessionItem toChatSessionItem(ChatSession session, Long messageCount, LocalDateTime lastMessageAt);
}
