package com.aimix_aimixapi.chat.repository;

import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 채팅 메시지 Repository
 * 채팅 메시지 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    /**
     * 세션으로 모든 메시지 조회 (생성일 오름차순)
     * 
     * @param session 채팅 세션 엔티티
     * @return 해당 세션의 모든 메시지 목록 (생성일 오름차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);

    /**
     * 세션 ID로 모든 메시지 조회 (생성일 오름차순)
     * 
     * @param sessionId 세션 ID
     * @return 해당 세션의 모든 메시지 목록 (생성일 오름차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    /**
     * 세션 ID로 메시지 개수 조회
     * 
     * @param sessionId 세션 ID
     * @return 해당 세션의 메시지 개수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    long countBySessionId(UUID sessionId);
}

