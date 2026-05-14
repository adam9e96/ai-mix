// src/pages/Home.jsx
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import Logo from "@assets/aimix_logo.webp";
import "@styles/pages/home.css";

export default function Home() {
  return (
    <div className="home-container">
      <div className="home-top">
        <img src={Logo} alt="AI-MIX Logo" className="home-logo" />

        <p className="home-subtitle">AI-MIX에 오신 것을 환영합니다 👋</p>
      </div>

      <h2 className="home-title">왜 AI-MIX인가요?</h2>
      <p className="home-description">최고의 질문·응답 경험을 제공합니다.</p>

      <div className="home-features">
        {/* AI 챗봇 */}
        <motion.div
          whileHover={{ scale: 1.05, y: -8 }}
          whileTap={{ scale: 0.95 }}
          transition={{ type: "spring", stiffness: 400, damping: 17 }}
        >
          <Link to="/chat" className="feature-circle">
            AI챗봇
          </Link>
        </motion.div>

        {/* 지식백과 */}
        <motion.div
          whileHover={{ scale: 1.05, y: -8 }}
          whileTap={{ scale: 0.95 }}
          transition={{ type: "spring", stiffness: 400, damping: 17 }}
        >
          <Link to="/knowledge" className="feature-circle">
            지식카드
          </Link>
        </motion.div>

        {/* Q&A */}
        <motion.div
          whileHover={{ scale: 1.05, y: -8 }}
          whileTap={{ scale: 0.95 }}
          transition={{ type: "spring", stiffness: 400, damping: 17 }}
        >
          <Link to="/qna" className="feature-circle">
            Q&A
          </Link>
        </motion.div>
      </div>

      <div className="home-bottom-text"></div>
    </div>
  );
}
