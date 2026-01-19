package com.aimix_aimixapi.knowledge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Knowledge Card 서비스 관련 설정
 * knowledge-prompts.yml에서 설정값을 주입받음
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeProperties {

    /**
     * GPT 카드 생성 프롬프트 설정
     * knowledge-prompts.yml에서 설정
     */
    private GptPrompt gptPrompt = new GptPrompt();

    @Getter
    @Setter
    public static class GptPrompt {
        /**
         * 시스템 메시지 (카드 생성 지침)
         * knowledge-prompts.yml에서 설정
         */
        private String systemMessage;

        /**
         * 사용자 메시지 템플릿
         * knowledge-prompts.yml에서 설정
         */
        private String userMessageTemplate;

        /**
         * 콘텐츠 라벨 설정
         * knowledge-prompts.yml에서 설정
         */
        private ContentLabel contentLabel = new ContentLabel();

        /**
         * 기본값 설정
         * knowledge-prompts.yml에서 설정
         */
        private DefaultValues defaultValues = new DefaultValues();

        @Getter
        @Setter
        public static class ContentLabel {
            /**
             * 채팅 대화 내용 라벨
             * knowledge-prompts.yml에서 설정
             */
            private String chat;

            /**
             * QnA 질문과 답변 라벨
             * knowledge-prompts.yml에서 설정
             */
            private String qna;
        }

        @Getter
        @Setter
        public static class DefaultValues {
            /**
             * 기본 제목
             * knowledge-prompts.yml에서 설정
             */
            private String title;

            /**
             * 기본 한 줄 정의
             * knowledge-prompts.yml에서 설정
             */
            private String oneLineDefinition;

            /**
             * 기본 핵심 포인트
             * knowledge-prompts.yml에서 설정
             */
            private String corePoint;
        }
    }
}
