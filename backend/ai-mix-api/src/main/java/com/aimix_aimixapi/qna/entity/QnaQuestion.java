package com.aimix_aimixapi.qna.entity;

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
 * QnA 질문 엔티티
 * 사용자가 작성한 질문 정보를 관리
 */
@Entity
@Table(name = "qna_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaQuestion {

    /**
     * 기본 키 (UUID v7)
     * uuid-creator 라이브러리를 사용하여 타임스탬프 기반 UUID v7 생성
     * 정렬 가능하고 인덱스 성능이 더 좋음
     */
    @Id
    @UuidV7
    private UUID id;

    /**
     * 질문을 작성한 사용자
     * 익명일 경우 NULL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 질문의 제목
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * 질문 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * 익명 여부
     */
    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    /**
     * 익명 게시글 비밀번호 (암호화되어 저장)
     * 익명일 경우 필수, 일반 게시글일 경우 NULL
     */
    @Column(name = "anonymous_password")
    private String anonymousPassword;

    /**
     * 질문이 작성된 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


    /**
     * 조회수
     * 기본값은 0
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * QnaQuestion 기준 - QnaAnswer와 1:N 연관관계
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaAnswer> answers = new ArrayList<>();

    /**
     * QnaQuestion 기준 - QnaQuestionTag와 1:N 연관관계
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaQuestionTag> questionTags = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * QnaAnswer를 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addAnswer(QnaAnswer answer) {
        answers.add(answer);
        answer.setQuestion(this);
    }

    /**
     * 연관관계 편의 메서드
     * QnaQuestionTag를 추가할 때 양방향 연관관계를 자동으로 설정
     */
    public void addQuestionTag(QnaQuestionTag questionTag) {
        questionTags.add(questionTag);
        questionTag.setQuestion(this);
        questionTag.setQuestionId(this.id);
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        long currentViewCount = this.viewCount != null ? this.viewCount : 0L;
        this.viewCount = currentViewCount + 1L;
    }

    /**
     * 익명 여부 확인
     */
    public boolean isAnonymous() {
        return Boolean.TRUE.equals(this.isAnonymous);
    }

    /**
     * 작성자 확인
     */
    public boolean hasAuthor() {
        return this.user != null;
    }

    /**
     * 일반 게시글 여부 확인
     */
    public boolean isNormalPost() {
        return !isAnonymous() && hasAuthor();
    }

    /**
     * 제목 업데이트
     */
    public void updateTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    /**
     * 내용 업데이트
     */
    public void updateBody(String body) {
        if (body != null && !body.isBlank()) {
            this.body = body;
        }
    }
}