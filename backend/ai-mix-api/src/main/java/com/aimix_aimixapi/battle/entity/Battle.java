package com.aimix_aimixapi.battle.entity;

import com.aimix_aimixapi.common.uuid.UuidV7;
import com.aimix_aimixapi.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 배틀 엔티티
 * 사용자가 참여한 배틀 정보를 관리
 */
@Entity
@Table(name = "battle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "questions", "answers"})
public class Battle {

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
     * battle.user_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 배틀 출제 소스 타입
     * CHAT, WIKI, QNA
     * PostgreSQL battle_source_type_enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "source_type", nullable = false, columnDefinition = "battle_source_type_enum")
    private BattleSourceType sourceType;

    /**
     * 배틀 출제에 해당하는 항목의 id
     * 예: chat_session.id
     */
    @Column(name = "source_id")
    private UUID sourceId;

    /**
     * 배틀의 난이도 등급
     * 예: S, A, B, C
     */
    @Column(length = 10)
    private String level;

    /**
     * 출제된 전체 문제 수
     */
    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 0;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Battle 기준 - BattleQuestion과 1:N 연관관계
     * 하나의 배틀에 여러 문제가 포함됨
     * cascade = CascadeType.ALL → 배틀 삭제 시 문제도 함께 삭제
     * orphanRemoval = true → 문제 참조 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BattleQuestion> questions = new ArrayList<>();

    /**
     * Battle 기준 - BattleAnswer와 1:N 연관관계
     * 하나의 배틀에 여러 답변이 포함됨
     * cascade = CascadeType.ALL → 배틀 삭제 시 답변도 함께 삭제
     * orphanRemoval = true → 답변 참조 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BattleAnswer> answers = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * BattleQuestion을 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addQuestion(BattleQuestion question) {
        questions.add(question);
        question.setBattle(this);
    }

    /**
     * 연관관계 편의 메서드
     * BattleAnswer를 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addAnswer(BattleAnswer answer) {
        answers.add(answer);
        answer.setBattle(this);
    }
}
