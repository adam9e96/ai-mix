import React, { useCallback, useEffect, useState } from "react";
import ReactFlow, {
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  Panel,
  useNodesState,
  useEdgesState,
  NodeChange,
  EdgeChange,
  Connection,
  addEdge,
  applyNodeChanges,
  applyEdgeChanges,
  ReactFlowProvider,
} from "reactflow";
import "reactflow/dist/style.css";
import { getQnaGraph, saveNodePositions } from "@/api/qnaTagApi";
import CustomQnaNode from "./CustomQnaNode";
import { Loader2 } from "lucide-react";
import { useNavigate } from "react-router-dom";

interface QnaGraphProps {
  centerQuestionId?: string;
  maxNodes?: number;
}

const nodeTypes = {
  qnaQuestion: CustomQnaNode,
};

const QnaGraph: React.FC<QnaGraphProps> = ({
  centerQuestionId,
  maxNodes = 50,
}) => {
  const navigate = useNavigate();
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saveTimeout, setSaveTimeout] = useState<number | null>(null);
  const [lastSaveTime, setLastSaveTime] = useState<number>(0);
  const [isSaving, setIsSaving] = useState(false);

  // 그래프 타입 결정
  const graphType = centerQuestionId
    ? `qna-center-${centerQuestionId}`
    : "qna-all";

  // 그래프 데이터 로드
  const loadGraph = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      console.log("그래프 로드 시작:", { centerQuestionId, maxNodes });
      const data = await getQnaGraph(centerQuestionId, maxNodes);
      console.log("API 응답 데이터:", data);

      if (!data || !data.nodes || data.nodes.length === 0) {
        console.warn("노드 데이터가 없습니다:", data);
        setNodes([]);
        setEdges([]);
        setLoading(false);
        return;
      }

      // 백엔드 응답을 React Flow 형식으로 변환 (이미 대부분 형식이 맞지만 보장)
      const reactFlowNodes: Node[] = data.nodes.map((node) => ({
        id: node.id,
        type: node.type || "qnaQuestion",
        position: node.position || { x: 0, y: 0 },
        data: {
          label: node.data?.label || node.label || "",
          metadata: node.data?.metadata || {},
        },
        selectable: true,
        draggable: true,
        connectable: false,
      }));

      const reactFlowEdges: Edge[] = data.edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.type || "smoothstep",
        label: edge.label || "",
        style: edge.style || {
          stroke: "#f59e0b",
          strokeWidth: 2,
        },
        animated: true,
        labelStyle: {
          fill: edge.style?.stroke || "#f59e0b",
          fontWeight: 600,
          fontSize: "12px",
        },
        labelBgStyle: {
          fill: "#fff",
          fillOpacity: 0.8,
        },
      }));

      console.log(
        "변환된 노드:",
        reactFlowNodes.length,
        "변환된 엣지:",
        reactFlowEdges.length
      );
      setNodes(reactFlowNodes);
      setEdges(reactFlowEdges);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "그래프 로드 실패";
      setError(errorMessage);
      console.error("그래프 로드 실패:", err);
      console.error("에러 상세:", {
        message: errorMessage,
        response: (err as any)?.response?.data,
        status: (err as any)?.response?.status,
      });
    } finally {
      setLoading(false);
    }
  }, [centerQuestionId, maxNodes, setNodes, setEdges]);

  // 현재 노드 위치 저장
  const saveCurrentPositions = useCallback(async () => {
    try {
      setIsSaving(true);
      // 로그인 여부는 백엔드에서 확인하므로 여기서는 바로 호출
      const positions: Record<string, { x: number; y: number }> = {};
      nodes.forEach((node) => {
        positions[node.id] = {
          x: node.position.x,
          y: node.position.y,
        };
      });

      if (Object.keys(positions).length > 0) {
        await saveNodePositions(graphType, positions);
        console.log("노드 위치 저장 완료");
      }
    } catch (err) {
      console.error("노드 위치 저장 실패:", err);
      // 로그인하지 않은 경우 에러 무시
    } finally {
      setIsSaving(false);
    }
  }, [nodes, graphType]);

  // 노드 변경 핸들러 (위치 변경 감지)
  const handleNodesChange = useCallback(
    (changes: NodeChange[]) => {
      onNodesChange(changes);

      // 위치 변경이 있는지 확인
      const positionChanges = changes.filter(
        (change) => change.type === "position"
      );

      if (positionChanges.length > 0) {
        const now = Date.now();
        const timeSinceLastSave = now - lastSaveTime;
        const throttleDelay = 200; // 200ms throttle

        // 드래그가 끝났을 때는 즉시 저장
        const isDragEnd = positionChanges.some(
          (change) => change.type === "position" && change.dragging === false
        );

        if (isDragEnd) {
          // 기존 타이머 취소
          if (saveTimeout) {
            clearTimeout(saveTimeout);
            setSaveTimeout(null);
          }
          // 즉시 저장
          saveCurrentPositions();
          setLastSaveTime(now);
        } else if (timeSinceLastSave >= throttleDelay) {
          // 드래그 중이지만 throttle 시간이 지났으면 저장
          saveCurrentPositions();
          setLastSaveTime(now);
        } else {
          // throttle 시간이 안 지났으면 기존 타이머 취소하고 새로 설정
          if (saveTimeout) {
            clearTimeout(saveTimeout);
          }
          const remainingTime = throttleDelay - timeSinceLastSave;
          const timer = setTimeout(() => {
            saveCurrentPositions();
            setLastSaveTime(Date.now());
            setSaveTimeout(null);
          }, remainingTime);
          setSaveTimeout(timer);
        }
      }
    },
    [onNodesChange, saveTimeout, saveCurrentPositions, lastSaveTime]
  );

  // 엣지 연결 핸들러
  const onConnect = useCallback(
    (params: Connection) => {
      setEdges((eds) => addEdge(params, eds));
    },
    [setEdges]
  );

  // 노드 클릭 핸들러
  const onNodeClick = useCallback(
    (event: React.MouseEvent, node: Node) => {
      // 노드 ID에서 questionId 추출 (qna-{uuid} 형식)
      let questionId = node.id;
      if (questionId.startsWith("qna-")) {
        questionId = questionId.replace("qna-", "");
      }
      console.log("노드 클릭:", {
        nodeId: node.id,
        questionId,
        centerQuestionId,
      });
      if (questionId && questionId !== centerQuestionId) {
        navigate(`/qna/question/${questionId}`);
      }
    },
    [navigate, centerQuestionId]
  );

  // 초기 로드
  useEffect(() => {
    if (centerQuestionId) {
      loadGraph();
    }

    // 컴포넌트 언마운트 시 타이머 정리
    return () => {
      if (saveTimeout) {
        clearTimeout(saveTimeout);
      }
    };
  }, [centerQuestionId, loadGraph]);

  if (loading) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          height: "100%",
          minHeight: "600px",
          gap: "12px",
          padding: "40px",
        }}
      >
        <Loader2
          className="spinner"
          size={32}
          style={{ animation: "spin 1s linear infinite" }}
        />
        <p style={{ fontSize: "16px", color: "#6b7280" }}>그래프 로딩 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          height: "100%",
          minHeight: "600px",
          gap: "16px",
          padding: "40px",
          color: "#ef4444",
        }}
      >
        <p style={{ fontSize: "18px", fontWeight: "600" }}>
          에러가 발생했습니다
        </p>
        <p style={{ fontSize: "14px", color: "#6b7280", textAlign: "center" }}>
          {error}
        </p>
        <button
          onClick={loadGraph}
          style={{
            padding: "10px 20px",
            background: "#3b82f6",
            color: "#fff",
            border: "none",
            borderRadius: "8px",
            cursor: "pointer",
            fontSize: "14px",
            fontWeight: "500",
          }}
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (nodes.length === 0) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          height: "100%",
          minHeight: "600px",
          gap: "16px",
          padding: "40px",
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: "48px",
            marginBottom: "8px",
            opacity: 0.5,
          }}
        >
          🏷️
        </div>
        <p
          style={{
            fontSize: "18px",
            color: "#374151",
            fontWeight: "600",
            marginBottom: "8px",
          }}
        >
          관련 게시물을 찾을 수 없습니다
        </p>
        <p
          style={{
            fontSize: "14px",
            color: "#6b7280",
            lineHeight: "1.6",
            maxWidth: "400px",
          }}
        >
          이 질문에 태그가 없어 관련 게시물 그래프를 표시할 수 없습니다.
          <br />
          태그를 추가하면 유사한 질문들을 찾아 연결해드립니다.
        </p>
        <button
          onClick={loadGraph}
          style={{
            padding: "10px 20px",
            background: "#6b7280",
            color: "#fff",
            border: "none",
            borderRadius: "8px",
            cursor: "pointer",
            fontSize: "14px",
            fontWeight: "500",
            marginTop: "8px",
            transition: "background 0.2s",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = "#4b5563";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = "#6b7280";
          }}
        >
          새로고침
        </button>
      </div>
    );
  }

  return (
    <div
      style={{
        width: "100%",
        height: "100%",
        minHeight: "600px",
        position: "relative",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodesChange={handleNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        onNodeClick={onNodeClick}
        fitView
        fitViewOptions={{ padding: 0.2 }}
        minZoom={0.1}
        maxZoom={1.5}
        attributionPosition="bottom-left"
        style={{ width: "100%", height: "100%", flex: 1 }}
      >
        <Panel position="top-right">
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              gap: "8px",
              alignItems: "flex-end",
            }}
          >
            <div
              style={{
                background: "#fff",
                padding: "8px 12px",
                borderRadius: "4px",
                boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                fontSize: "12px",
              }}
            >
              노드 수: {nodes.length} | 엣지 수: {edges.length}
            </div>
            {isSaving && (
              <div
                style={{
                  background: "#fff",
                  padding: "8px 12px",
                  borderRadius: "4px",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                  fontSize: "12px",
                  display: "flex",
                  alignItems: "center",
                  gap: "8px",
                  color: "#3b82f6",
                }}
              >
                <Loader2
                  size={14}
                  style={{ animation: "spin 1s linear infinite" }}
                />
                <span>업데이트 중...</span>
              </div>
            )}
          </div>
        </Panel>
        <Controls />
        <Background color="#e5e7eb" gap={16} />
        <MiniMap
          nodeColor={(node) => {
            if (node.data?.metadata?.isCenter) {
              return "#fbbf24";
            }
            return "#fff";
          }}
          style={{
            backgroundColor: "#f9fafb",
          }}
        />
      </ReactFlow>
    </div>
  );
};

// ReactFlowProvider로 감싸서 export
const QnaGraphWithProvider: React.FC<QnaGraphProps> = (props) => {
  return (
    <ReactFlowProvider>
      <QnaGraph {...props} />
    </ReactFlowProvider>
  );
};

export default QnaGraphWithProvider;
