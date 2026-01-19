import { Link } from "react-router-dom";
import "../../styles/layout/footer.css";

function Footer() {
  return (
    <footer className="aimix-footer">
      <div className="footer-inner">
        <div className="footer-section brand">
          <h3 className="footer-title">AI-MIX</h3>
          <p className="footer-desc">
            AI 기반 학습 · Q&A · 배틀 기능을 제공하는 차세대 학습 플랫폼입니다.
          </p>
        </div>

        <div className="footer-section links">
          <h4 className="footer-sub">Links</h4>
          <ul>
            <li>
              <Link to="/">Home</Link>
            </li>

            <li>
              <Link to="/about">About</Link>
            </li>
          </ul>
        </div>

        <div className="footer-section project">
          <h4 className="footer-sub">Project</h4>
          <ul>
            <li>
              <a
                href="https://www.notion.so/adam9e96/2bf9b563794080edb279f65a0529a107"
                target="_blank"
                rel="noopener noreferrer"
              >
                Notion 문서
              </a>
            </li>
            <li>
              <a
                href="https://github.com"
                target="_blank"
                rel="noopener noreferrer"
              >
                GitHub 저장소
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div className="footer-bottom">
        © {new Date().getFullYear()} AI-MIX. All rights reserved.
      </div>
    </footer>
  );
}

export default Footer;

