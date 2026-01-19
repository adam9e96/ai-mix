package com.aimix_aimixapi.qna.dto.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * React Flow 노드 위치 저장 요청 DTO
 * 사용자별로 노드 위치를 저장하기 위한 요청
 */
@Getter
@Setter
public class NodePositionSaveRequest {
    /**
     * 그래프 타입
     * "qna-all": 전체 QnA 그래프
     * "qna-center-{questionId}": 특정 게시물 중심 QnA 그래프
     */
    private String graphType;
    
    /**
     * 노드 ID별 위치 정보
     * key: 노드 ID (예: "qna-123")
     * value: 위치 정보 { "x": 100.0, "y": 200.0 }
     */
    private Map<String, Position> positions;
    
    @Getter
    @Setter
    public static class Position {
        private Double x;
        private Double y;
    }
}
