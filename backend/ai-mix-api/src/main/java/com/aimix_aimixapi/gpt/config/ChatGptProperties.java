package com.aimix_aimixapi.gpt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ChatGPT API 설정 Properties
 * chatgpt-config.yml에서 설정값을 주입받음
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "chatgpt")
public class ChatGptProperties {

    /**
     * OpenAI API 키
     * 환경변수 OPENAI_API_KEY에서 주입받음
     * chatgpt-config.yml에서 설정
     */
    private String apiKey;

    /**
     * 기본 모델명
     * chatgpt-config.yml에서 설정
     */
    private String defaultModel;

    /**
     * 기본 Temperature (0.0 ~ 2.0)
     * chatgpt-config.yml에서 설정
     */
    private Double defaultTemperature;

    /**
     * 기본 Max Tokens
     * chatgpt-config.yml에서 설정
     */
    private Integer defaultMaxTokens;

    /**
     * 타임아웃 설정 (초)
     * chatgpt-config.yml에서 설정
     */
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Timeout {
        /**
         * 연결 타임아웃 (초)
         * chatgpt-config.yml에서 설정
         */
        private Integer connect;

        /**
         * 읽기 타임아웃 (초)
         * chatgpt-config.yml에서 설정
         */
        private Integer read;
    }
}
