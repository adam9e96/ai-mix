import React from "react";
import { Handle, Position, NodeProps } from "reactflow";

interface QnaNodeData {
  label: string;
  metadata?: {
    viewCount?: number;
    answerCount?: number;
    tags?: string[];
    isCenter?: boolean;
    isAnonymous?: boolean;
    authorNickname?: string;
    [key: string]: any;
  };
}

const CustomQnaNode: React.FC<NodeProps<QnaNodeData>> = ({ data, selected }) => {
  const isCenter = data.metadata?.isCenter;
  const viewCount = data.metadata?.viewCount || 0;
  const answerCount = data.metadata?.answerCount || 0;
  const tags = data.metadata?.tags || [];

  return (
    <div
      style={{
        background: isCenter ? "#fbbf24" : "#fff",
        color: isCenter ? "#fff" : "#000",
        border: isCenter ? "3px solid #f59e0b" : "1px solid #777",
        borderRadius: "8px",
        padding: "12px",
        minWidth: "200px",
        maxWidth: "280px",
        boxShadow: selected ? "0 0 0 2px #3b82f6" : "0 2px 4px rgba(0,0,0,0.1)",
        cursor: "pointer",
      }}
    >
      <Handle type="target" position={Position.Top} />
      <div
        style={{
          fontWeight: isCenter ? "bold" : "normal",
          marginBottom: "8px",
          fontSize: "14px",
          wordBreak: "break-word",
        }}
      >
        {data.label}
      </div>
      {tags.length > 0 && (
        <div style={{ marginTop: "8px", fontSize: "12px" }}>
          {tags.slice(0, 3).map((tag, idx) => (
            <span
              key={idx}
              style={{
                background: isCenter ? "rgba(255,255,255,0.2)" : "#e5e7eb",
                padding: "2px 6px",
                borderRadius: "4px",
                marginRight: "4px",
                fontSize: "11px",
                display: "inline-block",
                marginBottom: "4px",
              }}
            >
              {tag}
            </span>
          ))}
          {tags.length > 3 && (
            <span
              style={{
                fontSize: "11px",
                opacity: 0.7,
                marginLeft: "4px",
              }}
            >
              +{tags.length - 3}
            </span>
          )}
        </div>
      )}
      <div
        style={{
          marginTop: "8px",
          fontSize: "11px",
          opacity: 0.7,
          display: "flex",
          gap: "8px",
        }}
      >
        <span>조회 {viewCount}</span>
        <span>답변 {answerCount}</span>
      </div>
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};

export default CustomQnaNode;

