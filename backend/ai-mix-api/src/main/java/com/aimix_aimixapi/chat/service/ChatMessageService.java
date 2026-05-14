package com.aimix_aimixapi.chat.service;

import com.aimix_aimixapi.chat.dto.ChatMessageItem;
import com.aimix_aimixapi.chat.dto.UpdateMessageRequest;
import com.aimix_aimixapi.chat.config.ChatProperties;
import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.entity.ChatSession;
import com.aimix_aimixapi.chat.entity.MessageSender;
import com.aimix_aimixapi.chat.repository.ChatMessageRepository;
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
 * 채팅 메시지 서비스
 * - 채팅 메시지 조회, 저장, 수정, 삭제 등 모든 메시지 관련 로직 관리
 * - 여러 서비스에서 공통으로 사용되는 메시지 관련 로직 제공
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatProperties chatProperties;

    /**
     * 세션 ID로 모든 메시지 조회 (생성일 오름차순)
     * 
     * @param sessionId 세션 ID
     * @return 메시지 목록
     * @since 2025-12-10
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId) {
        log.debug("세션의 메시지 조회: sessionId={}", sessionId);
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * 세션으로 모든 메시지 조회 (생성일 오름차순)
     * 
     * @param session 세션 엔티티
     * @return 메시지 목록
     * @since 2025-12-10
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session) {
        log.debug("세션의 메시지 조회: sessionId={}", session.getId());
        return chatMessageRepository.findBySessionOrderByCreatedAtAsc(session);
    }

    /**
     * 세션 ID로 메시지 개수 조회
     * 
     * @param sessionId 세션 ID
     * @return 메시지 개수
     * @since 2025-12-10
     */
    @Transactional(readOnly = true)
    public long countBySessionId(UUID sessionId) {
        log.debug("세션의 메시지 개수 조회: sessionId={}", sessionId);
        return chatMessageRepository.countBySessionId(sessionId);
    }

    /**
     * 메시지 ID로 메시지 조회
     * 
     * @param messageId 메시지 ID
     * @return 메시지 엔티티
     * @throws ResourceNotFoundException 메시지를 찾을 수 없는 경우
     * @since 2025-12-16
     */
    @Transactional(readOnly = true)
    public ChatMessage findById(UUID messageId) {
        log.debug("메시지 조회: messageId={}", messageId);
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.warn("메시지를 찾을 수 없습니다: messageId={}", messageId);
                    return new ResourceNotFoundException(
                            com.aimix_aimixapi.chat.message.ChatMessage.MESSAGE_NOT_FOUND.format(messageId));
                });
    }

    /**
     * 세션 ID로 해당 세션의 마지막 메시지 시각을 조회합니다.
     * MAX 집계 쿼리를 사용하여 전체 메시지 로딩 없이 마지막 시각만 조회합니다.
     * 메시지가 하나도 없으면 null을 반환합니다.
     *
     * @param sessionId 세션 ID
     * @return 마지막 메시지 시각 또는 null
     * @since 2025-12-16
     */
    @Transactional(readOnly = true)
    public LocalDateTime getLastMessageAtBySessionId(UUID sessionId) {
        log.debug("마지막 메시지 시각 조회: sessionId={}", sessionId);
        return chatMessageRepository.findLastMessageAtBySessionId(sessionId);
    }

    /**
     * 사용자 메시지 저장
     * 
     * @param session 세션 엔티티
     * @param message 메시지 내용
     * @return 저장된 메시지 엔티티
     * @since 2025-12-16
     */
    @Transactional
    public ChatMessage saveUserMessage(ChatSession session, String message) {
        log.debug("사용자 메시지 저장: sessionId={}", session.getId());
        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .sender(MessageSender.USER)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(userMessage);
        session.addMessage(savedMessage);
        log.info("사용자 메시지 저장 완료: messageId={}", savedMessage.getId());
        return savedMessage;
    }

    /**
     * AI 메시지 저장
     * 
     * @param session 세션 엔티티
     * @param message 메시지 내용
     * @return 저장된 메시지 엔티티
     * @since 2025-12-16
     */
    @Transactional
    public ChatMessage saveAiMessage(ChatSession session, String message) {
        log.debug("AI 메시지 저장: sessionId={}", session.getId());
        ChatMessage aiMessage = ChatMessage.builder()
                .session(session)
                .sender(MessageSender.AI)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(aiMessage);
        session.addMessage(savedMessage);
        log.info("AI 메시지 저장 완료: messageId={}", savedMessage.getId());
        return savedMessage;
    }

    /**
     * 메시지 수정
     * 권한 확인 및 비즈니스 로직 검증 포함
     *
     * @param messageId 메시지 ID
     * @param user 사용자 엔티티
     * @param newMessage 수정할 메시지 내용
     * @return 수정된 메시지 정보
     * @throws ResourceNotFoundException 메시지를 찾을 수 없는 경우
     * @throws AccessDeniedException 권한이 없는 경우
     * @throws IllegalArgumentException AI 메시지는 수정할 수 없는 경우
     * @since 2025-12-16
     */
    @Transactional
    public ChatMessageItem updateMessage(UUID messageId, User user, String newMessage) {
        log.info("메시지 수정 요청: messageId={}, userId={}", messageId, user.getId());

        ChatMessage message = findById(messageId);
        validateMessageUpdatePermission(message, user, messageId);
        validateUserMessage(message, messageId);

        message.setMessage(newMessage);
        chatMessageRepository.save(message);
        log.info("메시지 수정 완료: messageId={}", messageId);

        return buildChatMessageItem(message);
    }

    /**
     * 메시지 수정 권한을 검증합니다.
     *
     * @param message 메시지 엔티티
     * @param user 사용자 엔티티
     * @param messageId 메시지 ID (로깅용)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    private void validateMessageUpdatePermission(ChatMessage message, User user, UUID messageId) {
        ChatSession session = message.getSession();
        if (!session.getUser().getId().equals(user.getId())) {
            log.warn("메시지 수정 권한이 없습니다: messageId={}, userId={}, sessionUserId={}",
                    messageId, user.getId(), session.getUser().getId());
            throw new AccessDeniedException(
                    com.aimix_aimixapi.chat.message.ChatMessage.MESSAGE_UPDATE_ACCESS_DENIED.getMessage());
        }
    }

    /**
     * 사용자 메시지인지 확인합니다.
     *
     * @param message 메시지 엔티티
     * @param messageId 메시지 ID (로깅용)
     * @throws IllegalArgumentException AI 메시지인 경우
     */
    private void validateUserMessage(ChatMessage message, UUID messageId) {
        if (message.getSender() != MessageSender.USER) {
            log.warn("AI 메시지는 수정할 수 없습니다: messageId={}, sender={}", messageId, message.getSender());
            throw new IllegalArgumentException(
                    com.aimix_aimixapi.chat.message.ChatMessage.AI_MESSAGE_CANNOT_BE_MODIFIED.getMessage());
        }
    }

    /**
     * ChatMessageItem을 생성합니다.
     *
     * @param message 메시지 엔티티
     * @return ChatMessageItem
     */
    private ChatMessageItem buildChatMessageItem(ChatMessage message) {
        return ChatMessageItem.builder()
                .messageId(message.getId())
                .sender(message.getSender())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * 메시지 삭제
     * 권한 확인 및 비즈니스 로직 검증 포함
     *
     * @param messageId 메시지 ID
     * @param user 사용자 엔티티
     * @throws ResourceNotFoundException 메시지를 찾을 수 없는 경우
     * @throws AccessDeniedException 권한이 없는 경우
     * @throws IllegalArgumentException AI 메시지는 삭제할 수 없는 경우
     * @since 2025-12-16
     */
    @Transactional
    public void deleteMessage(UUID messageId, User user) {
        log.info("메시지 삭제 요청: messageId={}, userId={}", messageId, user.getId());

        ChatMessage message = findById(messageId);
        validateMessageDeletePermission(message, user, messageId);
        validateUserMessageForDeletion(message, messageId);

        chatMessageRepository.delete(message);
        log.info("메시지 삭제 완료: messageId={}", messageId);
    }

    /**
     * 메시지 삭제 권한을 검증합니다.
     *
     * @param message 메시지 엔티티
     * @param user 사용자 엔티티
     * @param messageId 메시지 ID (로깅용)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    private void validateMessageDeletePermission(ChatMessage message, User user, UUID messageId) {
        ChatSession session = message.getSession();
        if (!session.getUser().getId().equals(user.getId())) {
            log.warn("메시지 삭제 권한이 없습니다: messageId={}, userId={}, sessionUserId={}",
                    messageId, user.getId(), session.getUser().getId());
            throw new AccessDeniedException(
                    com.aimix_aimixapi.chat.message.ChatMessage.MESSAGE_DELETE_ACCESS_DENIED.getMessage());
        }
    }

    /**
     * 사용자 메시지인지 확인합니다 (삭제용).
     *
     * @param message 메시지 엔티티
     * @param messageId 메시지 ID (로깅용)
     * @throws IllegalArgumentException AI 메시지인 경우
     */
    private void validateUserMessageForDeletion(ChatMessage message, UUID messageId) {
        if (message.getSender() != MessageSender.USER) {
            log.warn("AI 메시지는 삭제할 수 없습니다: messageId={}, sender={}", messageId, message.getSender());
            throw new IllegalArgumentException(
                    com.aimix_aimixapi.chat.message.ChatMessage.AI_MESSAGE_CANNOT_BE_DELETED.getMessage());
        }
    }
}
