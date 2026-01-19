package com.aimix_aimixapi.chat.entity;

import com.aimix_aimixapi.common.uuid.UuidV7;
import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 채팅 세션 엔티티
 * 사용자의 채팅 대화 세션을 관리
 */
@Entity
@ToString(exclude = {"user", "messages"})
@Table(name = "chat_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    /**
     * 기본 키 (UUID v7)
     * uuid-creator 라이브러리를 사용하여 타임스탬프 기반 UUID v7 생성
     * 정렬 가능하고 인덱스 성능이 더 좋음
     */
    @Id
    @UuidV7
    private UUID id;

    /**
     * 사용자와의 다대일 관계
     * chat_session.user_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 채팅 세션 제목(주제)
     */
    @Column(nullable = false)
    private String title;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * ChatSession 기준 - ChatMessage와 1:N 연관관계
     * 하나의 세션에 여러 메시지가 포함됨
     * cascade = CascadeType.ALL → 세션 삭제 시 메시지도 함께 삭제
     * orphanRemoval = true → 메시지 참조 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * ChatMessage를 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setSession(this);
    }

    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setSession(null);
    }
}

