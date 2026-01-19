import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import instance from "../../api/axiosInstance";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import LoginModal from "@/components/modal/LoginModal";
import { EyeIcon, EyeOffIcon } from "@/components/common/Icons";
import "@styles/pages/qnaAsk.css";

export default function QnaAsk() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { showLoginModal, setShowLoginModal } = useUIStore();
  const [searchParams] = useSearchParams();
  const sourceId = searchParams.get("sourceId");

  const [form, setForm] = useState({
    title: "",
    body: "",
    isAnonymous: false,
    anonymousPassword: "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  /* 비로그인 → 익명 강제 */
  useEffect(() => {
    if (!user) {
      setForm((prev) => ({ ...prev, isAnonymous: true }));
    }
  }, [user]);

  const onChange = (e) => {
    const { name, value, type, checked } = e.target;

    // 비로그인 상태에서 익명 해제 시도
    if (name === "isAnonymous" && !checked && !user) {
      setShowLoginModal(true);
      return;
    }

    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const submitQuestion = async () => {
    // 이미 등록 중인 경우 중복 요청 방지
    if (isSubmitting) return;

    if (!form.title.trim()) return alert("제목을 입력해주세요.");
    if (!form.body.trim()) return alert("내용을 입력해주세요.");
    if (form.isAnonymous && !form.anonymousPassword.trim())
      return alert("익명 질문은 비밀번호가 필요합니다.");

    setIsSubmitting(true);

    const payload = {
      title: form.title.trim(),
      body: form.body.trim(),
      isAnonymous: form.isAnonymous,
      ...(form.isAnonymous && {
        anonymousPassword: form.anonymousPassword.trim(),
      }),
      ...(sourceId && {
        sourceId: sourceId,
      }),
    };

    try {
      const res = await instance.post("/qna/questions", payload);
      const questionId = res.data.id;

      // 질문 등록 후 바로 상세보기 페이지로 이동
      navigate(`/qna/question/${questionId}`);
    } catch (e) {
      console.error(e);
      alert("질문 등록에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="qna-ask-wrapper">
      <div className="qna-container">
        <h2 className="qna-title">질문 작성</h2>

        <div className="qna-box">
          {/* 익명 옵션 (최소) */}
          <div className="anon-inline">
            <label>
              <input
                type="checkbox"
                name="isAnonymous"
                checked={form.isAnonymous}
                onChange={onChange}
              />
              익명으로 질문하기
            </label>

            {!user && <span className="anon-info">비로그인은 익명만 가능</span>}
          </div>

          {/* 비밀번호 */}
          {form.isAnonymous && (
            <div className="pw-box">
              <input
                type={showPassword ? "text" : "password"}
                name="anonymousPassword"
                value={form.anonymousPassword}
                onChange={onChange}
                placeholder="익명 질문 비밀번호"
                className="anon-password-input pw-input"
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
          )}

          {/* 제목 */}
          <input
            type="text"
            name="title"
            value={form.title}
            onChange={onChange}
            placeholder="제목"
            className="qna-input"
          />

          {/* 내용 */}
          <textarea
            name="body"
            value={form.body}
            onChange={onChange}
            placeholder="질문 내용을 입력하세요"
            className="qna-textarea"
          />

          <button
            className="qna-submit-btn"
            onClick={submitQuestion}
            disabled={isSubmitting}
          >
            {isSubmitting ? "등록 중..." : "질문 등록"}
          </button>
        </div>
      </div>

      {/* 로그인 모달 */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onSuccess={() => {
            // 로그인 성공 후 필요한 작업 수행
          }}
        />
      )}
    </div>
  );
}
