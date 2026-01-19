package com.aimix_aimixapi.battle.controller;

import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.battle.dto.*;
import com.aimix_aimixapi.battle.service.BattleCommandService;
import com.aimix_aimixapi.battle.service.BattleQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 배틀 컨트롤러
 * - 배틀 생성 및 관리 API
 * - JWT 토큰 인증 필요
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleCommandService battleCommandService;
    private final BattleQueryService battleQueryService;

    /**
     * 배틀 생성
     * POST /api/v1/battle/create
     * <p>
     * 채팅 세션의 대화 내용을 분석하여 문제를 생성하고 배틀을 만듬
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request     배틀 생성 요청 (세션 ID)
     * @return 배틀 생성 응답 (배틀 ID, 문제 목록 등)
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BattleCreateResponse> createBattle(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody BattleCreateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 생성 요청 수신: email={}, sourceType={}, id={}, questionType={}",
                email, request.getSourceType(), request.getId(), request.getQuestionType());

        BattleCreateResponse response = battleCommandService.createBattle(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 답변 제출 및 채점
     * POST /api/v1/battle/submit-answer
     * <p>
     * 사용자가 제출한 답변을 GPT로 채점하고 점수와 피드백을 반환
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param request     답변 제출 요청 (배틀 ID, 문제 ID, 사용자 답변)
     * @return 답변 제출 응답 (점수, 피드백, 정답 여부 등)
     */
    @PostMapping(value = "/submit-answer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BattleAnswerSubmitResponse> submitAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody BattleAnswerSubmitRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 답변 제출 요청: email={}, battleId={}, questionId={}",
                email, request.getBattleId(), request.getQuestionId());

        BattleAnswerSubmitResponse response = battleCommandService.submitAnswer(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 목록 조회
     * GET /api/v1/battle/list
     * <p>
     * 사용자가 생성한 모든 배틀 목록을 조회합니다.
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return 배틀 목록 응답 (배틀 정보, 진행 상황 등)
     */
    @GetMapping("/list")
    public ResponseEntity<BattleListResponse> getBattleList(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 목록 조회 요청: email={}", email);

        BattleListResponse response = battleQueryService.getBattleList(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 결과 조회
     * GET /api/v1/battle/{battleId}/result
     * <p>
     * 배틀의 전체 결과를 조회합니다.
     * - 전체 통계 (총점, 평균, 정답률 등)
     * - 문제별 상세 결과
     * - 종합 평가 및 피드백
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param battleId    배틀 ID
     * @return 배틀 결과 응답
     */
    @GetMapping("/{battleId}/result")
    public ResponseEntity<BattleResultResponse> getBattleResult(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID battleId) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 결과 조회 요청: email={}, battleId={}", email, battleId);

        BattleResultResponse response = battleQueryService.getBattleResult(email, battleId);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 간단 승패 결과 조회
     * GET /api/v1/battle/{battleId}/summary
     * <p>
     * Chat에서 배틀 완료 후 간단한 승패 결과를 빠르게 조회합니다.
     * - 승패 결과 (WIN, DRAW, LOSE, IN_PROGRESS)
     * - 정답률 (%)
     * - 평균 점수
     * <p>
     * 상세한 결과가 필요하면 /{battleId}/result 엔드포인트를 사용하세요.
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param battleId    배틀 ID
     * @return 배틀 간단 승패 결과 응답
     */
    @GetMapping("/{battleId}/summary")
    public ResponseEntity<BattleSimpleResultResponse> getBattleSimpleResult(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID battleId) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 간단 승패 결과 조회 요청: email={}, battleId={}", email, battleId);

        BattleSimpleResultResponse response = battleQueryService.getBattleSimpleResult(email, battleId);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 전적 조회
     * GET /api/v1/battle/history
     * <p>
     * 사용자의 모든 배틀 전적을 조회합니다.
     * 게임 전적 사이트처럼 매 전적마다 승패 정보를 포함하여 반환합니다.
     * - 전체 전적 통계 (승/무/패, 승률, 연승/연패 등)
     * - 배틀별 상세 전적 (승패 결과, 점수, 정답률 등)
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @return 배틀 전적 응답
     * @since 2025-12-12
     */
    @GetMapping("/history")
    public ResponseEntity<BattleHistoryResponse> getBattleHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 전적 조회 요청: email={}", email);

        BattleHistoryResponse response = battleQueryService.getBattleHistory(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 재도전
     * POST /api/v1/battle/{battleId}/retry
     * <p>
     * 기존 배틀의 문제들을 그대로 사용하여 재도전합니다.
     * 기존 답변들은 모두 삭제되고 처음부터 다시 시작할 수 있습니다.
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param battleId    재도전할 배틀 ID
     * @return 배틀 정보 및 문제 목록
     */
    @PostMapping("/{battleId}/retry")
    public ResponseEntity<BattleCreateResponse> retryBattle(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID battleId) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 재도전 요청: email={}, battleId={}", email, battleId);

        BattleCreateResponse response = battleCommandService.retryBattle(email, battleId);
        return ResponseEntity.ok(response);
    }

    /**
     * 배틀 재개
     * GET /api/v1/battle/{battleId}/resume
     * <p>
     * 배틀을 하다가 도중에 나가버린 경우 다시 이어서 할 수 있도록 합니다.
     * 어디까지 답변했는지 순서를 기억하고 이어서 할 수 있게 해줍니다.
     * <p>
     * 응답에는 다음 정보가 포함됩니다:
     * - 배틀 기본 정보 (ID, 난이도, 전체 문제 수 등)
     * - 각 문제의 진행 상태 (답변 여부, 답변한 경우 점수/피드백 등)
     * - 전체 진행 상황 (답변한 문제 수, 완료 여부)
     *
     * @param userDetails 인증된 사용자 정보 (JWT 토큰에서 자동 주입)
     * @param battleId    재개할 배틀 ID
     * @return 배틀 재개 응답 (배틀 정보 및 문제별 진행 상태)
     */
    @GetMapping("/{battleId}/resume")
    public ResponseEntity<BattleResumeResponse> resumeBattle(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID battleId) {

        String email = userDetails.getUser().getEmail();
        log.info("배틀 재개 요청: email={}, battleId={}", email, battleId);

        BattleResumeResponse response = battleQueryService.resumeBattle(email, battleId);
        return ResponseEntity.ok(response);
    }

}
