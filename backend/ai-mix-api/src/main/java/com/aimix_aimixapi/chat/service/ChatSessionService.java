package com.aimix_aimixapi.chat.service;

import com.aimix_aimixapi.chat.config.ChatProperties;
import com.aimix_aimixapi.chat.dto.ChatSessionItem;
import com.aimix_aimixapi.chat.entity.ChatSession;
import com.aimix_aimixapi.chat.message.ChatMessage;
import com.aimix_aimixapi.chat.repository.ChatSessionRepository;
import com.aimix_aimixapi.common.exception.domain.AccessDeniedException;
import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 채팅 세션 서비스
 * - 채팅 세션 조회, 생성, 수정, 삭제 등 모든 세션 관련 로직 관리
 * - 여러 서비스에서 공통으로 사용되는 세션 관련 로직 제공
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageService chatMessageService;
    private final ChatProperties chatProperties;

    /**
     * 세션 ID로 세션 조회 (권한 확인 포함)
     * 
     * @param sessionId 세션 ID
     * @param user 사용자 엔티티 (권한 확인용)
     * @return 채팅 세션 엔티티
     * @throws ResourceNotFoundException 세션을 찾을 수 없는 경우
     * @throws AccessDeniedException 세션 접근 권한이 없는 경우
     * @since 2025-12-10
     */
    @Transactional(readOnly = true)
    public ChatSession findByIdWithAuthCheck(UUID sessionId, User user) {
        log.debug("세션 조회 (권한 확인 포함): sessionId={}, userId={}", sessionId, user.getId());
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("세션을 찾을 수 없습니다: sessionId={}", sessionId);
                    return new ResourceNotFoundException(ChatMessage.SESSION_NOT_FOUND.format(sessionId));
                });

        validateSessionAccess(session, user, sessionId);
        return session;
    }

    /**
     * 세션 접근 권한을 검증합니다.
     *
     * @param session 세션 엔티티
     * @param user 사용자 엔티티
     * @param sessionId 세션 ID (로깅용)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    private void validateSessionAccess(ChatSession session, User user, UUID sessionId) {
        if (!session.getUser().getId().equals(user.getId())) {
            log.warn("세션 접근 권한이 없습니다: sessionId={}, userId={}, sessionUserId={}", 
                    sessionId, user.getId(), session.getUser().getId());
            throw new AccessDeniedException(ChatMessage.SESSION_ACCESS_DENIED.getMessage());
        }
    }

    /**
     * 세션 ID와 사용자로 세션 조회 (권한 확인 포함)
     * Repository의 findByIdAndUser를 사용하여 더 효율적으로 조회
     *
     * @param sessionId 세션 ID
     * @param user      사용자 엔티티
     * @throws ResourceNotFoundException 세션을 찾을 수 없거나 권한이 없는 경우
     * @since 2025-12-10
     */
    @Transactional(readOnly = true)
    public void findByIdAndUser(UUID sessionId, User user) {
        log.debug("세션 조회 (사용자 포함): sessionId={}, userId={}", sessionId, user.getId());
        chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> {
                    log.warn("세션을 찾을 수 없거나 권한이 없습니다: sessionId={}, userId={}",
                            sessionId, user.getId());
                    return new ResourceNotFoundException(ChatMessage.SESSION_NOT_FOUND.format(sessionId));
                });
    }

    /**
     * 사용자의 모든 채팅 세션 조회 (생성일 내림차순)
     *
     * @param user 사용자 엔티티
     * @return 채팅 세션 목록 (생성일 내림차순)
     * @since 2025-12-16
     */
    @Transactional(readOnly = true)
    public List<ChatSession> findByUserOrderByCreatedAtDesc(User user) {
        log.debug("사용자의 세션 목록 조회: userId={}", user.getId());
        return chatSessionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 사용자의 세션 목록을 메시지 통계와 함께 조회 (N+1 방지)
     * 단일 JPQL 쿼리로 세션 + 메시지 개수 + 마지막 메시지 시각을 한 번에 가져옴
     *
     * @param user 사용자 엔티티
     * @return [ChatSession, messageCount, lastMessageAt] 배열 리스트
     * @since 2026-04-06
     */
    @Transactional(readOnly = true)
    public List<Object[]> findSessionsWithMessageStats(User user) {
        log.debug("사용자의 세션 목록 + 통계 조회: userId={}", user.getId());
        return chatSessionRepository.findSessionsWithMessageStats(user);
    }

    /**
     * 세션 조회 또는 생성
     * 
     * @param user         사용자 엔티티 (null이 아니어야 함)
     * @param sessionId    세션 ID (null이면 새 세션 생성, null이 아니면 기존 세션 조회)
     * @param title        세션 제목 (새 세션 생성 시 사용, null이거나 비어있으면 firstMessage에서 생성)
     * @param firstMessage 첫 메시지 (새 세션 생성 시 제목 생성에 사용, null일 수 있음)
     * @return 조회되거나 생성된 채팅 세션 엔티티
     * @since 2025-12-16
     */
    @Transactional
    public ChatSession getOrCreateSession(User user, UUID sessionId, String title, String firstMessage) {
        // 1. 기존 세션 ID가 있는 경우: 조회 + 권한 체크만 수행
        if (sessionId != null) {
            return findByIdWithAuthCheck(sessionId, user);
        }

        // 2. 새 세션 생성: 제목 계산 로직을 메서드로 분리해 가독성 및 재사용성 향상
        String sessionTitle = resolveSessionTitle(title, firstMessage);

        ChatSession newSession = ChatSession.builder()
                .user(user)
                .title(sessionTitle)
                .createdAt(LocalDateTime.now())
                .build();

        ChatSession savedSession = chatSessionRepository.save(newSession);
        log.info("새 세션 생성: sessionId={}, title={}", savedSession.getId(), savedSession.getTitle());
        return savedSession;
    }

    /**
     * 채팅 세션 제목을 결정합니다.
     * - title이 비어있지 않으면 그대로 사용
     * - 그렇지 않으면 firstMessage에서 제목을 생성
     *
     * @param title 제목 (null 가능)
     * @param firstMessage 첫 메시지 (null 가능)
     * @return 결정된 세션 제목
     * @since 2025-12-16
     */
    private String resolveSessionTitle(String title, String firstMessage) {
        if (title != null) {
            String trimmedTitle = title.trim();
            if (!trimmedTitle.isEmpty()) {
                return trimmedTitle;
            }
        }
        return generateTitleFromMessage(firstMessage);
    }

    /**
     * 메시지 내용에서 채팅방 제목을 생성합니다.
     *
     * <p>동작 방식:
     * <ul>
     *   <li>메시지가 null이거나 공백만 있는 경우: 기본 제목 반환</li>
     *   <li>메시지가 최대 길이 이하인 경우: 앞뒤 공백 제거 후 그대로 반환</li>
     *   <li>메시지가 최대 길이 초과인 경우: 앞뒤 공백 제거 후 최대 길이만큼만 추출하고 생략 표시 추가</li>
     * </ul>
     *
     * @param message 제목을 생성할 메시지 (null 가능)
     * @return 생성된 제목
     * @since 2025-12-16
     */
    private String generateTitleFromMessage(String message) {
        if (isMessageEmpty(message)) {
            return chatProperties.getDefaultTitle();
        }

        String trimmed = message.trim();
        int maxLength = chatProperties.getMaxTitleLength();
        
        if (shouldTruncateTitle(trimmed, maxLength)) {
            return truncateTitle(trimmed, maxLength);
        }
        
        return trimmed;
    }

    /**
     * 메시지가 비어있는지 확인합니다.
     *
     * @param message 메시지
     * @return 비어있으면 true
     */
    private boolean isMessageEmpty(String message) {
        return message == null || message.trim().isEmpty();
    }

    /**
     * 제목을 생략해야 하는지 확인합니다.
     *
     * @param trimmed 공백 제거된 메시지
     * @param maxLength 최대 길이
     * @return 생략이 필요하면 true
     */
    private boolean shouldTruncateTitle(String trimmed, int maxLength) {
        return trimmed.length() > maxLength;
    }

    /**
     * 제목을 최대 길이로 자르고 생략 표시를 추가합니다.
     *
     * @param trimmed 공백 제거된 메시지
     * @param maxLength 최대 길이
     * @return 생략된 제목
     */
    private String truncateTitle(String trimmed, int maxLength) {
        return trimmed.substring(0, maxLength) + chatProperties.getTitleEllipsis();
    }

    /**
     * 세션 제목 수정
     * 권한 확인 포함
     *
     * @param sessionId 세션 ID
     * @param user 사용자 엔티티
     * @param newTitle 새로운 제목
     * @return 수정된 세션 정보
     * @throws ResourceNotFoundException 세션을 찾을 수 없는 경우
     * @throws AccessDeniedException 권한이 없는 경우
     * @since 2025-12-16
     */
    @Transactional
    public ChatSessionItem updateTitle(UUID sessionId, User user, String newTitle) {
        log.info("세션 제목 수정 요청: sessionId={}, userId={}, newTitle={}", sessionId, user.getId(), newTitle);

        // 1. 세션 조회 및 권한 확인
        ChatSession session = findByIdWithAuthCheck(sessionId, user);

        // 2. 제목 업데이트
        session.setTitle(newTitle);
        chatSessionRepository.save(session);
        log.info("세션 제목 수정 완료: sessionId={}, title={}", sessionId, session.getTitle());

        // 3. 응답 DTO를 위한 데이터 조회
        long messageCount = chatMessageService.countBySessionId(session.getId());
        LocalDateTime lastMessageAt = chatMessageService.getLastMessageAtBySessionId(session.getId());

        // 4. 응답 생성
        return ChatSessionItem.builder()
                .sessionId(session.getId())
                .title(session.getTitle())
                .createdAt(session.getCreatedAt())
                .lastMessageAt(lastMessageAt)
                .messageCount(messageCount)
                .build();
    }

    /**
     * 세션 삭제
     * 권한 확인 포함
     * 세션 삭제 시 cascade 설정으로 인해 관련된 모든 메시지도 자동 삭제됨
     *
     * @param sessionId 세션 ID
     * @param user 사용자 엔티티
     * @throws ResourceNotFoundException 세션을 찾을 수 없는 경우
     * @throws AccessDeniedException 권한이 없는 경우
     * @since 2025-12-16
     */
    @Transactional
    public void deleteSession(UUID sessionId, User user) {
        log.info("세션 삭제 요청: sessionId={}, userId={}", sessionId, user.getId());

        // 1. 세션 조회 및 권한 확인
        ChatSession session = findByIdWithAuthCheck(sessionId, user);

        // 2. 세션 삭제 (cascade 설정으로 인해 관련 메시지도 자동 삭제됨)
        chatSessionRepository.delete(session);
        log.info("세션 삭제 완료: sessionId={}, title={}", sessionId, session.getTitle());
    }
}

