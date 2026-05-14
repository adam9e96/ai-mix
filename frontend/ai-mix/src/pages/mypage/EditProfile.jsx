// src/pages/mypage/EditProfile.jsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import "../../styles/pages/editProfile.css";
import { useUIStore } from "../../stores/ui.store";
import { toast } from "react-toastify";
import { EyeIcon, EyeOffIcon } from "../../components/common/Icons";

export default function EditProfile() {
  const navigate = useNavigate();
  const { mypageLoading, setMypageLoading } = useUIStore();
  const [data, setData] = useState(null);
  const [form, setForm] = useState({
    nickname: "",
    email: "",
    bio: "",
    birthDate: "",
    password: "",
    passwordConfirm: "",
    avatar: null,
  });
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);

  useEffect(() => {
    async function fetchData() {
      try {
        setMypageLoading(true);
        const res = await axiosInstance.get("/user/mypage");
        setData(res.data);
        // 새로운 API 응답 구조: userResponse
        const { userResponse } = res.data;
        const userInfo = userResponse;
        setForm({
          nickname: userInfo?.nickname || "",
          email: userInfo?.email || "",
          bio: userInfo?.bio || "",
          birthDate: userInfo?.birthDate || "",
          password: "",
          passwordConfirm: "",
          avatar: null,
        });
        if (userInfo?.avatarUrl) {
          const getAvatarUrl = (url) => {
            if (!url) return null;
            if (url.startsWith("http://") || url.startsWith("https://")) {
              return url;
            }
            return url.startsWith("/") ? url : `/${url}`;
          };
          setAvatarPreview(getAvatarUrl(userInfo.avatarUrl));
        }
      } catch (err) {
        if (err.response?.status === 401 || err.response?.status === 403) {
          toast.error("로그인이 필요합니다.");
          navigate("/login");
          return;
        }

        console.error("회원정보 불러오기 실패:", err);
      } finally {
        setMypageLoading(false);
      }
    }
    fetchData();
  }, [setMypageLoading, navigate]);

  const onChange = (e) => {
    const { name, value, type, files } = e.target;

    if (type === "file") {
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
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // 비밀번호가 입력되었는지 확인 (공백 제거 후 체크)
    const passwordTrimmed = form.password?.trim();
    const passwordConfirmTrimmed = form.passwordConfirm?.trim();

    // 비밀번호 확인 체크
    if (passwordTrimmed && passwordTrimmed !== passwordConfirmTrimmed) {
      toast.error("비밀번호가 일치하지 않습니다.");
      return;
    }

    // 비밀번호가 입력되었는데 확인이 비어있으면
    if (passwordTrimmed && !passwordConfirmTrimmed) {
      toast.error("비밀번호 확인을 입력해주세요.");
      return;
    }

    try {
      // FormData 형식으로 전송 (avatar 파일 포함 가능)
      const fd = new FormData();
      fd.append("email", form.email);
      fd.append("nickname", form.nickname);

      if (form.birthDate) fd.append("birthDate", form.birthDate);
      if (form.bio) fd.append("bio", form.bio);

      // 비밀번호가 실제로 입력되었을 때만 추가 (공백 제거 후 길이 체크)
      if (passwordTrimmed && passwordTrimmed.length > 0) {
        fd.append("password", passwordTrimmed);
      }

      if (form.avatar) fd.append("avatar", form.avatar);

      // 디버깅: FormData 내용 확인
      console.log("전송할 FormData:");
      // @ts-ignore
      for (let [key, value] of fd.entries()) {
        if (key === "avatar") {
          console.log(`${key}:`, value.name || "File");
        } else {
          console.log(`${key}:`, value);
        }
      }

      await axiosInstance.put("/user", fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      toast.success("회원정보가 수정되었습니다.");
      navigate("/mypage");
    } catch (err) {
      console.error("회원정보 수정 실패:", err);
      // 에러 메시지는 axiosInstance의 인터셉터에서 자동으로 표시됨
    }
  };

  const handleCancel = () => {
    navigate("/mypage");
  };

  if (mypageLoading)
    return <div className="edit-profile-loading">불러오는 중...</div>;
  if (!data)
    return <div className="edit-profile-error">데이터가 없습니다.</div>;

  // 새로운 API 응답 구조: userResponse
  const { userResponse } = data;
  const userInfo = userResponse;

  // userInfo가 없으면 에러 표시
  if (!userInfo) {
    return (
      <div className="edit-profile-error">
        <p>사용자 정보를 불러올 수 없습니다.</p>
      </div>
    );
  }
  const getAvatarUrl = (url) => {
    if (!url) return null;
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return url;
    }
    return url.startsWith("/") ? url : `/${url}`;
  };

  const avatarSrc = getAvatarUrl(userInfo?.avatarUrl);

  return (
    <div className="edit-profile-wrapper">
      <div className="edit-profile-header">
        <h1 className="edit-profile-title">회원정보 수정</h1>
        <p className="edit-profile-subtitle">회원정보를 수정할 수 있습니다.</p>
      </div>

      <div className="edit-profile-card">
        {/* 프로필 이미지 영역 */}
        <div className="edit-profile-avatar-section">
          {avatarPreview || avatarSrc ? (
            <img
              src={avatarPreview || avatarSrc}
              alt="avatar"
              className="edit-profile-avatar"
            />
          ) : (
            <div className="edit-profile-avatar-placeholder">
              {userInfo?.nickname?.[0]?.toUpperCase() || "U"}
            </div>
          )}
          <label className="edit-profile-avatar-btn">
            프로필 이미지 변경
            <input
              type="file"
              name="avatar"
              accept="image/*"
              onChange={onChange}
              style={{ display: "none" }}
            />
          </label>
        </div>

        {/* 회원정보 수정 폼 */}
        <form onSubmit={handleSubmit} className="edit-profile-form">
          <div className="edit-profile-form-group">
            <label className="edit-profile-label">닉네임</label>
            <input
              type="text"
              name="nickname"
              value={form.nickname}
              onChange={onChange}
              className="edit-profile-input"
              placeholder="닉네임을 입력하세요"
            />
          </div>

          <div className="edit-profile-form-group">
            <label className="edit-profile-label">이메일</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={onChange}
              className="edit-profile-input"
              placeholder="이메일을 입력하세요"
              disabled
            />
            <p className="edit-profile-hint">이메일은 변경할 수 없습니다.</p>
          </div>

          <div className="edit-profile-form-group">
            <label className="edit-profile-label">소개글</label>
            <textarea
              name="bio"
              value={form.bio}
              onChange={onChange}
              className="edit-profile-textarea"
              placeholder="자기소개를 입력하세요"
              rows={4}
            />
          </div>

          <div className="edit-profile-form-group">
            <label className="edit-profile-label">생년월일</label>
            <input
              type="date"
              name="birthDate"
              value={form.birthDate}
              onChange={onChange}
              className="edit-profile-input"
            />
          </div>

          <div className="edit-profile-form-group">
            <label className="edit-profile-label">비밀번호 변경</label>
            <div className="pw-box">
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                value={form.password}
                onChange={onChange}
                className="edit-profile-input pw-input"
                placeholder="새 비밀번호 (변경하지 않으려면 비워두세요)"
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
          </div>

          {form.password && (
            <div className="edit-profile-form-group">
              <label className="edit-profile-label">비밀번호 확인</label>
              <div className="pw-box">
                <input
                  type={showPasswordConfirm ? "text" : "password"}
                  name="passwordConfirm"
                  value={form.passwordConfirm}
                  onChange={onChange}
                  className="edit-profile-input pw-input"
                  placeholder="새 비밀번호 확인"
                />
                <div
                  className="pw-eye"
                  onClick={() => setShowPasswordConfirm(!showPasswordConfirm)}
                >
                  {showPasswordConfirm ? (
                    <EyeIcon size={20} color="#666" />
                  ) : (
                    <EyeOffIcon size={20} color="#666" />
                  )}
                </div>
              </div>
            </div>
          )}

          <div className="edit-profile-btn-group">
            <button
              type="button"
              className="edit-profile-save-btn edit-profile-save-btn-cancel"
              onClick={handleCancel}
            >
              취소
            </button>
            <button
              type="submit"
              className="edit-profile-save-btn edit-profile-save-btn-submit"
            >
              저장
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
