import { useState, useEffect, useRef } from "react";
import { signupApi, sendEmailVerification, verifyEmailCode } from "../api/auth";
import "../styles/pages/auth.css";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

/* 🔥 아이콘들 */
import { EyeIcon, EyeOffIcon, ShieldCheckIcon } from "../components/common/Icons";

export default function Signup() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    emailUsername: "",
    emailDomain: "",
    password: "",
    passwordConfirm: "",
    nickname: "",
    birthDate: "",
    isAgreed: false,
    bio: "",
    settings: { darkMode: false, notifications: true },
    avatar: null,
  });

  const [showPw, setShowPw] = useState(false);
  const [showPwConfirm, setShowPwConfirm] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [showEmailDropdown, setShowEmailDropdown] = useState(false);
  const emailDropdownRef = useRef(null);

  /* 📧 이메일 인증 관련 상태 */
  const [verificationState, setVerificationState] = useState("IDLE"); // IDLE, SENT, VERIFIED
  const [verificationCode, setVerificationCode] = useState("");
  const [timer, setTimer] = useState(0); // 초 단위 (5분 = 300)
  const timerRef = useRef(null);

  // 이메일 도메인 목록 (필수 항목만)
  const emailDomains = [
    "naver.com",
    "gmail.com",
    "daum.net",
    "hanmail.net",
    "nate.com",
  ];

  /* ⭐ 비밀번호 조건: 아무 문자든 최소 8글자 */
  const isPwValid = form.password.trim().length >= 8;
  const isPwMatch = form.password === form.passwordConfirm && form.passwordConfirm.length > 0;

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        emailDropdownRef.current &&
        !emailDropdownRef.current.contains(event.target)
      ) {
        setShowEmailDropdown(false);
      }
    };

    if (showEmailDropdown) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showEmailDropdown]);

  // 타이머 로직
  useEffect(() => {
    if (timer > 0) {
      timerRef.current = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
    } else if (timer === 0 && verificationState === "SENT") {
      clearInterval(timerRef.current);
    }
    return () => clearInterval(timerRef.current);
  }, [timer, verificationState]);

  // 시간 포맷팅 (MM:SS)
  const formatTime = (seconds) => {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return `${min}:${sec < 10 ? "0" : ""}${sec}`;
  };

  const handleSendVerification = async () => {
    const fullEmail = `${form.emailUsername}@${form.emailDomain}`;
    console.log(">>> [인증번호 발송 시도] 받는 이메일:", fullEmail); // 디버깅용 로그

    if (!form.emailUsername || !form.emailDomain) {
      toast.error("이메일을 모두 입력해주세요.");
      return;
    }

    try {
      await sendEmailVerification(fullEmail);
      toast.success("인증 코드가 발송되었습니다. 이메일을 확인해주세요.");
      setVerificationState("SENT");
      setVerificationCode("");
      setTimer(300); // 5분
    } catch (err) {
      toast.error("인증 코드 발송 실패: " + (err.response?.data?.message || "오류 발생"));
    }
  };

  const handleVerifyCode = async () => {
    const fullEmail = `${form.emailUsername}@${form.emailDomain}`;
    if (!verificationCode) {
      toast.error("인증 코드를 입력해주세요.");
      return;
    }

    try {
      await verifyEmailCode(fullEmail, verificationCode);
      toast.success("이메일 인증이 완료되었습니다!");
      setVerificationState("VERIFIED");
      clearInterval(timerRef.current);
    } catch (err) {
      toast.error("인증 실패: " + (err.response?.data?.message || "코드가 올바르지 않거나 만료되었습니다."));
    }
  };

  const onChange = (e) => {
    const { name, value, type, checked, files } = e.target;

    if (type === "checkbox") {
      setForm({ ...form, [name]: checked });
    } else if (type === "file") {
      const file = files[0];
      if (file) {
        setForm({ ...form, avatar: file });
        // 미리보기 생성
        const reader = new FileReader();
        reader.onloadend = () => {
          setAvatarPreview(reader.result);
        };
        reader.readAsDataURL(file);
      }
    } else {
      setForm({ ...form, [name]: value });
    }

    // 이메일 수정 시 인증 초기화
    if (name === "emailUsername" || name === "emailDomain") {
      setVerificationState("IDLE");
      setVerificationCode("");
      setTimer(0);
      clearInterval(timerRef.current);
    }
  };

  const handleEmailDomainSelect = (domain) => {
    setForm({ ...form, emailDomain: domain });
    setShowEmailDropdown(false);
  };

  const onSubmit = async (e) => {
    e.preventDefault();

    if (verificationState !== "VERIFIED") {
      toast.error("이메일 인증을 완료해주세요.");
      return;
    }

    // 이메일 합치기
    const fullEmail = `${form.emailUsername}@${form.emailDomain}`;

    const fd = new FormData();
    fd.append("email", fullEmail);
    fd.append("password", form.password);
    fd.append("nickname", form.nickname);
    fd.append("isAgreed", String(form.isAgreed));

    if (form.birthDate) fd.append("birthDate", form.birthDate);
    if (form.bio) fd.append("bio", form.bio);
    if (form.settings) fd.append("settings", JSON.stringify(form.settings));
    if (form.avatar) fd.append("avatar", form.avatar);

    try {
      await signupApi(fd);
      toast.success("회원가입이 완료되었습니다! 🎉");
      navigate("/login");
    } catch (err) {
      console.error("회원가입 실패:", err);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">회원가입</h1>
        <p className="auth-subtitle">AI MIX에 오신 것을 환영합니다 🎉</p>

        <form onSubmit={onSubmit} className="auth-form">
          {/* 이메일 */}
          <div className="email-input-box" ref={emailDropdownRef}>
            <div className="email-input-row">
              <input
                className="email-username-input auth-input"
                name="emailUsername"
                placeholder=""
                value={form.emailUsername}
                onChange={onChange}
                required
                disabled={verificationState === "VERIFIED"}
              />
              <span className="email-at">@</span>
              <div className="email-domain-wrapper">
                <input
                  className="email-domain-input auth-input"
                  name="emailDomain"
                  placeholder="직접 입력"
                  value={form.emailDomain}
                  onChange={onChange}
                  onFocus={() => setShowEmailDropdown(true)}
                  required
                  disabled={verificationState === "VERIFIED"}
                />
                <button
                  type="button"
                  className="email-dropdown-btn"
                  onClick={() => setShowEmailDropdown(!showEmailDropdown)}
                  disabled={verificationState === "VERIFIED"}
                >
                  ▼
                </button>
                {showEmailDropdown && (
                  <div className="email-dropdown">
                    <div className="email-dropdown-title">이메일선택</div>
                    {emailDomains.map((domain) => (
                      <div
                        key={domain}
                        className={`email-dropdown-item ${form.emailDomain === domain ? "selected" : ""
                          }`}
                        onClick={() => handleEmailDomainSelect(domain)}
                      >
                        {domain}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* 인증번호 발송 버튼 */}
          {verificationState === "IDLE" && (
            <button
              type="button"
              className="auth-verify-btn"
              onClick={handleSendVerification}
            >
              인증번호 받기
            </button>
          )}

          {/* 인증번호 입력 영역 */}
          {(verificationState === "SENT" || verificationState === "VERIFIED") && (
            <div className="verification-area">
              <div className="verification-input-row">
                <input
                  className="auth-input verification-code-input"
                  placeholder="인증번호 6자리"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  disabled={verificationState === "VERIFIED"}
                  maxLength={6}
                />
                {verificationState === "SENT" && (
                  <span className="verification-timer">{formatTime(timer)}</span>
                )}
                {verificationState === "VERIFIED" ? (
                  <span className="verification-success-badge">
                    <ShieldCheckIcon size={20} color="#4CAF50" />
                    인증 완료
                  </span>
                ) : (
                  <button
                    type="button"
                    className="auth-verify-confirm-btn"
                    onClick={handleVerifyCode}
                  >
                    확인
                  </button>
                )}
              </div>
              {verificationState === "SENT" && (
                <div className="verification-retry-text">
                  인증번호가 오지 않나요?
                  <span onClick={handleSendVerification} className="retry-link">재전송</span>
                </div>
              )}
            </div>
          )}

          {/* 🔥 비밀번호 */}
          <div className="pw-box">
            <input
              className="auth-input pw-input"
              type={showPw ? "text" : "password"}
              name="password"
              placeholder="비밀번호 (8자 이상)"
              value={form.password}
              onChange={onChange}
              required
            />

            {/* 👁 오른쪽 눈 아이콘 */}
            <div className="pw-eye" onClick={() => setShowPw(!showPw)}>
              {showPw ? <EyeIcon size={22} /> : <EyeOffIcon size={22} />}
            </div>
          </div>

          {/* 🔽 비밀번호 조건 안내 */}
          {form.password.length > 0 && (
            <div className="pw-valid-row">
              {isPwValid ? (
                <span className="pw-success">
                  <ShieldCheckIcon size={18} color="#4CAF50" />
                  조건을 만족했습니다.
                </span>
              ) : (
                <span className="pw-fail">8자리 이상 입력해주세요.</span>
              )}
            </div>
          )}

          {/* 비밀번호 확인 */}
          <div className="pw-box">
            <input
              className="auth-input pw-input"
              type={showPwConfirm ? "text" : "password"}
              name="passwordConfirm"
              placeholder="비밀번호 확인"
              value={form.passwordConfirm}
              onChange={onChange}
              required
            />

            {/* 👁 오른쪽 눈 아이콘 */}
            <div className="pw-eye" onClick={() => setShowPwConfirm(!showPwConfirm)}>
              {showPwConfirm ? <EyeIcon size={22} /> : <EyeOffIcon size={22} />}
            </div>
          </div>

          {/* 비밀번호 일치 확인 */}
          {form.passwordConfirm.length > 0 && (
            <div className="pw-valid-row">
              {isPwMatch ? (
                <span className="pw-success">
                  <ShieldCheckIcon size={18} color="#4CAF50" />
                  비밀번호가 일치합니다.
                </span>
              ) : (
                <span className="pw-fail">비밀번호가 일치하지 않습니다.</span>
              )}
            </div>
          )}

          {/* 닉네임 */}
          <input
            className="auth-input"
            name="nickname"
            placeholder="닉네임"
            value={form.nickname}
            onChange={onChange}
            required
          />

          {/* 생년월일 */}
          <input
            className="auth-input"
            type="date"
            name="birthDate"
            value={form.birthDate}
            onChange={onChange}
          />

          {/* 자기소개 */}
          <textarea
            className="auth-input auth-textarea"
            name="bio"
            placeholder="자기소개"
            value={form.bio}
            onChange={onChange}
          />

          {/* 개인정보 동의 */}
          <label className="auth-checkbox-row">
            <input
              type="checkbox"
              name="isAgreed"
              checked={form.isAgreed}
              onChange={onChange}
            />
            개인정보 동의
          </label>

          {/* 프로필 이미지 업로드 */}
          <div className="auth-avatar-section">
            {avatarPreview && (
              <div className="auth-avatar-preview">
                <img
                  src={avatarPreview}
                  alt="프로필 미리보기"
                  className="auth-avatar-preview-img"
                />
                <button
                  type="button"
                  className="auth-avatar-remove"
                  onClick={() => {
                    setAvatarPreview(null);
                    setForm({ ...form, avatar: null });
                  }}
                >
                  ✕
                </button>
              </div>
            )}
            <label className="auth-file-label">
              <span className="auth-file-button">파일 선택</span>
              <span className="auth-file-text">
                {form.avatar ? form.avatar.name : "선택된 파일 없음"}
              </span>
              <input
                type="file"
                name="avatar"
                accept="image/*"
                onChange={onChange}
                style={{ display: "none" }}
              />
            </label>
          </div>

          <button className="auth-btn" type="submit">
            회원가입
          </button>
        </form>

        {/* 하단 로그인 안내 */}
        <div className="auth-bottom">
          이미 계정이 있나요?
          <span onClick={() => navigate("/login")}>로그인하기</span>
        </div>
      </div>
    </div>
  );
}
