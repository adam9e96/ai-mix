package com.aimix_aimixapi.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")  // 추가
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User와 1:1 관계 (FK: user_id)
     * 연관관계의 주인은 UserProfile
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 자기소개 텍스트
     */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * 프로필 아바타 이미지 URL
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * PostgreSQL JSONB 매핑
     * {@code @JdbcTypeCode(SqlTypes.JSON)} → Hibernate 6.x 공식 JSON 매핑 방식
     * columnDefinition = "jsonb" → DB에서는 jsonb 타입 사용
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> settings = new HashMap<>();

    /**
     * 그래프 노드 위치 정보 (JSONB)
     * React Flow 그래프의 노드 위치를 사용자별로 저장
     * 형식: {"qna-all": {"qna-123": {"x": 100.0, "y": 200.0}}, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "graph_positions", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> graphPositions = new HashMap<>();

    /**
     * 암호화된 OpenAI API 키
     * 사용자가 자신의 API 키를 등록하면 암호화하여 저장
     */
    @Column(name = "openai_api_key", length = 500)
    private String openaiApiKey;
}