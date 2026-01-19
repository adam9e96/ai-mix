import axios from "axios";
import { toast } from "react-toastify";

const instance = axios.create({
  baseURL: "/api/v1",
  withCredentials: true, // refreshToken 쿠키 자동 전송
});

/* ===========================
    요청 인터셉터
    httpOnly 쿠키는 자동으로 전송되므로 별도 처리 불필요
=========================== */
instance.interceptors.request.use((config) => {
  // httpOnly 쿠키는 자동으로 전송되므로 Authorization 헤더 추가 불필요

  // 디버깅: accept 엔드포인트 요청 로그
  if (config.url?.includes("/answers/") && config.url?.includes("/accept")) {
    console.log("Axios 요청 인터셉터:", {
      url: config.url,
      method: config.method,
      baseURL: config.baseURL,
      fullURL: `${config.baseURL}${config.url}`,
      data: config.data,
      dataString: JSON.stringify(config.data),
      headers: config.headers,
    });
  }

  return config;
});

/* ===========================
    응답 인터셉터
    401 → refresh 자동 시도
=========================== */
instance.interceptors.response.use(
  (res) => res,

  async (error) => {
    const original = error.config;
    const status = error.response?.status;

    // ⭐ 401 에러 + 아직 재시도 안했을 때 → refresh 시도
    // httpOnly 쿠키가 있으면 자동으로 refreshToken이 전송됨
    if (status === 401 && !original._retry) {
      original._retry = true;

      try {
        // 🔄 refreshToken 쿠키로 accessToken 재발급 요청
        // 백엔드에서 새로운 accessToken을 쿠키로 설정해줌
        await axios.post("/api/v1/auth/refresh", {}, { withCredentials: true });

        // ⭐ 쿠키 기반이므로 새로운 토큰은 쿠키로 자동 설정됨
        // 원래 요청을 다시 실행 (쿠키가 자동으로 전송됨)
        return instance(original);
      } catch (e) {
        console.error("Refresh 실패 → 로그아웃 처리");

        window.location.href = "/login";

        return Promise.reject(e);
      }
    }

    // ===========================
    // 공통 에러 메시지 처리
    // ===========================
    // ⭐ /user/me 엔드포인트의 401/403 에러는 정상 흐름 (로그인 안된 상태)
    // → 토스트 표시하지 않고, 콘솔 에러도 출력하지 않음
    const isUserMeEndpoint = original?.url?.includes("/user/me");
    if (isUserMeEndpoint && (status === 401 || status === 403)) {
      // 조용히 에러 반환 (콘솔 에러 없음)
      // 에러 객체에 플래그 추가하여 콘솔에 표시되지 않도록 처리
      error._silent = true;
      error._isAuthCheck = true;
      return Promise.reject(error);
    }

    // ⭐ /user/verify-password 엔드포인트는 토스트 표시하지 않음 (모달 내부에서 에러 처리)
    const isVerifyPasswordEndpoint = original?.url?.includes(
      "/user/verify-password"
    );
    if (isVerifyPasswordEndpoint) {
      return Promise.reject(error);
    }

    // ⭐ 지식카드 관련 엔드포인트는 토스트 표시하지 않음 (컴포넌트에서 직접 에러 처리)
    const isKnowledgeCardEndpoint = original?.url?.includes("/knowledge/cards");
    if (isKnowledgeCardEndpoint) {
      return Promise.reject(error);
    }

    // ⭐ 401/403 에러는 인증 관련 정상 흐름일 수 있으므로 콘솔 에러 출력하지 않음
    // (다른 엔드포인트에서 발생한 경우에만 토스트 표시)
    if (status === 401 || status === 403) {
      const backendMsg =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.response?.data?.msg;

      if (backendMsg) toast.error(backendMsg);
      else toast.error("인증이 필요합니다.");

      return Promise.reject(error);
    }

    // ⭐ 기타 에러는 콘솔에 출력하고 토스트 표시
    console.error("API Error:", error);

    const backendMsg =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.response?.data?.msg;

    if (backendMsg) toast.error(backendMsg);
    else toast.error("서버 요청 중 오류가 발생했습니다.");

    return Promise.reject(error);
  }
);

export default instance;
