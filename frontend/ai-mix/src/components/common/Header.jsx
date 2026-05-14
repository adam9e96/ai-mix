import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { useAuth } from "../../hooks/useAuthContext";
import Logo from "../../assets/logo.webp";
import { logoutApi } from "../../api/auth";
import { toast } from "react-toastify";
import "../../styles/layout/header.css";

/* 기존 아이콘 유지 */
import { BotIcon, WikiIcon, QnaIcon } from "./Icons";

export default function Header() {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logoutApi();
      setUser(null);
      toast.info("로그아웃 되었습니다.");
      navigate("/login");
    } catch {
      toast.error("로그아웃 실패!");
    }
  };

  const goToMyPage = () => {
    navigate("/mypage");
  };

  /** ⭐ 이미지 URL 처리: 전체 URL이면 그대로, 상대 경로면 백엔드 서버 URL 추가 */
  const getAvatarUrl = (url) => {
    if (!url) return null;
    // 이미 전체 URL이면 그대로 사용
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return url;
    }
    // 상대 경로면 백엔드 서버 URL 추가 (프록시를 통해 접근)
    // 백엔드에서 반환하는 경로가 /uploads/... 형식이면 /api/v1을 붙이거나 그대로 사용
    return url.startsWith("/") ? url : `/${url}`;
  };

  const avatarSrc = getAvatarUrl(user?.avatarUrl);

  return (
    <header className="mix-header">
      <div className="mix-header-inner">
        {/* 로고 */}
        <div className="mix-header-left">
          <Link to="/">
            <img src={Logo} alt="AI-MIX Logo" className="mix-logo-rounded" />
          </Link>
        </div>

        {/* 메뉴 */}
        <nav className="mix-header-center">
          <motion.div
            whileHover={{ y: -2 }}
            whileTap={{ y: 0 }}
            transition={{ type: "spring", stiffness: 400 }}
          >
            <Link to="/chat" className="mix-nav-item nav-with-icon">
              <BotIcon size={20} className="nav-icon" />
              챗봇
            </Link>
          </motion.div>

          <motion.div
            whileHover={{ y: -2 }}
            whileTap={{ y: 0 }}
            transition={{ type: "spring", stiffness: 400 }}
          >
            <Link to="/knowledge" className="mix-nav-item nav-with-icon">
              <WikiIcon size={20} className="nav-icon" />
              지식카드
            </Link>
          </motion.div>

          <motion.div
            whileHover={{ y: -2 }}
            whileTap={{ y: 0 }}
            transition={{ type: "spring", stiffness: 400 }}
          >
            <Link to="/qna" className="mix-nav-item nav-with-icon">
              <QnaIcon size={20} className="nav-icon" />
              Q&A
            </Link>
          </motion.div>
        </nav>

        {/* 오른쪽 */}
        <div className="mix-header-right">
          {user ? (
            <>
              <motion.div
                className="mix-user-info"
                onClick={goToMyPage}
                whileHover={{ y: -2 }}
                whileTap={{ y: 0 }}
                transition={{ type: "spring", stiffness: 400 }}
              >
                {/* ⭐ 여기! 이미지 env + 상대경로 조합 */}
                {avatarSrc ? (
                  <img
                    src={avatarSrc}
                    alt="프로필"
                    className="mix-profile-img"
                  />
                ) : (
                  <div className="mix-profile-placeholder">
                    {user.nickname?.[0]?.toUpperCase() || "U"}
                  </div>
                )}

                <span className="mix-nickname">{user.nickname}님</span>
              </motion.div>

              <motion.button
                className="mix-logout-btn"
                onClick={handleLogout}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                transition={{ type: "spring", stiffness: 400 }}
              >
                로그아웃
              </motion.button>
            </>
          ) : (
            <>
              <motion.div
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                transition={{ type: "spring", stiffness: 400 }}
              >
                <Link to="/login" className="mix-auth-link mix-auth-link-login">
                  로그인
                </Link>
              </motion.div>
              <motion.div
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                transition={{ type: "spring", stiffness: 400 }}
              >
                <Link
                  to="/signup"
                  className="mix-auth-link mix-auth-link-signup"
                >
                  회원가입
                </Link>
              </motion.div>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

