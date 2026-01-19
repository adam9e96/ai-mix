// 배틀 전적 관리
import { create } from "zustand";
import axiosInstance from "../api/axiosInstance";

export const useBattleStore = create((set, get) => ({
  // 현재 세션의 배틀 전적 (sessionId를 키로 사용)
  battleSummaries: {},

  // 전적 표시 여부 (세션별)
  showSummaries: {},

  // 액션: 배틀 전적 가져오기
  fetchBattleSummary: async (battleId) => {
    try {
      const res = await axiosInstance.get(`/battle/${battleId}/summary`);
      return res.data;
    } catch (err) {
      console.error("배틀 전적 조회 실패:", err);
      return null;
    }
  },

  // 액션: 세션에 배틀 전적 추가
  addBattleSummary: (sessionId, summary) => {
    set((state) => ({
      battleSummaries: {
        ...state.battleSummaries,
        [sessionId]: [...(state.battleSummaries[sessionId] || []), summary],
      },
      // 전적 추가 시 자동으로 표시
      showSummaries: {
        ...state.showSummaries,
        [sessionId]: true,
      },
    }));
  },

  // 액션: 세션의 배틀 전적 가져오기
  getBattleSummaries: (sessionId) => {
    return get().battleSummaries[sessionId] || [];
  },

  // 액션: 전적 표시 토글
  toggleShowSummaries: (sessionId) => {
    set((state) => ({
      showSummaries: {
        ...state.showSummaries,
        [sessionId]: !state.showSummaries[sessionId],
      },
    }));
  },

  // 액션: 전적 표시 여부 확인
  // 배틀 전적이 있으면 기본적으로 true 반환 (새로고침 후에도 표시)
  isShowSummaries: (sessionId) => {
    const state = get();
    const summaries = state.battleSummaries[sessionId] || [];
    // 배틀 전적이 있으면 자동으로 표시 (명시적으로 false로 설정된 경우만 제외)
    if (summaries.length > 0) {
      return state.showSummaries[sessionId] !== false;
    }
    return state.showSummaries[sessionId] ?? false;
  },

  // 액션: 세션의 배틀 전적 초기화
  clearBattleSummaries: (sessionId) => {
    set((state) => {
      const newSummaries = { ...state.battleSummaries };
      const newShowSummaries = { ...state.showSummaries };
      delete newSummaries[sessionId];
      delete newShowSummaries[sessionId];
      return { battleSummaries: newSummaries, showSummaries: newShowSummaries };
    });
  },
}));
