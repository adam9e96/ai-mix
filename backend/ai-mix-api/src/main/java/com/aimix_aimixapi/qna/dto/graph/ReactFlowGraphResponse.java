package com.aimix_aimixapi.qna.dto.graph;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * React Flow 형식의 그래프 응답 DTO
 * QnA 게시물을 노드로, 태그 관계를 엣지로 표현
 */
@Getter
@Setter
@Builder
public class ReactFlowGraphResponse {

    /**
     * React Flow 노드 목록
     */
    private List<ReactFlowNode> nodes;

    /**
     * React Flow 엣지 목록
     */
    private List<ReactFlowEdge> edges;

    @Getter
    @Setter
    @Builder
    public static class ReactFlowNode {
        /**
         * 노드 ID (문자열 형식)
         * Q&A: "qna-{id}"
         */
        private String id;

        /**
         * 노드 타입
         * "qnaQuestion"
         */
        private String type;

        /**
         * 노드 위치 (React Flow에서 사용)
         */
        private Position position;

        /**
         * 노드 데이터
         */
        private NodeData data;

        @Getter
        @Setter
        @Builder
        public static class Position {
            private Double x;
            private Double y;
        }

        @Getter
        @Setter
        @Builder
        public static class NodeData {
            /**
             * 노드 레이블 (제목)
             */
            private String label;

            /**
             * 추가 정보
             */
            private Map<String, Object> metadata;
        }
    }

    @Getter
    @Setter
    @Builder
    public static class ReactFlowEdge {
        /**
         * 엣지 ID
         */
        private String id;

        /**
         * 출발 노드 ID
         */
        private String source;

        /**
         * 도착 노드 ID
         */
        private String target;

        /**
         * 엣지 타입 (선택적)
         * "default", "straight", "step", "smoothstep", "bezier" 등
         */
        private String type;

        /**
         * 엣지 레이블 (관계 타입 표시용)
         */
        private String label;

        /**
         * 엣지 스타일 (선택적)
         */
        private EdgeStyle style;

        @Getter
        @Setter
        @Builder
        public static class EdgeStyle {
            private String stroke;
            private Integer strokeWidth;
        }
    }
}
