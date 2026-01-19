import { useState } from "react";
import { X } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import "../../styles/components/passwordConfirmModal.css";
import { EyeIcon, EyeOffIcon } from "../common/Icons";
import axiosInstance from "../../api/axiosInstance";

export default function PasswordConfirmModal({ onClose, onConfirm }) {
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!password.trim()) {
      setError("비밀번호를 입력해주세요.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await axiosInstance.post("/user/verify-password", {
        password: password,
      });

      if (res.data.verified) {
        // 비밀번호 확인 성공 시 콜백 실행
        onConfirm(password);
      } else {
        setError("비밀번호가 일치하지 않습니다.");
      }
    } catch (err) {
      const backendMessage = err.response?.data?.message || "";
      // "이메일 또는 비밀번호" 메시지를 "비밀번호"로 변경
      const errorMessage = backendMessage.includes("이메일 또는 비밀번호")
        ? "비밀번호가 일치하지 않습니다."
        : backendMessage || "비밀번호 확인 중 오류가 발생했습니다.";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AnimatePresence>
      <motion.div
        className="password-confirm-backdrop"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="password-confirm-modal-box"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: "spring", damping: 25 }}
          onClick={(e) => e.stopPropagation()}
        >
          <button
            className="password-confirm-close mac-close"
            onClick={onClose}
          >
            <X size={15} />
          </button>

          <div className="password-confirm-header">
            <h2 className="password-confirm-title">비밀번호 확인</h2>
            <p className="password-confirm-description">
              회원정보를 수정하려면 비밀번호를 입력해주세요.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="password-confirm-form">
            <div className="pw-box">
              <input
                className="auth-input pw-input"
                type={showPassword ? "text" : "password"}
                placeholder="비밀번호 입력"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setError("");
                }}
                autoFocus
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

            {error && <p className="password-confirm-error">{error}</p>}

            <div className="password-confirm-btn-group">
              <button
                type="button"
                className="password-confirm-btn password-confirm-btn-cancel"
                onClick={onClose}
              >
                취소
              </button>
              <button
                type="submit"
                className="password-confirm-btn password-confirm-btn-submit"
                disabled={!password.trim() || loading}
              >
                {loading ? "확인 중..." : "확인"}
              </button>
            </div>
          </form>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}

