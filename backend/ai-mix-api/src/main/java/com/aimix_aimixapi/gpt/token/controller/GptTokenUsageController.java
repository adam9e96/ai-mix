package com.aimix_aimixapi.gpt.token.controller;

import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.gpt.token.dto.GptTokenUsageGraphResponse;
import com.aimix_aimixapi.gpt.token.dto.GptTokenUsageResponse;
import com.aimix_aimixapi.gpt.token.service.graph.GptTokenUsageGraphService;
import com.aimix_aimixapi.gpt.token.service.query.GptTokenUsageQueryService;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GPT 토큰 사용량 조회 컨트롤러
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/gpt/usage")
@RequiredArgsConstructor
public class GptTokenUsageController {

    private final GptTokenUsageQueryService queryService;
    private final GptTokenUsageGraphService graphService;
    private final UserService userService;

    /**
     * 사용자별 GPT 토큰 사용량 조회
     * GET /api/v1/gpt/usage
     *
     * @param userDetails 인증된 사용자 정보
     * @return 토큰 사용량 정보
     */
    @GetMapping
    public ResponseEntity<GptTokenUsageResponse> getTokenUsage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String email = userDetails.getUser().getEmail();
        log.info("GPT 토큰 사용량 조회 요청: email={}", email);

        User user = userService.findUserByEmail(email);
        GptTokenUsageResponse response = queryService.getTokenUsageResponse(user);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자별 GPT 토큰 사용량 그래프 데이터 조회
     * GET /api/v1/gpt/usage/graph?period=daily&days=30
     *
     * @param userDetails 인증된 사용자 정보
     * @param period      집계 기간 (daily/weekly/monthly, 기본값: daily)
     * @param days        조회할 기간 (일 수, 기본값: 30)
     * @return 그래프용 토큰 사용량 데이터
     * @apiNote 점검O
     * @since 2025-12-30
     */
    @GetMapping("/graph")
    public ResponseEntity<GptTokenUsageGraphResponse> getTokenUsageGraph(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false, defaultValue = "30") int days) {

        String email = userDetails.getUser().getEmail();
        log.info("GPT 토큰 사용량 그래프 조회 요청: email={}, period={}, days={}", email, period, days);

        User user = userService.findUserByEmail(email);
        GptTokenUsageGraphResponse response = graphService.getTokenUsageGraph(user, period, days);

        return ResponseEntity.ok(response);
    }
}
