import { X, ExternalLink, Shield, AlertTriangle, DollarSign, Key, Cpu } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import "../../styles/components/apiKeyGuideModal.css";

export default function ApiKeyGuideModal({ onClose }) {
  return (
    <AnimatePresence>
      <motion.div
        className="api-key-guide-backdrop"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="api-key-guide-modal"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: "spring", damping: 25 }}
          onClick={(e) => e.stopPropagation()}
        >
          <button
            className="api-key-guide-close"
            onClick={onClose}
            aria-label="닫기"
          >
            <X size={18} />
          </button>

          <div className="api-key-guide-header">
            <div className="api-key-guide-icon">
              <Key size={24} />
            </div>
            <h2 className="api-key-guide-title">OpenAI API 키 안내</h2>
            <p className="api-key-guide-subtitle">
              API 키 발급 및 사용 시 주의사항을 확인해주세요
            </p>
          </div>

          <div className="api-key-guide-content">
            {/* API 키 발급 방법 */}
            <section className="api-key-guide-section">
              <h3 className="api-key-guide-section-title">
                <Key size={18} />
                API 키 발급 방법
              </h3>
              <ol className="api-key-guide-list">
                <li>
                  <a
                    href="https://platform.openai.com/api-keys"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="api-key-guide-link"
                  >
                    OpenAI Platform
                    <ExternalLink size={14} />
                  </a>
                  에 로그인합니다
                </li>
                <li>좌측 메뉴에서 "API keys"를 선택합니다</li>
                <li>"Create new secret key" 버튼을 클릭합니다</li>
                <li>키 이름을 입력하고 생성합니다</li>
                <li>
                  <strong>생성된 키는 한 번만 표시되므로</strong> 반드시 복사하여
                  안전한 곳에 보관하세요
                </li>
              </ol>
            </section>

            {/* 보안 주의사항 */}
            <section className="api-key-guide-section">
              <h3 className="api-key-guide-section-title warning">
                <Shield size={18} />
                보안 주의사항
              </h3>
              <ul className="api-key-guide-list">
                <li>
                  <strong>절대 공개하지 마세요</strong>
                  <br />
                  <span className="api-key-guide-note">
                    API 키는 비밀번호와 같습니다. 공개 저장소, 코드, 스크린샷 등에
                    노출하지 마세요.
                  </span>
                </li>
                <li>
                  <strong>키는 암호화되어 저장됩니다</strong>
                  <br />
                  <span className="api-key-guide-note">
                    입력하신 API 키는 서버에서 안전하게 암호화되어 저장되며, 본인만
                    확인할 수 있습니다.
                  </span>
                </li>
                <li>
                  <strong>의심스러운 활동 발견 시 즉시 삭제</strong>
                  <br />
                  <span className="api-key-guide-note">
                    키가 유출되었다고 생각되면 OpenAI Platform에서 즉시 삭제하고
                    새 키를 발급받으세요.
                  </span>
                </li>
                <li>
                  <strong>불필요한 키는 삭제하세요</strong>
                  <br />
                  <span className="api-key-guide-note">
                    사용하지 않는 키는 보안을 위해 삭제하는 것이 좋습니다.
                  </span>
                </li>
              </ul>
            </section>

            {/* 사용 모델 정보 */}
            <section className="api-key-guide-section">
              <h3 className="api-key-guide-section-title">
                <Cpu size={18} />
                사용 모델
              </h3>
              <div className="api-key-guide-model-info">
                <div className="api-key-guide-model-badge">
                  <strong>gpt-4o-mini</strong>
                </div>
                <p className="api-key-guide-model-description">
                  본 서비스는 <strong>gpt-4o-mini</strong> 모델을 사용합니다. 이 모델은
                  빠른 응답 속도와 효율적인 비용으로 최적화되어 있으며, 다양한 작업에
                  적합합니다.
                </p>
                <p className="api-key-guide-model-note">
                  API 키를 입력하시면 gpt-4o-mini 모델 호출에 사용됩니다.
                </p>
              </div>
            </section>

            {/* 사용량 및 요금 */}
            <section className="api-key-guide-section">
              <h3 className="api-key-guide-section-title">
                <DollarSign size={18} />
                사용량 및 요금
              </h3>
              <ul className="api-key-guide-list">
                <li>
                  API 사용량에 따라{" "}
                  <a
                    href="https://openai.com/api/pricing"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="api-key-guide-link"
                  >
                    OpenAI 요금
                    <ExternalLink size={14} />
                  </a>
                  이 부과됩니다
                </li>
                <li>
                  사용량은 OpenAI Platform의{" "}
                  <a
                    href="https://platform.openai.com/usage"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="api-key-guide-link"
                  >
                    Usage 대시보드
                    <ExternalLink size={14} />
                  </a>
                  에서 확인할 수 있습니다
                </li>
                <li>
                  본 서비스에서도 사용량 통계를 확인할 수 있으며, 사용자 API 키와
                  공용 키 사용량을 구분하여 표시됩니다
                </li>
                <li>
                  <strong>사용량 제한을 설정</strong>하여 예상치 못한 비용을 방지할
                  수 있습니다
                </li>
              </ul>
            </section>

            {/* 중요 알림 */}
            <div className="api-key-guide-alert">
              <AlertTriangle size={20} />
              <div>
                <strong>중요:</strong> API 키를 입력하면 해당 키로 모든 GPT 호출이
                이루어집니다. 사용량을 정기적으로 모니터링하고, 필요시 OpenAI
                Platform에서 사용량 제한을 설정하세요.
              </div>
            </div>
          </div>

          <div className="api-key-guide-footer">
            <button
              className="api-key-guide-btn api-key-guide-btn-primary"
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


