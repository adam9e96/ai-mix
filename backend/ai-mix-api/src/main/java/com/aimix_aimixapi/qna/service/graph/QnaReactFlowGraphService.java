package com.aimix_aimixapi.qna.service.graph;

import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import com.aimix_aimixapi.qna.dto.graph.NodePositionSaveRequest;
import com.aimix_aimixapi.qna.dto.graph.ReactFlowGraphResponse;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.entity.QnaQuestionTag;
import com.aimix_aimixapi.qna.repository.QnaQuestionRepository;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.entity.UserProfile;
import com.aimix_aimixapi.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * QnA React Flow 그래프 서비스
 * 같은 태그를 가진 QnA 질문들을 React Flow 형식으로 연결
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaReactFlowGraphService {

    private final QnaQuestionRepository qnaQuestionRepository;
    private final UserProfileRepository userProfileRepository;
    private final GraphPositionRedisService graphPositionRedisService;

    /**
     * QNA 질문을 기준으로 React Flow 그래프 생성
     * 같은 태그를 가진 QNA 질문들을 연결
     * 
     * @param centerQuestionId 중심 질문 ID (선택적, null이면 전체 그래프)
     * @param maxNodes 최대 노드 수 (기본값: 50)
     * @param excludeNoTag 태그가 없는 게시물 제외 여부 (기본값: true)
     * @param user 사용자 (선택적, null이면 저장된 위치 불러오지 않음)
     * @return React Flow 형식의 그래프 데이터
     */
    @Transactional(readOnly = true)
    public ReactFlowGraphResponse getQnaReactFlowGraph(UUID centerQuestionId, Integer maxNodes, Boolean excludeNoTag, User user) {
        boolean excludeNoTagQuestions = (excludeNoTag != null) ? excludeNoTag : true; // 기본값: true (태그 없는 것 제외)
        log.info("QNA React Flow 그래프 생성 요청: centerQuestionId={}, maxNodes={}, excludeNoTag={}", 
                centerQuestionId, maxNodes, excludeNoTagQuestions);

        List<ReactFlowGraphResponse.ReactFlowNode> nodes = new ArrayList<>();
        List<ReactFlowGraphResponse.ReactFlowEdge> edges = new ArrayList<>();
        Set<String> nodeIds = new HashSet<>();
        AtomicInteger edgeCounter = new AtomicInteger(0);
        Map<UUID, Set<Long>> questionTagsMap = new HashMap<>(); // 질문별 태그 ID 목록

        int nodeLimit = (maxNodes != null && maxNodes > 0) ? maxNodes : 50;

        // 1. QNA 질문 목록 조회
        List<QnaQuestion> questions;
        UUID centerQuestionIdForMetadata = null; // 중심 노드 표시용
        
        if (centerQuestionId != null) {
            // 특정 질문을 중심으로 관련 질문 찾기
            // Fetch Join을 사용하여 태그 정보를 함께 가져옴 (성능 최적화)
            List<QnaQuestion> allQuestionsWithTags = qnaQuestionRepository.findAllWithTags();
            
            QnaQuestion centerQuestion = allQuestionsWithTags.stream()
                    .filter(q -> q.getId().equals(centerQuestionId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("질문을 찾을 수 없습니다: " + centerQuestionId));
            
            centerQuestionIdForMetadata = centerQuestionId;
            
            // 중심 질문의 태그 ID 수집
            Set<Long> centerTagIds = centerQuestion.getQuestionTags() != null
                    ? centerQuestion.getQuestionTags().stream()
                            .map(QnaQuestionTag::getTagId)
                            .collect(Collectors.toSet())
                    : new HashSet<>();
            
            // 중심 질문에 태그가 없고 excludeNoTag가 true이면 빈 그래프 반환
            if (centerTagIds.isEmpty() && excludeNoTagQuestions) {
                log.info("중심 질문에 태그가 없고 excludeNoTag=true이므로 빈 그래프 반환");
                return ReactFlowGraphResponse.builder()
                        .nodes(new ArrayList<>())
                        .edges(new ArrayList<>())
                        .build();
            }
            
            // 같은 태그를 가진 질문들 찾기
            questions = new ArrayList<>();
            questions.add(centerQuestion); // 중심 질문 추가 (태그가 없어도 포함)
            
            for (QnaQuestion q : allQuestionsWithTags) {
                if (questions.size() >= nodeLimit) break;
                if (q.getId().equals(centerQuestionId)) continue;
                
                // excludeNoTag가 true이면 태그가 없는 질문 제외
                if (excludeNoTagQuestions && (q.getQuestionTags() == null || q.getQuestionTags().isEmpty())) {
                    continue;
                }
                
                // 중심 질문에 태그가 있으면 공통 태그 확인
                if (!centerTagIds.isEmpty()) {
                    // 공통 태그 확인
                    Set<Long> qTagIds = q.getQuestionTags() != null
                            ? q.getQuestionTags().stream()
                                    .map(QnaQuestionTag::getTagId)
                                    .collect(Collectors.toSet())
                            : new HashSet<>();
                    
                    qTagIds.retainAll(centerTagIds);
                    if (!qTagIds.isEmpty()) {
                        questions.add(q);
                    }
                } else {
                    // 중심 질문에 태그가 없으면 excludeNoTag가 false일 때만 추가
                    if (!excludeNoTagQuestions) {
                        questions.add(q);
                    }
                }
            }
        } else {
            // 전체 질문 목록 (최대 개수 제한)
            // Fetch Join을 사용하여 태그 정보를 함께 가져옴 (성능 최적화)
            questions = qnaQuestionRepository.findAllWithTags().stream()
                    .filter(q -> {
                        // excludeNoTag가 true이면 태그가 없는 질문 제외
                        if (excludeNoTagQuestions) {
                            return q.getQuestionTags() != null && !q.getQuestionTags().isEmpty();
                        }
                        return true; // excludeNoTag가 false이면 모든 질문 포함
                    })
                    .limit(nodeLimit)
                    .collect(Collectors.toList());
        }

        // 2. 노드 생성 및 태그 정보 수집
        Map<Long, String> tagNameMap = new HashMap<>(); // 태그 ID -> 태그 이름 맵
        
        for (QnaQuestion question : questions) {
            // 질문의 태그 정보 수집 (Fetch Join으로 이미 로드됨)
            List<QnaQuestionTag> questionTags = question.getQuestionTags() != null 
                    ? question.getQuestionTags() 
                    : new ArrayList<>();
            
            // excludeNoTag가 true이고 태그가 없으면 노드 생성하지 않음
            if (excludeNoTagQuestions && questionTags.isEmpty()) {
                continue;
            }
            
            String qnaId = "qna-" + question.getId();
            nodeIds.add(qnaId);
            
            Set<Long> tagIds = questionTags.stream()
                    .map(QnaQuestionTag::getTagId)
                    .collect(Collectors.toSet());
            questionTagsMap.put(question.getId(), tagIds);

            // 태그 이름 맵 생성
            for (QnaQuestionTag questionTag : questionTags) {
                if (questionTag.getTag() != null) {
                    tagNameMap.putIfAbsent(questionTag.getTagId(), questionTag.getTag().getName());
                }
            }

            // 노드 데이터 생성
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("viewCount", question.getViewCount());
            metadata.put("answerCount", question.getAnswers() != null ? question.getAnswers().size() : 0);
            metadata.put("isAnonymous", question.isAnonymous());
            if (question.getUser() != null) {
                metadata.put("authorNickname", question.getUser().getNickname());
            }
            
            // 태그 이름 목록 추가
            List<String> tagNames = questionTags.stream()
                    .map(qt -> qt.getTag() != null ? qt.getTag().getName() : "")
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());
            metadata.put("tags", tagNames);
            
            // 중심 노드 표시 (중심 게시물 중심 그래프인 경우)
            if (question.getId().equals(centerQuestionIdForMetadata)) {
                metadata.put("isCenter", true);
            }

            nodes.add(ReactFlowGraphResponse.ReactFlowNode.builder()
                    .id(qnaId)
                    .type("qnaQuestion")
                    .position(ReactFlowGraphResponse.ReactFlowNode.Position.builder()
                            .x(0.0)
                            .y(0.0)
                            .build())
                    .data(ReactFlowGraphResponse.ReactFlowNode.NodeData.builder()
                            .label(question.getTitle())
                            .metadata(metadata)
                            .build())
                    .build());
        }

        // 3. 같은 태그를 가진 질문들 연결 (엣지 생성)
        List<UUID> questionIds = new ArrayList<>(questionTagsMap.keySet());
        for (int i = 0; i < questionIds.size(); i++) {
            UUID questionId1 = questionIds.get(i);
            String sourceId = "qna-" + questionId1;
            Set<Long> tags1 = questionTagsMap.get(questionId1);

            for (int j = i + 1; j < questionIds.size(); j++) {
                UUID questionId2 = questionIds.get(j);
                String targetId = "qna-" + questionId2;
                Set<Long> tags2 = questionTagsMap.get(questionId2);

                // 공통 태그 찾기
                Set<Long> commonTags = new HashSet<>(tags1);
                commonTags.retainAll(tags2);

                if (!commonTags.isEmpty()) {
                    // 공통 태그 이름 목록 생성 (최대 3개까지 표시)
                    List<String> commonTagNames = commonTags.stream()
                            .map(tagId -> tagNameMap.getOrDefault(tagId, "태그#" + tagId))
                            .limit(3)
                            .collect(Collectors.toList());
                    
                    // 엣지 레이블 생성
                    String edgeLabel;
                    if (commonTagNames.size() == 1) {
                        edgeLabel = commonTagNames.getFirst();
                    } else if (commonTagNames.size() == commonTags.size()) {
                        // 모든 태그가 표시되는 경우
                        edgeLabel = String.join(", ", commonTagNames);
                    } else {
                        // 태그가 많아서 일부만 표시되는 경우
                        edgeLabel = String.join(", ", commonTagNames) + " (+" + (commonTags.size() - commonTagNames.size()) + ")";
                    }
                    
                    // 공통 태그가 있으면 엣지 생성
                    edges.add(ReactFlowGraphResponse.ReactFlowEdge.builder()
                            .id("edge-" + edgeCounter.getAndIncrement())
                            .source(sourceId)
                            .target(targetId)
                            .type("smoothstep")
                            .label(edgeLabel)
                            .style(ReactFlowGraphResponse.ReactFlowEdge.EdgeStyle.builder()
                                    .stroke("#f59e0b")
                                    .strokeWidth(2)
                                    .build())
                            .build());
                }
            }
        }

        // 4. 노드 위치 배치
        // 저장된 위치가 있으면 사용, 없으면 자동 배치
        String graphType = centerQuestionId != null 
                ? "qna-center-" + centerQuestionId 
                : "qna-all";
        
        Map<String, NodePositionSaveRequest.Position> savedPositions = null;
        if (user != null) {
            savedPositions = getSavedNodePositions(user, graphType);
        }
        
        if (savedPositions != null && !savedPositions.isEmpty()) {
            // 저장된 위치 적용
            applySavedPositions(nodes, savedPositions);
        } else {
            // 자동 배치 (원형 레이아웃)
            layoutNodes(nodes);
        }

        log.info("QNA React Flow 그래프 생성 완료: nodes={}, edges={}", nodes.size(), edges.size());

        return ReactFlowGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    /**
     * 사용자별 노드 위치 저장 (Write-Through 패턴)
     * 1. Redis에 즉시 저장 (빠른 응답)
     * 2. PostgreSQL에 비동기 동기화 (데이터 보존)
     * 
     * @param user 사용자
     * @param request 노드 위치 저장 요청
     */
    public void saveNodePositions(User user, NodePositionSaveRequest request) {
        log.info("노드 위치 저장 요청: userId={}, graphType={}", user.getId(), request.getGraphType());
        
        // 1단계: Redis에 즉시 저장 (빠른 응답 시간: 1-3ms)
        try {
            graphPositionRedisService.saveNodePositions(user.getId(), request);
            log.info("Redis에 노드 위치 저장 완료: graphType={}, nodeCount={}", 
                    request.getGraphType(), 
                    request.getPositions() != null ? request.getPositions().size() : 0);
        } catch (Exception e) {
            log.error("Redis 저장 실패, PostgreSQL로 직접 저장: userId={}, graphType={}", 
                    user.getId(), request.getGraphType(), e);
            // Redis 실패 시 PostgreSQL에 직접 저장 (Fallback)
            saveNodePositionsToPostgreSQL(user, request);
            return;
        }
        
        // 2단계: PostgreSQL에 비동기 동기화 (백그라운드 처리)
        syncToPostgreSQL(user, request);
    }
    
    /**
     * PostgreSQL에 노드 위치 저장 (Fallback 또는 직접 저장용)
     * 별도의 graphPositions JSONB 컬럼에 저장 (settings와 분리)
     */
    @Transactional
    public void saveNodePositionsToPostgreSQL(User user, NodePositionSaveRequest request) {
        // UserProfile 조회 또는 생성
        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = UserProfile.builder()
                    .user(user)
                    .build();
            user.setUserProfile(userProfile);
        }
        
        // graphPositions 필드 가져오기 (별도 JSONB 컬럼)
        Map<String, Object> graphPositions = userProfile.getGraphPositions();
        if (graphPositions == null) {
            graphPositions = new HashMap<>();
            userProfile.setGraphPositions(graphPositions);
        }
        
        // 노드 위치 정보를 Map으로 변환하여 저장
        Map<String, Map<String, Double>> positionsMap = new HashMap<>();
        if (request.getPositions() != null) {
            for (Map.Entry<String, NodePositionSaveRequest.Position> entry : request.getPositions().entrySet()) {
                Map<String, Double> pos = new HashMap<>();
                pos.put("x", entry.getValue().getX());
                pos.put("y", entry.getValue().getY());
                positionsMap.put(entry.getKey(), pos);
            }
        }
        
        // 그래프 타입별로 위치 저장
        graphPositions.put(request.getGraphType(), positionsMap);
        
        userProfileRepository.save(userProfile);
        log.info("PostgreSQL에 노드 위치 저장 완료: graphType={}, nodeCount={}", 
                request.getGraphType(), positionsMap.size());
    }
    
    /**
     * PostgreSQL에 비동기 동기화
     * Redis 저장 후 백그라운드에서 실행
     */
    @Async("taskExecutor")
    public void syncToPostgreSQL(User user, NodePositionSaveRequest request) {
        try {
            log.debug("PostgreSQL 동기화 시작: userId={}, graphType={}", user.getId(), request.getGraphType());
            saveNodePositionsToPostgreSQL(user, request);
            log.debug("PostgreSQL 동기화 완료: userId={}, graphType={}", user.getId(), request.getGraphType());
        } catch (Exception e) {
            log.error("PostgreSQL 동기화 실패: userId={}, graphType={}", 
                    user.getId(), request.getGraphType(), e);
            // 실패 시 재시도 로직은 향후 구현 가능
        }
    }
    
    /**
     * 사용자별 저장된 노드 위치 조회 (Redis 우선, 없으면 PostgreSQL)
     * 
     * @param user 사용자
     * @param graphType 그래프 타입
     * @return 노드 ID별 위치 정보 (없으면 null)
     */
    public Map<String, NodePositionSaveRequest.Position> getSavedNodePositions(User user, String graphType) {
        if (user == null) {
            return null;
        }
        
        // 1단계: Redis에서 조회 (빠른 조회: 0.5-2ms)
        try {
            Map<String, NodePositionSaveRequest.Position> redisResult = 
                    graphPositionRedisService.getNodePositions(user.getId(), graphType);
            if (redisResult != null && !redisResult.isEmpty()) {
                log.debug("Redis에서 노드 위치 조회 성공: graphType={}, nodeCount={}", 
                        graphType, redisResult.size());
                return redisResult;
            }
        } catch (Exception e) {
            log.warn("Redis 조회 실패, PostgreSQL에서 조회: userId={}, graphType={}", 
                    user.getId(), graphType, e);
        }
        
        // 2단계: Redis에 없으면 PostgreSQL에서 조회 및 Redis에 캐싱
        return getFromPostgreSQLAndCache(user, graphType);
    }
    
    /**
     * PostgreSQL에서 조회하고 Redis에 캐싱
     * 별도의 graphPositions JSONB 컬럼에서 조회 (settings와 분리)
     */
    @Transactional(readOnly = true)
    public Map<String, NodePositionSaveRequest.Position> getFromPostgreSQLAndCache(
            User user, String graphType) {
        if (user.getUserProfile() == null) {
            return null;
        }
        
        // graphPositions 필드에서 조회 (별도 JSONB 컬럼)
        Map<String, Object> graphPositions = user.getUserProfile().getGraphPositions();
        if (graphPositions == null || graphPositions.isEmpty()) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> positionsMap = (Map<String, Map<String, Object>>) graphPositions.get(graphType);
        if (positionsMap == null || positionsMap.isEmpty()) {
            return null;
        }
        
        // Map<String, Map<String, Object>>를 Map<String, Position>으로 변환
        Map<String, NodePositionSaveRequest.Position> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : positionsMap.entrySet()) {
            Map<String, Object> posMap = entry.getValue();
            NodePositionSaveRequest.Position position = new NodePositionSaveRequest.Position();
            
            Object xObj = posMap.get("x");
            Object yObj = posMap.get("y");
            
            if (xObj instanceof Number) {
                position.setX(((Number) xObj).doubleValue());
            }
            if (yObj instanceof Number) {
                position.setY(((Number) yObj).doubleValue());
            }
            
            result.put(entry.getKey(), position);
        }
        
        // PostgreSQL에서 조회한 데이터를 Redis에 캐싱 (비동기)
        if (!result.isEmpty()) {
            cacheToRedis(user.getId(), graphType, result);
        }
        
        log.debug("PostgreSQL에서 노드 위치 조회 및 Redis 캐싱: graphType={}, nodeCount={}", 
                graphType, result.size());
        return result;
    }
    
    /**
     * Redis에 캐싱 (비동기)
     */
    @Async("taskExecutor")
    public void cacheToRedis(Long userId, String graphType, 
                              Map<String, NodePositionSaveRequest.Position> positions) {
        try {
            NodePositionSaveRequest request = new NodePositionSaveRequest();
            request.setGraphType(graphType);
            request.setPositions(positions);
            graphPositionRedisService.saveNodePositions(userId, request);
            log.debug("Redis 캐싱 완료: userId={}, graphType={}", userId, graphType);
        } catch (Exception e) {
            log.warn("Redis 캐싱 실패: userId={}, graphType={}", userId, graphType, e);
        }
    }
    
    /**
     * 저장된 위치를 노드에 적용
     * 
     * @param nodes 노드 목록
     * @param savedPositions 저장된 위치 정보
     */
    private void applySavedPositions(List<ReactFlowGraphResponse.ReactFlowNode> nodes,
                                     Map<String, NodePositionSaveRequest.Position> savedPositions) {
        for (ReactFlowGraphResponse.ReactFlowNode node : nodes) {
            NodePositionSaveRequest.Position savedPos = savedPositions.get(node.getId());
            if (savedPos != null && savedPos.getX() != null && savedPos.getY() != null) {
                node.getPosition().setX(savedPos.getX());
                node.getPosition().setY(savedPos.getY());
            }
        }
    }

    /**
     * 노드 위치 자동 배치 (원형 레이아웃)
     */
    private void layoutNodes(List<ReactFlowGraphResponse.ReactFlowNode> nodes) {
        int nodeCount = nodes.size();
        if (nodeCount == 0) {
            return;
        }

        double centerX = 400.0;
        double centerY = 400.0;
        double radius = Math.min(300.0, nodeCount * 30.0);
        double angleStep = 2 * Math.PI / nodeCount;

        for (int i = 0; i < nodeCount; i++) {
            double angle = i * angleStep;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            nodes.get(i).getPosition().setX(x);
            nodes.get(i).getPosition().setY(y);
        }
    }
}
