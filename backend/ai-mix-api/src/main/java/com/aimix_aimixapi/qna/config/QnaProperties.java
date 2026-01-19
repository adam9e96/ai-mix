package com.aimix_aimixapi.qna.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * QNA 서비스 관련 설정
 * qna-prompts.yml에서 설정값을 주입받음
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "qna")
public class QnaProperties {

    /**
     * 질문 목록 본문 미리보기 최대 길이 (자)
     * qna-prompts.yml에서 설정
     */
    private int bodyPreviewMaxLength;

    /**
     * 본문 미리보기 생략 표시
     * qna-prompts.yml에서 설정
     */
    private String bodyPreviewEllipsis;

    /**
     * GPT 답변 생성 프롬프트 설정
     * qna-prompts.yml에서 설정
     */
    private GptPrompt gptPrompt = new GptPrompt();

    /**
     * 배틀 데이터 수집 프롬프트 설정
     * qna-prompts.yml에서 설정
     */
    private BattleDataPrompt battleDataPrompt = new BattleDataPrompt();

    /**
     * 태그 생성 프롬프트 설정
     * qna-prompts.yml에서 설정
     */
    private TagPrompt tagPrompt = new TagPrompt();

    @Getter
    @Setter
    public static class GptPrompt {
        /**
         * 프롬프트 시작 문구
         * qna-prompts.yml에서 설정
         */
        private String introduction;

        /**
         * 질문 제목 라벨
         * qna-prompts.yml에서 설정
         */
        private String questionTitleLabel;

        /**
         * 질문 내용 라벨
         * qna-prompts.yml에서 설정
         */
        private String questionBodyLabel;

        /**
         * 기존 답변 참고 문구
         * qna-prompts.yml에서 설정
         */
        private String existingAnswersLabel;

        /**
         * 기존 답변 참고 후 지시사항
         * qna-prompts.yml에서 설정
         */
        private String existingAnswersInstruction;

        /**
         * 답변 작성 가이드라인
         * qna-prompts.yml에서 설정
         */
        private String guidelines;

        /**
         * 마크다운 형식 지시사항
         * qna-prompts.yml에서 설정
         */
        private String markdownFormat;

        /**
         * 답변 시작 라벨
         * qna-prompts.yml에서 설정
         */
        private String answerLabel;
    }

    @Getter
    @Setter
    public static class BattleDataPrompt {
        /**
         * 질문 제목 라벨
         * qna-prompts.yml에서 설정
         */
        private String questionTitleLabel;

        /**
         * 질문 내용 라벨
         * qna-prompts.yml에서 설정
         */
        private String questionBodyLabel;

        /**
         * 선택된 사용자 답변 라벨
         * qna-prompts.yml에서 설정
         */
        private String selectedUserAnswerLabel;

        /**
         * 채택됨 표시
         * qna-prompts.yml에서 설정
         */
        private String acceptedLabel;

        /**
         * 점수 및 추천 표시 포맷
         * qna-prompts.yml에서 설정
         */
        private String scoreFormat;

        /**
         * 사용자 답변 보완 지시사항
         * qna-prompts.yml에서 설정
         */
        private String userAnswerSupplementInstruction;

        /**
         * GPT 답변 라벨
         * qna-prompts.yml에서 설정
         */
        private String gptAnswerLabel;
    }

    @Getter
    @Setter
    public static class TagPrompt {
        /**
         * 태그 생성 프롬프트 시작 문구
         * qna-prompts.yml에서 설정
         */
        private String introduction;

        /**
         * 제목 라벨
         * qna-prompts.yml에서 설정
         */
        private String titleLabel;

        /**
         * 내용 라벨
         * qna-prompts.yml에서 설정
         */
        private String bodyLabel;

        /**
         * 태그 추천 조건
         * qna-prompts.yml에서 설정
         */
        private String conditions;

        /**
         * 응답 형식 지시사항
         * qna-prompts.yml에서 설정
         */
        private String responseFormat;

        /**
         * 최종 지시사항
         * qna-prompts.yml에서 설정
         */
        private String instruction;
    }
}
