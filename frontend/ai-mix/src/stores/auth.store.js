// 인증 상태 관리
import { create } from "zustand";
import { subscribeWithSelector } from "zustand/middleware";
import axiosInstance from "../api/axiosInstance";

export const useAuthStore = create(
  subscribeWithSelector((set, get) => ({
    // 상태
    user: null,
    loading: true,

    // 액션: 사용자 정보 로드
    loadUser: async () => {
      try {
        // ⭐ 쿠키 기반 인증: 쿠키가 있으면 성공, 없으면 401/403 에러
        const res = await axiosInstance.get("/user/me");

        const data = res.data;

        // 디버깅: API 응답 구조 확인 (성공한 경우에만)
        console.log("/user/me API 응답:", {
          전체응답: data,
          id: data.id,
          email: data.email,
          nickname: data.nickname,
          응답키목록: Object.keys(data),
        });

        const userData = {
          id: data.id || data.userId || null, // id가 없으면 userId 시도
          email: data.email,
          nickname: data.nickname,
          role: data.role,
          avatarUrl: data.avatarUrl,
        };

        set({
          user: userData,
          loading: false,
        });

        // 사용자 정보 반환 (토스트 메시지 등에서 사용)
        return userData;
      } catch (err) {
        // 401 / 403 은 "로그인 안됨" 상황 (쿠키 없음) → 정상 흐름
        // → 콘솔 에러 출력하지 않음
        if (
          err.response?.status === 401 ||
          err.response?.status === 403 ||
          err._isAuthCheck
        ) {
          set({ user: null, loading: false });
          return null;
        } else {
          // 예기치 못한 오류만 로그 출력
          console.error("AuthStore loadUser error:", err);
          set({ user: null, loading: false });
          return null;
        }
      }
    },

    // 액션: 로그아웃
    logout: () => {
      set({ user: null });
      // 쿠키는 서버에서 처리하거나 별도 API 호출 필요
    },

    // 액션: 사용자 정보 설정 (로그인 후 사용)
    setUser: (userInfo) => {
      if (!userInfo) {
        set({ user: null, loading: false });
        return;
      }

      // userInfo 구조에 맞게 변환
      const user = {
        id: userInfo.id || null,
        email: userInfo.email || null,
        nickname: userInfo.nickname || null,
        role: userInfo.role || null,
        avatarUrl: userInfo.avatarUrl || null,
      };

      // 즉시 상태 업데이트 (헤더에 바로 반영되도록)
      set({ user, loading: false });
    },

    // Computed: 로그인 여부
    isAuthenticated: () => get().user !== null,
  }))
);
