package com.aimix_aimixapi.chat.repository;

import com.aimix_aimixapi.chat.entity.ChatSession;
import com.aimix_aimixapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 채팅 세션 Repository
 * 채팅 세션 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    /**
     * 사용자로 모든 채팅 세션 조회 (생성일 내림차순)
     *
     * @param user 사용자 엔티티
     * @return 해당 사용자의 모든 채팅 세션 목록 (생성일 내림차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 세션 ID와 사용자로 세션 조회 (권한 확인용)
     *
     * @param sessionId 세션 ID
     * @param user 사용자 엔티티
     * @return 해당 세션 (사용자가 소유한 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<ChatSession> findByIdAndUser(UUID sessionId, User user);

    /**
     * 사용자별 채팅 세션 개수 조회 (통계용)
     * - 마이페이지 통계 정보 조회에 사용
     * - COUNT 쿼리로 최적화되어 전체 데이터를 로드하지 않음
     *
     * @param user 사용자 엔티티
     * @return 해당 사용자의 채팅 세션 수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    long countByUser(User user);

    /**
     * 사용자의 세션 목록을 메시지 통계와 함께 조회 (N+1 방지)
     * 세션별 메시지 개수와 마지막 메시지 시각을 한 번의 쿼리로 가져옴
     *
     * @param user 사용자 엔티티
     * @return [ChatSession, messageCount, lastMessageAt] 배열 리스트
     * @since 2026-04-06
     */
    @Query("SELECT s, COUNT(m), MAX(m.createdAt) " +
            "FROM ChatSession s " +
            "LEFT JOIN ChatMessage m ON m.session = s " +
            "WHERE s.user = :user " +
            "GROUP BY s " +
            "ORDER BY s.createdAt DESC")
    List<Object[]> findSessionsWithMessageStats(@Param("user") User user);
}

