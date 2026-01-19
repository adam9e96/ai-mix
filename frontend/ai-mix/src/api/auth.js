import axiosInstance from "./axiosInstance";

// ----------------------------------
// 회원가입 (FormData + 인터셉터 적용됨)
// ----------------------------------
export const signupApi = async (formData) => {
  return axiosInstance.post("/auth/signup", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// ----------------------------------
// 로그인 (쿠키 기반 인증 + 인터셉터 적용됨)
// ----------------------------------
export const loginApi = async (data) => {
  return axiosInstance.post("/auth/login", data);
};

// ----------------------------------
// 로그아웃
// ----------------------------------
export const logoutApi = async () => {
  return axiosInstance.post("/auth/logout");
};

// ----------------------------------
// 이메일 중복 체크
// ----------------------------------
export const checkEmailApi = async (email) => {
  return axiosInstance.get(`/auth/check-email?email=${email}`);
};

// ----------------------------------
// 닉네임 중복 체크
// ----------------------------------
export const checkNicknameApi = async (nickname) => {
  return axiosInstance.get(`/auth/check-nickname?nickname=${nickname}`);
};

// ----------------------------------
// 사용자 설정 업데이트
// ----------------------------------
export const updateSettingsApi = async (settings) => {
  return axiosInstance.put("/user/settings", settings);
};

// ----------------------------------
// 이메일 인증 코드 발송
// ----------------------------------
export const sendEmailVerification = async (email) => {
  return axiosInstance.post("/auth/email/send", { email });
};

// ----------------------------------
// 이메일 인증 코드 검증
// ----------------------------------
export const verifyEmailCode = async (email, code) => {
  return axiosInstance.post("/auth/email/verify", { email, code });
};
