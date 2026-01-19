import Logo from "@assets/aimix_logo.png";
import "@styles/pages/about.css";

export default function About() {
  return (
    <div className="about-container">
      <div className="about-header">
        <img src={Logo} alt="AI-MIX Logo" className="about-logo" />
        <h1 className="about-title">About AI-MIX</h1>
      </div>

      <div className="about-content">
        <div className="about-section">
          <h2 className="about-section-title">프로젝트 소개</h2>
          <p className="about-text">
            AI-MIX는 AI 기반 학습 플랫폼으로, 사용자들이 AI와 대화하고,
            질문과 답변을 공유하며, 배틀 모드를 통해 학습할 수 있는 서비스입니다.
          </p>
        </div>

        <div className="about-section">
          <h2 className="about-section-title">주요 기능</h2>
          <ul className="about-features">
            <li>AI 챗봇: GPT 기반 대화 기능</li>
            <li>Q&A 게시판: 질문과 답변 공유</li>
            <li>배틀 모드: 문제 풀이를 통한 학습</li>
            <li>지식 카드: 위키 형태의 지식 저장소</li>
          </ul>
        </div>

        <div className="about-section">
          <h2 className="about-section-title">기술 스택</h2>
          <p className="about-text">
            React, Zustand, Framer Motion, React Router 등 최신 기술을 사용하여
            개발되었습니다.
          </p>
        </div>
      </div>
    </div>
  );
}

