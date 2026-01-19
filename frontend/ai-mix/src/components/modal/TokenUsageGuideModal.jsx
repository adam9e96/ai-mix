import { X, BarChart3, Key, Info as InfoIcon, ExternalLink } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import "../../styles/components/tokenUsageGuideModal.css";

export default function TokenUsageGuideModal({ onClose }) {
  return (
    <AnimatePresence>
      <motion.div
        className="token-usage-guide-backdrop"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="token-usage-guide-modal"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: "spring", damping: 25 }}
          onClick={(e) => e.stopPropagation()}
        >
          <button
            className="token-usage-guide-close"
            onClick={onClose}
            aria-label="닫기"
          >
            <X size={18} />
          </button>

          <div className="token-usage-guide-header">
            <div className="token-usage-guide-icon">
              <BarChart3 size={24} />
            </div>
            <h2 className="token-usage-guide-title">GPT 토큰 사용량 안내</h2>
            <p className="token-usage-guide-subtitle">
              토큰 사용량 통계 및 그래프 읽는 방법을 확인해주세요
            </p>
          </div>

          <div className="token-usage-guide-content">
            {/* 그래프 읽는 방법 */}
            <section className="token-usage-guide-section">
              <h3 className="token-usage-guide-section-title">
                <BarChart3 size={18} />
                그래프 읽는 방법
              </h3>
              <ul className="token-usage-guide-list">
                <li>
                  <strong>기간 선택</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    일별, 주별, 월별로 사용량을 확인할 수 있습니다. 기간 선택 버튼을 클릭하여 원하는 기간을 선택하세요.
                  </span>
                </li>
                <li>
                  <strong>API 키 필터</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    "전체", "내 API 키", "공용 키"로 필터링하여 사용량을 구분하여 확인할 수 있습니다.
                  </span>
                </li>
                <li>
                  <strong>그래프 호버</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    그래프 위에 마우스를 올리면 해당 날짜/기간의 상세 사용량 정보가 툴팁으로 표시됩니다.
                  </span>
                </li>
              </ul>
            </section>

            {/* 사용 유형별 설명 */}
            <section className="token-usage-guide-section">
              <h3 className="token-usage-guide-section-title">
                <InfoIcon size={18} />
                사용 유형별 설명
              </h3>
              <ul className="token-usage-guide-list">
                <li>
                  <strong>QNA</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    질문과 답변 생성에 사용된 토큰입니다.
                  </span>
                </li>
                <li>
                  <strong>배틀문제</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    AI 배틀 모드에서 문제 생성에 사용된 토큰입니다.
                  </span>
                </li>
                <li>
                  <strong>배틀 채점</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    AI 배틀 모드에서 답안 채점에 사용된 토큰입니다.
                  </span>
                </li>
                <li>
                  <strong>지식 카드</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    지식 카드 생성에 사용된 토큰입니다.
                  </span>
                </li>
                <li>
                  <strong>채팅</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    채팅 기능에서 사용된 토큰입니다.
                  </span>
                </li>
              </ul>
            </section>

            {/* API 키 구분 */}
            <section className="token-usage-guide-section">
              <h3 className="token-usage-guide-section-title">
                <Key size={18} />
                API 키 구분
              </h3>
              <ul className="token-usage-guide-list">
                <li>
                  <strong>내 API 키</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    사용자가 직접 입력한 OpenAI API 키로 사용된 토큰입니다. 이 토큰 사용량은 사용자의 OpenAI 계정에 요금이 부과됩니다.
                  </span>
                </li>
                <li>
                  <strong>공용 키</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    서비스에서 제공하는 공용 API 키로 사용된 토큰입니다. 공용 키 사용량은 서비스에서 관리됩니다.
                  </span>
                </li>
                <li>
                  <strong>전체</strong>
                  <br />
                  <span className="token-usage-guide-note">
                    내 API 키와 공용 키의 사용량을 합산하여 표시합니다. 그래프에서는 두 키의 사용량을 구분하여 확인할 수 있습니다.
                  </span>
                </li>
              </ul>
            </section>

            {/* 툴팁 정보 */}
            <section className="token-usage-guide-section">
              <h3 className="token-usage-guide-section-title">
                <InfoIcon size={18} />
                툴팁 정보
              </h3>
              <ul className="token-usage-guide-list">
                <li>
                  그래프 위에 마우스를 올리면 상세 정보가 표시됩니다:
                  <ul className="token-usage-guide-sublist">
                    <li>날짜/기간 정보</li>
                    <li>총 토큰 사용량</li>
                    <li>사용 유형별 토큰 사용량 (QNA, 배틀문제, 배틀 채점, 지식 카드, 채팅)</li>
                    <li>API 키별 구분 (내 API 키, 공용 키)</li>
                  </ul>
                </li>
                <li>
                  "전체" 필터를 선택하면 내 API 키와 공용 키의 사용량을 구분하여 표시합니다.
                </li>
                <li>
                  "내 API 키" 또는 "공용 키" 필터를 선택하면 해당 키의 사용량만 표시됩니다.
                </li>
              </ul>
            </section>

            {/* 추가 정보 */}
            <section className="token-usage-guide-section">
              <h3 className="token-usage-guide-section-title">
                <InfoIcon size={18} />
                추가 정보
              </h3>
              <ul className="token-usage-guide-list">
                <li>
                  사용량 통계는{" "}
                  <a
                    href="https://platform.openai.com/usage"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="token-usage-guide-link"
                  >
                    OpenAI Platform
                    <ExternalLink size={14} />
                  </a>
                  의 실제 사용량과 다를 수 있습니다.
                </li>
                <li>
                  정확한 사용량과 요금은{" "}
                  <a
                    href="https://platform.openai.com/usage"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="token-usage-guide-link"
                  >
                    OpenAI Usage 대시보드
                    <ExternalLink size={14} />
                  </a>
                  에서 확인하세요.
                </li>
                <li>
                  본인의 API 키를 입력하면 사용량 절감에 도움이 됩니다.
                </li>
              </ul>
            </section>
          </div>

          <div className="token-usage-guide-footer">
            <button
              className="token-usage-guide-btn token-usage-guide-btn-primary"
              onClick={onClose}
            >
              확인했습니다
            </button>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}


