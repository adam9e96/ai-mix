package com.aimix_aimixapi.qna.mapper;

import com.aimix_aimixapi.qna.config.QnaProperties;
import com.aimix_aimixapi.qna.dto.qna.QnaAnswerResponse;
import com.aimix_aimixapi.qna.dto.qna.QnaQuestionListResponse;
import com.aimix_aimixapi.qna.dto.qna.QnaQuestionResponse;
import com.aimix_aimixapi.qna.entity.AnswerType;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QNA 엔티티와 DTO 간 변환을 담당하는 매퍼
 * MapStruct 대신 수동 매핑을 사용하여 복잡한 변환 로직 처리
 */
@Component
@RequiredArgsConstructor
public class QnaMapper {

    private final QnaProperties qnaProperties;

    /**
     * QnaQuestion을 QnaQuestionResponse로 변환
     */
    public QnaQuestionResponse toQuestionResponse(QnaQuestion question) {
        List<String> tags = extractTags(question);
        String authorNickname = extractAuthorNickname(question);
        String authorAvatarUrl = extractAuthorAvatarUrl(question);

        return QnaQuestionResponse.builder()
                .id(question.getId())
                .authorNickname(authorNickname)
                .authorAvatarUrl(authorAvatarUrl)
                .title(question.getTitle())
                .body(question.getBody())
                .isAnonymous(question.getIsAnonymous())
                .createdAt(question.getCreatedAt())
                .tags(tags)
                .answerCount(question.getAnswers().size())
                .viewCount(getViewCount(question))
                .build();
    }

    /**
     * QnaQuestion 엔티티를 QnaQuestionListResponse DTO로 변환
     */
    public QnaQuestionListResponse toQuestionListResponse(QnaQuestion question) {
        List<String> tags = extractTags(question);
        String authorNickname = extractAuthorNickname(question);
        String bodyPreview = createBodyPreview(question.getBody());

        return QnaQuestionListResponse.builder()
                .id(question.getId())
                .authorNickname(authorNickname)
                .title(question.getTitle())
                .bodyPreview(bodyPreview)
                .isAnonymous(question.getIsAnonymous())
                .createdAt(question.getCreatedAt())
                .tags(tags)
                .answerCount(question.getAnswers().size())
                .viewCount(getViewCount(question))
                .build();
    }

    /**
     * QnaAnswer를 QnaAnswerResponse로 변환
     */
    public QnaAnswerResponse toAnswerResponse(QnaAnswer answer) {
        String authorNickname = extractAnswerAuthorNickname(answer);
        String authorAvatarUrl = extractAnswerAuthorAvatarUrl(answer);

        return QnaAnswerResponse.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .authorNickname(authorNickname)
                .authorAvatarUrl(authorAvatarUrl)
                .answerType(answer.getAnswerType().name())
                .body(answer.getBody())
                .score(answer.getScore())
                .isAccepted(answer.getIsAccepted())
                .createdAt(answer.getCreatedAt())
                .build();
    }

    private List<String> extractTags(QnaQuestion question) {
        if (question.getQuestionTags() == null || question.getQuestionTags().isEmpty()) {
            return new ArrayList<>();
        }
        return question.getQuestionTags().stream()
                .map(qt -> qt.getTag().getName())
                .collect(Collectors.toList());
    }

    private String extractAuthorNickname(QnaQuestion question) {
        if (isAnonymousOrNoUser(question)) {
            return null;
        }
        return question.getUser().getNickname();
    }

    private String extractAnswerAuthorNickname(QnaAnswer answer) {
        return switch (answer.getAnswerType()) {
            case AI -> "AI";
            case USER -> answer.getUser() != null ? answer.getUser().getNickname() : null;
        };
    }

    /**
     * 질문 작성자의 아바타 URL 추출
     * - 작성자가 유저이고 프로필이 있을 경우에만 아바타 URL 반환
     * - 익명 질문이거나 프로필이 없으면 null
     */
    private String extractAuthorAvatarUrl(QnaQuestion question) {
        if (isAnonymousOrNoUser(question)) {
            return null;
        }
        if (question.getUser().getUserProfile() != null) {
            return question.getUser().getUserProfile().getAvatarUrl();
        }
        return null;
    }

    /**
     * 답변 작성자의 아바타 URL 추출
     * - 작성자가 유저이고 프로필이 있을 경우에만 아바타 URL 반환
     * - AI 답변이거나 프로필이 없으면 null
     */
    private String extractAnswerAuthorAvatarUrl(QnaAnswer answer) {
        return switch (answer.getAnswerType()) {
            case AI -> null;
            case USER -> answer.getUser() != null && answer.getUser().getUserProfile() != null
                    ? answer.getUser().getUserProfile().getAvatarUrl()
                    : null;
        };
    }

    private String createBodyPreview(String body) {
        if (body == null) {
            return null;
        }
        int maxLength = qnaProperties.getBodyPreviewMaxLength();
        if (body.length() <= maxLength) {
            return body;
        }
        return body.substring(0, maxLength) + qnaProperties.getBodyPreviewEllipsis();
    }

    private Long getViewCount(QnaQuestion question) {
        return question.getViewCount() != null ? question.getViewCount() : 0L;
    }

    private boolean isAnonymousOrNoUser(QnaQuestion question) {
        return Boolean.TRUE.equals(question.getIsAnonymous()) || question.getUser() == null;
    }
}
