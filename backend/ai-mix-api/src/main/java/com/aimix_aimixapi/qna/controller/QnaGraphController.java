package com.aimix_aimixapi.qna.controller;

import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.common.exception.domain.AccessDeniedException;
import com.aimix_aimixapi.qna.dto.graph.NodePositionSaveRequest;
import com.aimix_aimixapi.qna.dto.graph.ReactFlowGraphResponse;
import com.aimix_aimixapi.qna.service.graph.QnaReactFlowGraphService;
import com.aimix_aimixapi.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/qna/graph")
@RequiredArgsConstructor
public class QnaGraphController {

    private final QnaReactFlowGraphService qnaReactFlowGraphService;

    /**
     * QNA 질문을 기준으로 React Flow 그래프 조회
     * 같은 태그를 가진 QNA 질문들을 연결
     * 로그인한 사용자의 경우 저장된 노드 위치를 불러옵니다.
     * GET /api/v1/qna/graph/react-flow?centerQuestionId={questionId}&maxNodes=50&excludeNoTag=true
     * 
     * @param centerQuestionId 중심 질문 ID (선택적, null이면 전체 그래프)
     * @param maxNodes 최대 노드 수 (기본값: 50)
     * @param excludeNoTag 태그가 없는 게시물 제외 여부 (기본값: true)
     * @param userDetails 인증된 사용자 정보 (선택적, 로그인한 경우에만 저장된 위치 사용)
     * @return React Flow 형식의 그래프 데이터
     */
    @GetMapping("/react-flow")
    public ResponseEntity<ReactFlowGraphResponse> getQnaReactFlowGraph(
            @RequestParam(required = false) UUID centerQuestionId,
            @RequestParam(required = false) Integer maxNodes,
            @RequestParam(required = false, defaultValue = "true") Boolean excludeNoTag,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("QNA React Flow 그래프 조회 요청: centerQuestionId={}, maxNodes={}, excludeNoTag={}", 
                centerQuestionId, maxNodes, excludeNoTag);

        User user = (userDetails != null && userDetails.getUser() != null)
                ? userDetails.getUser() 
                : null;
        
        ReactFlowGraphResponse response = qnaReactFlowGraphService.getQnaReactFlowGraph(
                centerQuestionId, maxNodes, excludeNoTag, user);
        return ResponseEntity.ok(response);
    }
    
    /**
     * React Flow 노드 위치 저장
     * 사용자가 노드를 드래그하여 위치를 변경한 후 저장합니다.
     * POST /api/v1/qna/graph/react-flow/positions
     * 
     * @param userDetails 인증된 사용자 정보 (필수)
     * @param request 노드 위치 저장 요청
     * @return 성공 응답
     */
    @PostMapping(value = "/react-flow/positions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveNodePositions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody NodePositionSaveRequest request) {

        if (userDetails == null || userDetails.getUser() == null) {
            throw new AccessDeniedException("로그인한 사용자만 노드 위치를 저장할 수 있습니다.");
        }

        log.info("노드 위치 저장 요청: userId={}, graphType={}", 
                userDetails.getUser().getId(), request.getGraphType());

        qnaReactFlowGraphService.saveNodePositions(userDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }
}
