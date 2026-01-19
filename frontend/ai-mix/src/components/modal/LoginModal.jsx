import { useState } from "react";
import { X } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { loginApi } from "../../api/auth";
import { useNavigate } from "react-router-dom";
import "../../styles/pages/auth.css";
import "../../styles/components/loginModal.css";
import { useAuth } from "../../hooks/useAuthContext";
import { useAuthStore } from "../../stores/auth.store";
import { EyeIcon, EyeOffIcon } from "../common/Icons";
import { toast } from "react-toastify";

export default function LoginModal({ onClose, onSuccess }) {
  const navigate = useNavigate();
  const { setUser } = useAuth();
  const { loadUser } = useAuthStore();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

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

      // 성공 콜백이 있으면 실행
      if (onSuccess) {
        onSuccess();
      }

      // 모달 닫기
      onClose();
    } catch (err) {
      // ❌ 여기서는 toast 실행 안 함 (중복 방지)
      console.error("로그인 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AnimatePresence>
      <motion.div
        className="login-backdrop"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="login-modal-box"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: "spring", damping: 25 }}
          onClick={(e) => e.stopPropagation()}
        >
          <button className="login-close mac-close" onClick={onClose}>
            <X size={15} />
          </button>

          <div className="login-header">
            <h1 className="auth-title">로그인</h1>
            <p className="login-subtitle">로그인이 필요한 서비스입니다</p>
          </div>

          <form onSubmit={onSubmit} className="auth-form">
            <input
              className="auth-input"
              name="email"
              type="email"
              placeholder="이메일 입력"
              value={form.email}
              onChange={onChange}
              required
              disabled={loading}
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
                disabled={loading}
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

            <button className="auth-btn" type="submit" disabled={loading}>
              {loading ? "로그인 중..." : "로그인"}
            </button>
          </form>

          <div className="auth-bottom">
            계정이 없나요?
            <span
              onClick={() => {
                onClose();
                navigate("/signup");
              }}
            >
              회원가입하기
            </span>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
