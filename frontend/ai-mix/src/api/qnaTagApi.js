import axiosInstance from "./axiosInstance";

// React Flow QnA 그래프 조회 (새 API)
export const getQnaGraph = async (
  centerQuestionId = null,
  maxNodes = 50,
  excludeNoTag = false
) => {
  try {
    const params = new URLSearchParams();
    if (centerQuestionId) {
      params.append("centerQuestionId", centerQuestionId);
    }
    params.append("maxNodes", maxNodes.toString());
    if (excludeNoTag) {
      params.append("excludeNoTag", "true");
    }

    const queryString = params.toString();
    const url = `/qna/graph/react-flow${queryString ? `?${queryString}` : ""}`;

    console.log("getQnaGraph API 호출:", url);
    const response = await axiosInstance.get(url);
    console.log("getQnaGraph API 응답:", response.data);

    if (!response.data) {
      console.warn("응답 데이터가 없습니다");
      return { nodes: [], edges: [] };
    }

    return response.data;
  } catch (error) {
    console.error("getQnaGraph error:", error);
    console.error("에러 상세:", {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      url: error.config?.url,
    });
    if (error.response?.status === 404) {
      return { nodes: [], edges: [] };
    }
    throw error;
  }
};

// 노드 위치 저장
export const saveNodePositions = async (graphType, positions) => {
  try {
    const response = await axiosInstance.post(
      `/qna/graph/react-flow/positions`,
      {
        graphType,
        positions,
      }
    );
    return response.data;
  } catch (error) {
    console.error("saveNodePositions error:", error);
    throw error;
  }
};
