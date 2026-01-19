import { useState } from "react";
import { loginApi } from "../api/auth";
import { useNavigate } from "react-router-dom";
import "../styles/pages/auth.css";
import { useAuth } from "../hooks/useAuthContext";
import { useAuthStore } from "../stores/auth.store";
import { EyeIcon, EyeOffIcon } from "../components/common/Icons";
import { toast } from "react-toastify";

export default function Login() {
  const navigate = useNavigate();
  const { setUser } = useAuth();
  const { loadUser } = useAuthStore();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });
  const [showPassword, setShowPassword] = useState(false);

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const onSubmit = async (e) => {
    e.preventDefault();

    try {
      const res = await loginApi(form); // ⭐ 쿠키 자동 저장됨

      // ⭐ userInfo를 전역 상태로 → 헤더 즉시 갱신
      if (res.data?.userInfo) {
        setUser(res.data.userInfo);
      }

      // ⭐ 서버에서 최신 사용자 정보를 다시 불러와서 헤더에 확실히 반영
      await loadUser();

      // ⭐ 로그인 성공 토스트 알림
      const userInfo = res.data?.userInfo || res.data;
      const nickname = userInfo?.nickname || "회원";
      toast.success(`${nickname}님 로그인했습니다`, {
        position: "top-right",
      });

      navigate("/");
    } catch (err) {
      // ❌ 여기서는 toast 실행 안 함 (중복 방지)
      console.error("로그인 실패:", err);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo-text">AI-MIX</div>

        <form onSubmit={onSubmit} className="auth-form">
          <input
            className="auth-input"
            name="email"
            placeholder="이메일 입력"
            value={form.email}
            onChange={onChange}
            required
          />

          <div className="pw-box">
            <input
              className="auth-input pw-input"
              type={showPassword ? "text" : "password"}
              name="password"
              placeholder="비밀번호 입력"
              value={form.password}
              onChange={onChange}
              required
            />
            <div
              className="pw-eye"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? (
                <EyeIcon size={20} color="#666" />
              ) : (
                <EyeOffIcon size={20} color="#666" />
              )}
            </div>
          </div>

          <button className="auth-btn" type="submit">
            로그인
          </button>
        </form>

        <div className="auth-bottom">
          계정이 없나요?
          <span onClick={() => navigate("/signup")}>회원가입하기</span>
        </div>
      </div>
    </div>
  );
}
