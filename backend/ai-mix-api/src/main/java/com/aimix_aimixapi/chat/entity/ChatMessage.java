package com.aimix_aimixapi.chat.entity;

import com.aimix_aimixapi.common.uuid.UuidV7;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅 메시지 엔티티
 * 채팅 세션 내의 개별 메시지를 관리
 */
@Entity
@ToString(exclude = "session")
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    /**
     * 기본 키 (UUID v7)
     * uuid-creator 라이브러리를 사용하여 타임스탬프 기반 UUID v7 생성
     * 정렬 가능하고 인덱스 성능이 더 좋음
     */
    @Id
    @UuidV7
    private UUID id;

    /**
     * ChatSession과의 다대일 관계
     * chat_message.session_id → chat_session.id
     * 연관관계의 주인 (외래키 보유)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    /**
     * 메시지 발신자
     * USER: 사용자가 보낸 메시지
     * AI: AI(챗봇)가 보낸 메시지
     * PostgreSQL message_sender_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "message_sender_enum")
    private MessageSender sender;

    /**
     * 메시지 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}

