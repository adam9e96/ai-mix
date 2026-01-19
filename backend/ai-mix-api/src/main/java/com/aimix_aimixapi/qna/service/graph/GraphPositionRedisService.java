package com.aimix_aimixapi.qna.service.graph;

import com.aimix_aimixapi.qna.dto.graph.NodePositionSaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * React Flow 노드 위치 Redis 서비스
 * Write-Through 패턴: Redis에 즉시 저장, PostgreSQL에 비동기 동기화
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GraphPositionRedisService {

    private final StringRedisTemplate stringRedisTemplate; // String 직렬화용
    
    private static final String KEY_PREFIX = "graph:positions:";
    private static final Duration TTL = Duration.ofDays(30); // 30일 TTL

    /**
     * Redis Key 생성
     */
    private String buildKey(Long userId, String graphType) {
        return KEY_PREFIX + userId + ":" + graphType;
    }

    /**
     * 노드 위치를 Redis Hash 형식으로 변환
     */
    private Map<String, String> convertToRedisMap(Map<String, NodePositionSaveRequest.Position> positions) {
        Map<String, String> redisMap = new HashMap<>();
        if (positions != null) {
            for (Map.Entry<String, NodePositionSaveRequest.Position> entry : positions.entrySet()) {
                NodePositionSaveRequest.Position pos = entry.getValue();
                // JSON 형식으로 저장: "x:100.0,y:200.0"
                String value = pos.getX() + "," + pos.getY();
                redisMap.put(entry.getKey(), value);
            }
        }
        return redisMap;
    }

    /**
     * Redis Hash에서 노드 위치 조회 및 변환
     */
    private Map<String, NodePositionSaveRequest.Position> convertFromRedisMap(Map<String, String> redisData) {
        Map<String, NodePositionSaveRequest.Position> result = new HashMap<>();
        for (Map.Entry<String, String> entry : redisData.entrySet()) {
            String nodeId = entry.getKey();
            String value = entry.getValue();
            
            // "100.0,200.0" 형식 파싱
            String[] parts = value.split(",");
            if (parts.length == 2) {
                try {
                    NodePositionSaveRequest.Position position = new NodePositionSaveRequest.Position();
                    position.setX(Double.parseDouble(parts[0]));
                    position.setY(Double.parseDouble(parts[1]));
                    result.put(nodeId, position);
                } catch (NumberFormatException e) {
                    log.warn("위치 데이터 파싱 실패: nodeId={}, value={}", nodeId, value);
                }
            }
        }
        return result;
    }

    /**
     * 노드 위치 저장 (Redis)
     * 
     * @param userId 사용자 ID
     * @param request 노드 위치 저장 요청
     */
    public void saveNodePositions(Long userId, NodePositionSaveRequest request) {
        String key = buildKey(userId, request.getGraphType());
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        
        // Redis Hash에 저장
        Map<String, String> redisMap = convertToRedisMap(request.getPositions());
        if (!redisMap.isEmpty()) {
            hashOps.putAll(key, redisMap);
            stringRedisTemplate.expire(key, TTL);
            log.info("Redis에 노드 위치 저장 완료: key={}, nodeCount={}", key, redisMap.size());
        } else {
            // 위치가 비어있으면 삭제
            stringRedisTemplate.delete(key);
            log.info("Redis에서 노드 위치 삭제: key={}", key);
        }
    }

    /**
     * 노드 위치 조회 (Redis)
     * 
     * @param userId 사용자 ID
     * @param graphType 그래프 타입
     * @return 노드 ID별 위치 정보 (없으면 null)
     */
    public Map<String, NodePositionSaveRequest.Position> getNodePositions(Long userId, String graphType) {
        String key = buildKey(userId, graphType);
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        
        Map<String, String> redisData = hashOps.entries(key);
        if (redisData.isEmpty()) {
            return null;
        }
        
        Map<String, NodePositionSaveRequest.Position> result = convertFromRedisMap(redisData);
        log.debug("Redis에서 노드 위치 조회: key={}, nodeCount={}", key, result != null ? result.size() : 0);
        return result;
    }

    /**
     * 노드 위치 삭제 (Redis)
     * 
     * @param userId 사용자 ID
     * @param graphType 그래프 타입
     */
    public void deleteNodePositions(Long userId, String graphType) {
        String key = buildKey(userId, graphType);
        stringRedisTemplate.delete(key);
        log.info("Redis에서 노드 위치 삭제: key={}", key);
    }
}
