package com.aimix_aimixapi.chat.service;

import com.aimix_aimixapi.chat.dto.*;
import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.entity.ChatSession;
import com.aimix_aimixapi.chat.entity.MessageSender;
import com.aimix_aimixapi.chat.mapper.ChatMapper;
import com.aimix_aimixapi.gpt.dto.GptMessage;
import com.aimix_aimixapi.gpt.dto.GptMessageRole;
import com.aimix_aimixapi.gpt.service.GptService;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 채팅 서비스
 * - ChatGPT API 호출 및 응답 처리
 * - 채팅 세션 및 메시지 DB 저장
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final GptService gptService;
    private final ChatMapper chatMapper;

    /**
     * 채팅 질문 처리 및 답변 반환
     * 1. 사용자 질문을 DB에 저장
     * 2. ChatGPT API 호출하여 답변 생성
     * 3. AI 답변을 DB에 저장
     * 4. 답변 반환
     *
     * @param email   사용자 이메일
     * @param request 채팅 요청 (세션 ID, 메시지, 제목)
     * @return 채팅 응답 (세션 ID, 답변, 생성 시각)
     */
    @Transactional
    public ChatResponse processChatMessage(String email, ChatRequest request) {
        log.info("채팅 요청: email={}, sessionId={}, message={}, title={}",
                email, request.getSessionId(), request.getMessage(), request.getTitle());

        User user = findUserByEmail(email);
        ChatSession session = getOrCreateSession(user, request);

        if (isSessionOnlyCreation(request)) {
            return createSessionOnlyResponse(session);
        }

        return processMessageWithAiResponse(user, session, request.getMessage());
    }

    /**
     * 사용자를 이메일로 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     */
    private User findUserByEmail(String email) {
        return userService.findUserByEmail(email);
    }

    /**
     * 세션을 조회하거나 생성합니다.
     *
     * @param user 사용자 엔티티
     * @param request 채팅 요청
     * @return 채팅 세션 엔티티
     */
    private ChatSession getOrCreateSession(User user, ChatRequest request) {
        return chatSessionService.getOrCreateSession(user, request.getSessionId(), request.getTitle(), request.getMessage());
    }

    /**
     * 세션만 생성하는 경우인지 확인합니다.
     *
     * @param request 채팅 요청
     * @return 메시지가 없으면 true
     */
    private boolean isSessionOnlyCreation(ChatRequest request) {
        return !StringUtils.hasText(request.getMessage());
    }

    /**
     * 세션만 생성한 경우의 응답을 생성합니다.
     *
     * @param session 채팅 세션 엔티티
     * @return 채팅 응답
     */
    private ChatResponse createSessionOnlyResponse(ChatSession session) {
        log.info("메시지가 없어 세션(챗봇 채팅방만 생성: sessionId={}, title={}", session.getId(), session.getTitle());
        return chatMapper.toChatSessionOnly(session);
    }

    /**
     * 메시지와 AI 응답을 처리합니다.
     *
     * @param user 사용자 엔티티
     * @param session 채팅 세션 엔티티
     * @param userMessage 사용자 메시지
     * @return 채팅 응답
     */
    private ChatResponse processMessageWithAiResponse(User user, ChatSession session, String userMessage) {
        chatMessageService.saveUserMessage(session, userMessage);

        List<ChatMessage> previousMessages = chatMessageService.findBySessionOrderByCreatedAtAsc(session);
        log.info("조회된 이전 메시지 개수: {}", previousMessages.size());

        List<GptMessage> gptMessages = convertToGptMessages(previousMessages);
        log.info("변환된 GPT 메시지 개수: {}", gptMessages.size());

        String aiResponse = callGptApi(user, gptMessages);
        ChatMessage aiMessage = chatMessageService.saveAiMessage(session, aiResponse);

        return chatMapper.toChatResponse(session, aiResponse, aiMessage);
    }

    /**
     * GPT API를 호출합니다.
     *
     * @param user 사용자 엔티티
     * @param gptMessages GPT 메시지 목록
     * @return AI 응답
     */
    private String callGptApi(User user, List<GptMessage> gptMessages) {
        return gptService.callGptApiWithMessages(user, gptMessages, null, null, com.aimix_aimixapi.gpt.token.entity.GptUsageType.CHAT);
    }


    /**
     * ChatMessage 리스트를 GptMessage 리스트로 변환
     *
     * @param chatMessages 채팅 메시지 목록
     * @return GPT 메시지 목록
     * @since 2025-12-15
     */
    private List<GptMessage> convertToGptMessages(List<ChatMessage> chatMessages) {
        return chatMessages.stream()
                .map(this::convertToGptMessage)
                .toList();
    }

    /**
     * ChatMessage를 GptMessage로 변환합니다.
     *
     * @param chatMessage 채팅 메시지
     * @return GPT 메시지
     */
    private GptMessage convertToGptMessage(ChatMessage chatMessage) {
        return GptMessage.builder()
                .role(mapToGptMessageRole(chatMessage.getSender()))
                .content(chatMessage.getMessage())
                .build();
    }

    /**
     * MessageSender를 GptMessageRole로 매핑합니다.
     *
     * @param sender 메시지 발신자
     * @return GPT 메시지 역할
     */
    private GptMessageRole mapToGptMessageRole(MessageSender sender) {
        return sender == MessageSender.USER ? GptMessageRole.USER : GptMessageRole.AI;
    }

    /**
     * 사용자의 채팅방 목록 조회
     *
     * @param email 사용자 이메일
     * @return 채팅 세션 목록 (생성일 내림차순)
     * @since 2025-12-15
     */
    @Transactional(readOnly = true)
    public ChatSessionListResponse getSessions(String email) {
        log.info("채팅방 목록 조회 요청: email={}", email);

        User user = findUserByEmail(email);
        List<ChatSession> sessions = chatSessionService.findByUserOrderByCreatedAtDesc(user);
        log.info("조회된 채팅방 개수: {}", sessions.size());

        List<ChatSessionItem> sessionItems = convertToSessionItems(sessions);
        return buildSessionListResponse(sessionItems);
    }

    /**
     * 세션 목록을 ChatSessionItem 목록으로 변환합니다.
     *
     * @param sessions 세션 목록
     * @return ChatSessionItem 목록
     */
    private List<ChatSessionItem> convertToSessionItems(List<ChatSession> sessions) {
        return sessions.stream()
                .map(this::convertToSessionItem)
                .toList();
    }

    /**
     * 세션을 ChatSessionItem으로 변환합니다.
     *
     * @param session 세션 엔티티
     * @return ChatSessionItem
     */
    private ChatSessionItem convertToSessionItem(ChatSession session) {
        long messageCount = chatMessageService.countBySessionId(session.getId());
        LocalDateTime lastMessageAt = chatMessageService.getLastMessageAtBySessionId(session.getId());
        return chatMapper.toChatSessionItem(session, messageCount, lastMessageAt);
    }

    /**
     * 세션 목록 응답을 생성합니다.
     *
     * @param sessionItems 세션 아이템 목록
     * @return ChatSessionListResponse
     */
    private ChatSessionListResponse buildSessionListResponse(List<ChatSessionItem> sessionItems) {
        return ChatSessionListResponse.builder()
                .sessions(sessionItems)
                .totalCount((long) sessionItems.size())
                .build();
    }

    /**
     * 특정 세션의 채팅 메시지 목록 조회
     *
     * @param email     사용자 이메일
     * @param sessionId 세션 ID
     * @return 채팅 메시지 목록 (생성일 오름차순)
     */
    @Transactional(readOnly = true)
    public ChatMessageListResponse getMessages(String email, UUID sessionId) {
        log.info("채팅 메시지 목록 조회 요청: email={}, sessionId={}", email, sessionId);

        User user = findUserByEmail(email);
        ChatSession session = chatSessionService.findByIdWithAuthCheck(sessionId, user);

        List<ChatMessage> messages = chatMessageService.findBySessionIdOrderByCreatedAtAsc(sessionId);
        log.info("조회된 메시지 개수: {}", messages.size());

        List<ChatMessageItem> messageItems = convertToMessageItems(messages);
        return buildMessageListResponse(session, messageItems);
    }

    /**
     * 메시지 목록을 ChatMessageItem 목록으로 변환합니다.
     *
     * @param messages 메시지 목록
     * @return ChatMessageItem 목록
     */
    private List<ChatMessageItem> convertToMessageItems(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::convertToMessageItem)
                .toList();
    }

    /**
     * 메시지를 ChatMessageItem으로 변환합니다.
     *
     * @param message 메시지 엔티티
     * @return ChatMessageItem
     */
    private ChatMessageItem convertToMessageItem(ChatMessage message) {
        return ChatMessageItem.builder()
                .messageId(message.getId())
                .sender(message.getSender())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * 메시지 목록 응답을 생성합니다.
     *
     * @param session 세션 엔티티
     * @param messageItems 메시지 아이템 목록
     * @return ChatMessageListResponse
     */
    private ChatMessageListResponse buildMessageListResponse(ChatSession session, List<ChatMessageItem> messageItems) {
        return ChatMessageListResponse.builder()
                .sessionId(session.getId())
                .title(session.getTitle())
                .messages(messageItems)
                .totalCount((long) messageItems.size())
                .build();
    }


    /**
     * 메시지 수정
     *
     * @param email     사용자 이메일
     * @param messageId 메시지 ID
     * @param request   메시지 수정 요청
     * @return 수정된 메시지 정보
     */
    @Transactional
    public ChatMessageItem updateMessage(String email, UUID messageId, UpdateMessageRequest request) {
        log.info("메시지 수정 요청: email={}, messageId={}", email, messageId);

        User user = findUserByEmail(email);
        return chatMessageService.updateMessage(messageId, user, request.getMessage());
    }

    /**
     * 채팅방 제목 수정
     * 사용자와 세션을 조회하고 제목업데이트
     *
     * @param email     사용자 이메일
     * @param sessionId 세션 ID
     * @param request   채팅방 제목 수정 요청
     * @return 수정된 세션 정보
     */
    @Transactional
    public ChatSessionItem updateSessionTitle(String email, UUID sessionId, UpdateSessionTitleRequest request) {
        log.info("채팅방 제목 수정 요청: email={}, sessionId={}, title={}", email, sessionId, request.getTitle());

        User user = findUserByEmail(email);
        return chatSessionService.updateTitle(sessionId, user, request.getTitle());
    }

    /**
     * 메시지 삭제
     *
     * @param email     사용자 이메일
     * @param messageId 메시지 ID
     */
    @Transactional
    public void deleteMessage(String email, UUID messageId) {
        log.info("메시지 삭제 요청: email={}, messageId={}", email, messageId);

        User user = findUserByEmail(email);
        chatMessageService.deleteMessage(messageId, user);
    }

    /**
     * 채팅방 삭제
     * 세션 삭제 시 cascade 설정으로 인해 관련된 모든 메시지도 자동 삭제됨
     *
     * @param email     사용자 이메일
     * @param sessionId 세션 ID
     * @since 2025-12-16
     */
    @Transactional
    public void deleteSession(String email, UUID sessionId) {
        log.info("채팅방 삭제 요청: email={}, sessionId={}", email, sessionId);

        User user = findUserByEmail(email);
        chatSessionService.deleteSession(sessionId, user);
    }
}

