package com.aimix_aimixapi.chat.controller;

import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.chat.dto.ChatRequest;
import com.aimix_aimixapi.chat.dto.ChatResponse;
import com.aimix_aimixapi.chat.dto.ChatSessionListResponse;
import com.aimix_aimixapi.chat.dto.ChatMessageListResponse;
import com.aimix_aimixapi.chat.dto.ChatMessageItem;
import com.aimix_aimixapi.chat.dto.ChatSessionItem;
import com.aimix_aimixapi.chat.dto.UpdateMessageRequest;
import com.aimix_aimixapi.chat.dto.UpdateSessionTitleRequest;
import com.aimix_aimixapi.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 채팅 컨트롤러
 * - 채팅 질문 및 답변 API
 * - JWT 토큰 인증 필요
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅 질문 전송 및 답변 받기
     * POST /api/v1/chat
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request     채팅 요청 (세션 ID, 메시지, 제목)
     * @return 채팅 응답 (세션 ID, 답변, 생성 시각)
     * @AuthenticationPrincipal을 사용하여 JWT 토큰에서 인증된 사용자 정보를 자동으로 주입받음
     * 인증되지 않은 경우 userDetails는 null이 되며, SecurityConfig에서 이미 인증을 요구하므로
     * 이 메서드에 도달했다는 것은 인증이 성공했다는 의미
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> sendMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChatRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("채팅 요청 수신: email={}, sessionId={}", email, request.getSessionId());

        ChatResponse response = chatService.processChatMessage(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 채팅방 목록 조회
     * GET /api/v1/chat/sessions
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return 채팅 세션 목록 (생성일 내림차순)
     */
    @GetMapping("/sessions")
    public ResponseEntity<ChatSessionListResponse> getSessions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String email = userDetails.getUser().getEmail();
        log.info("채팅방 목록 조회 요청: email={}", email);

        ChatSessionListResponse response = chatService.getSessions(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 채팅방의 대화 기록 조회
     * GET /api/v1/chat/{sessionId}
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param sessionId   세션 ID (URL 경로 변수)
     * @return 채팅 메시지 목록 (USER와 AI의 대화 기록, 생성일 오름차순)
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ChatMessageListResponse> getChatHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID sessionId) {

        String email = userDetails.getUser().getEmail();
        log.info("채팅방 기록 조회 요청: email={}, sessionId={}", email, sessionId);

        ChatMessageListResponse response = chatService.getMessages(email, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 세션의 채팅 메시지 목록 조회
     * GET /api/v1/chat/sessions/{sessionId}/messages
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param sessionId   세션 ID (경로 변수)
     * @return 채팅 메시지 목록 (생성일 오름차순)
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageListResponse> getMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID sessionId) {

        String email = userDetails.getUser().getEmail();
        log.info("채팅 메시지 목록 조회 요청: email={}, sessionId={}", email, sessionId);

        ChatMessageListResponse response = chatService.getMessages(email, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 메시지 수정
     * PUT /api/v1/chat/messages/{messageId}
     * Body: { "message": "수정된 메시지 내용" }
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param messageId   메시지 ID (경로 변수)
     * @param request     메시지 수정 요청
     * @return 수정된 메시지 정보
     */
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageItem> updateMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("메시지 수정 요청: email={}, messageId={}", email, messageId);

        ChatMessageItem response = chatService.updateMessage(email, messageId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 제목 수정
     * PUT /api/v1/chat/sessions/{sessionId}
     * Body: { "title": "새 제목" }
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param sessionId   세션 ID (경로 변수)
     * @param request     채팅방 제목 수정 요청
     * @return 수정된 세션 정보
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionItem> updateSessionTitle(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSessionTitleRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("채팅방 제목 수정 요청: email={}, sessionId={}", email, sessionId);

        ChatSessionItem response = chatService.updateSessionTitle(email, sessionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 삭제
     * DELETE /api/v1/chat/sessions/{sessionId}
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param sessionId   세션 ID (경로 변수)
     * @return 204 No Content
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID sessionId) {
        String email = userDetails.getUser().getEmail();
        log.debug("채팅방 삭제 요청: email={}, sessionId={}", email, sessionId);
        chatService.deleteSession(email, sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 메시지 삭제
     * DELETE /api/v1/chat/messages/{messageId}
     * 
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param messageId   메시지 ID (경로 변수)
     * @return 204 No Content
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID messageId) {

        String email = userDetails.getUser().getEmail();
        log.info("메시지 삭제 요청: email={}, messageId={}", email, messageId);

        chatService.deleteMessage(email, messageId);
        return ResponseEntity.noContent().build();
    }
}

