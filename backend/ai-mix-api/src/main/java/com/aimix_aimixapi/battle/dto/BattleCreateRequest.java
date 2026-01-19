package com.aimix_aimixapi.battle.dto;

import com.aimix_aimixapi.battle.entity.BattleSourceType;
import com.aimix_aimixapi.battle.entity.QuestionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 배틀 생성 요청 DTO
 * <p>
 * 배틀을 생성하기 위한 요청 정보를 담는 DTO
 * sourceType에 따라 필수 필드가 달라짐
 * 사용 예시:
 * <pre>
 * // QNA 기반 배틀 생성
 * {
 *   "sourceType": "QNA",
 *   "questionType": "SUBJECTIVE",
 *   "id": "질문 UUID"
 * }
 * 
 * // 채팅 세션 기반 배틀 생성
 * {
 *   "sourceType": "CHAT",
 *   "id": "세션 UUID",
 *   "questionType": "SUBJECTIVE"
 * }
 * </pre>
 * <p>
 * 필수 필드 규칙:
 * <ul>
 *   <li>sourceType이 QNA인 경우: id 필수</li>
 *   <li>sourceType이 CHAT인 경우: id 필수</li>
 *   <li>sourceType과 questionType은 항상 필수</li>
 * </ul>
 * <p>
 * @since 2025-01-15
 * @author AI-Mix Development Team
 */
@Getter
@Setter
public class BattleCreateRequest {
    /**
     * 배틀 출제 소스 타입
     * <p>
     * 배틀 문제를 생성할 소스의 타입을 지정합니다.
     * <ul>
     *   <li>CHAT: 채팅 세션의 대화 내용을 기반으로 문제 생성</li>
     *   <li>WIKI: 위키 문서를 기반으로 문제 생성 (별도 엔드포인트 사용)</li>
     *   <li>QNA: QnA 질문과 GPT 응답을 기반으로 문제 생성</li>
     * </ul>
     */
    @NotNull(message = "소스 타입은 필수입니다")
    private BattleSourceType sourceType;

    /**
     * 소스 ID
     * <p>
     * sourceType에 따라 의미가 달라집니다:
     * <ul>
     *   <li>CHAT: 채팅 세션 ID (필수)</li>
     *   <li>QNA: QnA 질문 ID (필수, 해당 게시글의 질문과 모든 GPT 답변을 자동으로 수집)</li>
     * </ul>
     */
    private UUID id;

    /**
     * 문제 유형
     * <p>
     * 생성할 문제의 유형을 지정합니다.
     * <ul>
     *   <li>SUBJECTIVE: 주관식 문제 (사용자가 자유롭게 답변 작성)</li>
     *   <li>OBJECTIVE: 객관식 문제 (4개의 선택지 중 하나 선택)</li>
     * </ul>
     */
    @NotNull(message = "문제 유형은 필수입니다")
    private QuestionType questionType;

}
