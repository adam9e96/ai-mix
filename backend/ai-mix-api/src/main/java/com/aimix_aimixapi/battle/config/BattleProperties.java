package com.aimix_aimixapi.battle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 배틀 서비스 관련 설정
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "battle")
public class BattleProperties {

  /**
   * 정답으로 인정되는 최소 점수 (0 ~ 100)
   * battle-prompts.yml에서 설정
   */
  private int correctScoreThreshold;

  /**
   * 승리 판정 기준 정답률 (%)
   * battle-prompts.yml에서 설정
   */
  private double winRateThreshold;

  /**
   * 무승부 판정 기준 정답률 (%)
   * battle-prompts.yml에서 설정
   */
  private double drawRateThreshold;

  /**
   * 기본 문제 생성 개수
   * battle-prompts.yml에서 설정
   */
  private int defaultQuestionCount;

  /**
   * 문제 생성 시 최대 토큰 수
   * battle-prompts.yml에서 설정
   */
  private int questionGenerationMaxTokens;

  /**
   * 문제 생성 시 Temperature
   * battle-prompts.yml에서 설정
   */
  private double questionGenerationTemperature;

  /**
   * 문제 생성 프롬프트 설정
   */
  private QuestionGeneration questionGeneration = new QuestionGeneration();

  /**
   * 채점 프롬프트 설정
   */
  private Scoring scoring = new Scoring();

  /**
   * 평가 메시지 설정
   */
  private Evaluation evaluation = new Evaluation();

  @Getter
  @Setter
  public static class QuestionGeneration {
    /**
     * 문제 생성 시스템 메시지 템플릿
     * %s: 질문 타입 설명 (객관식/주관식)
     * %s: JSON 포맷 예시
     * %s: 타입별 요구사항
     */
    private String systemMessageTemplate = "";

    /**
     * 객관식 JSON 포맷 예시
     */
    private String objectiveJsonFormat = "";

    /**
     * 주관식 JSON 포맷 예시
     */
    private String subjectiveJsonFormat = "";

    /**
     * 객관식 문제 생성 요구사항
     */
    private String objectiveRequirements = "";

    /**
     * 주관식 문제 생성 요구사항
     */
    private String subjectiveRequirements = "";
  }

  @Getter
  @Setter
  public static class Scoring {
    /**
     * 주관식 채점 프롬프트
     * %s: 문제, %s: 정답, %s: 사용자 답변
     */
    private String subjectivePrompt = "";
  }

  @Getter
  @Setter
  public static class Evaluation {
    /**
     * 등급별 설명
     */
    private java.util.Map<String, String> gradeDescriptions = new java.util.HashMap<>();

    /**
     * 평가 코멘트
     */
    private String commentHigh = "";
    private String commentGood = "";
    private String commentFair = "";
    private String commentLow = "";
    private String commentFormat = "";

    /**
     * 추천 메시지
     */
    private String recommendationHigh = "";
    private String recommendationMedium = "";
    private String recommendationPerfect = "";
    private String recommendationNextLevel = "";
    private String recommendationBasic = "";
  }
}
