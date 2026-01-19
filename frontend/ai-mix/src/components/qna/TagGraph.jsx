import React, { useCallback, useEffect, useState } from "react";
import ReactFlow, {
  Panel,
  useNodesState,
  useEdgesState,
  Controls,
  Background,
  ReactFlowProvider,
} from "reactflow";
import "reactflow/dist/style.css";
import { useNavigate } from "react-router-dom";
import { getQnaGraph, saveNodePositions } from "@/api/qnaTagApi";
import { toast } from "react-toastify";
import { Loader2 } from "lucide-react";
import "@styles/components/tag-graph.css";
import CustomQnaNode from "./CustomQnaNode";

// 노드 타입 매핑
const nodeTypes = {
  qnaQuestion: CustomQnaNode,
};

const TagGraph = ({ onClose }) => {
  const navigate = useNavigate();
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [loading, setLoading] = useState(true);
  const [saveTimeout, setSaveTimeout] = useState(null);

  // 그래프 타입 (전체 QnA 그래프)
  const graphType = "qna-all";

  const loadGraph = useCallback(async () => {
    try {
      setLoading(true);
      // 새 API 사용 (React Flow 형식으로 직접 반환, excludeNoTag=true)
      const data = await getQnaGraph(null, 50, true);

      // 데이터가 없거나 형식이 잘못된 경우 처리
      if (!data) {
        console.warn("No graph data received");
        setNodes([]);
        setEdges([]);
        return;
      }

      const nodesData = data.nodes || [];
      const edgesData = data.edges || [];

      // 노드가 없는 경우
      if (nodesData.length === 0) {
        setNodes([]);
        setEdges([]);
        return;
      }

      // 백엔드 응답을 React Flow 형식으로 변환 (이미 대부분 형식이 맞지만 보장)
      const reactFlowNodes = nodesData.map((node) => ({
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

      const reactFlowEdges = edgesData.map((edge) => ({
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

      setNodes(reactFlowNodes);
      setEdges(reactFlowEdges);
    } catch (error) {
      console.error("Failed to load graph", error);
      const errorMessage =
        error.response?.data?.message || error.message || "알 수 없는 오류";
      toast.error(`그래프를 불러오는 중 오류가 발생했습니다: ${errorMessage}`);
      setNodes([]);
      setEdges([]);
    } finally {
      setLoading(false);
    }
  }, [setNodes, setEdges]);

  // 현재 노드 위치 저장
  const saveCurrentPositions = useCallback(async () => {
    try {
      const positions = {};
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
    }
  }, [nodes, graphType]);

  // 노드 변경 핸들러 (위치 변경 감지)
  const handleNodesChange = useCallback(
    (changes) => {
      onNodesChange(changes);

      // 위치 변경이 있는지 확인
      const hasPositionChange = changes.some(
        (change) => change.type === "position" && change.dragging === false
      );

      if (hasPositionChange) {
        // 기존 타이머 취소
        if (saveTimeout) {
          clearTimeout(saveTimeout);
        }

        // 1초 후 저장 (디바운스)
        const timer = setTimeout(() => {
          saveCurrentPositions();
        }, 1000);

        setSaveTimeout(timer);
      }
    },
    [onNodesChange, saveTimeout, saveCurrentPositions]
  );

  useEffect(() => {
    loadGraph();

    // 컴포넌트 언마운트 시 타이머 정리
    return () => {
      if (saveTimeout) {
        clearTimeout(saveTimeout);
      }
    };
  }, [loadGraph]);

  const onNodeClick = useCallback(
    (event, node) => {
      // 노드 ID에서 questionId 추출 (qna-{uuid} 형식)
      const questionId = node.id.replace("qna-", "");
      if (questionId) {
        navigate(`/qna/question/${questionId}`);
      }
    },
    [navigate]
  );

  return (
    <div className="tag-graph-container" onClick={(e) => e.stopPropagation()}>
      <div className="tag-graph-header">
        <h3 className="tag-graph-title">게시물 시각화</h3>
        <div className="tag-graph-header-actions">
          <button className="tag-graph-close" onClick={onClose}>
            ✕
          </button>
        </div>
      </div>
      <div className="tag-graph-content" onClick={(e) => e.stopPropagation()}>
        {loading ? (
          <div className="tag-graph-loading">
            <Loader2 className="spinner" size={32} />
            <p>그래프를 불러오는 중...</p>
          </div>
        ) : nodes.length === 0 ? (
          <div className="tag-graph-empty">
            <p>표시할 데이터가 없습니다.</p>
          </div>
        ) : (
          <div className="tag-graph-flow-wrapper">
            <ReactFlowProvider>
              <ReactFlow
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                onNodesChange={handleNodesChange}
                onEdgesChange={onEdgesChange}
                onNodeClick={onNodeClick}
                fitView
                fitViewOptions={{
                  padding: 0.2,
                  minZoom: 0.1,
                  maxZoom: 1.5,
                  includeHiddenNodes: false,
                }}
                minZoom={0.1}
                maxZoom={1.5}
                attributionPosition="bottom-left"
              >
                <Panel position="top-right" className="tag-graph-panel">
                  <div
                    style={{
                      background: "#fff",
                      padding: "8px 12px",
                      borderRadius: "4px",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                      fontSize: "12px",
                      marginBottom: "8px",
                    }}
                  >
                    노드 수: {nodes.length} | 엣지 수: {edges.length}
                  </div>
                  <button
                    onClick={() => loadGraph()}
                    style={{
                      padding: "8px 16px",
                      background: "#4CAF50",
                      color: "#fff",
                      border: "none",
                      borderRadius: "4px",
                      cursor: "pointer",
                      fontWeight: "bold",
                    }}
                    disabled={loading}
                  >
                    새로고침
                  </button>
                </Panel>
                <Controls />
                <Background color="#e5e7eb" gap={16} />
              </ReactFlow>
            </ReactFlowProvider>
          </div>
        )}
      </div>
    </div>
  );
};

export default TagGraph;
