import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";

const dummyList = ["REST API", "JWT 인증", "TCP/IP", "CORS"];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0 },
};

export default function KnowledgeSection({
  title,
  cards = undefined,
  onCardClick,
}) {
  const navigate = useNavigate();
  // cards prop이 있으면 실제 데이터 사용, 없으면 더미 데이터 사용
  const displayCards = cards || dummyList;

  const handleCardClick = (item) => {
    // 실제 카드 데이터인 경우에만 모달 열기 또는 상세 페이지로 이동
    if (typeof item === "object" && item !== null) {
      if (onCardClick) {
        // 모달 열기 콜백이 있으면 모달 사용
        onCardClick(item);
      } else {
        // 없으면 기존처럼 페이지 이동 (하위 호환성)
        const slug = item.slug;
        if (slug) {
          navigate(`/knowledge/cards/${slug}`);
        } else {
          const cardId = item.id || item.sourceId;
          if (cardId) {
            navigate(`/knowledge/cards/${cardId}`);
          }
        }
      }
    }
  };

  return (
    <section className="knowledge-section">
      <h3>{title}</h3>

      <motion.div
        className="knowledge-card-list"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {displayCards.map((item, index) => {
          // cards가 배열인 경우 (실제 API 데이터)
          if (typeof item === "object" && item !== null) {
            // 카드별 색상 테마 (인덱스 기반) - 트렌디한 무채색 톤
            const colorThemes = [
              { color: "#6c63ff", light: "#f5f4ff" },
              { color: "#4a5568", light: "#f7fafc" },
              { color: "#2d3748", light: "#f7fafc" },
              { color: "#718096", light: "#f7fafc" },
              { color: "#805ad5", light: "#faf5ff" },
              { color: "#4299e1", light: "#ebf8ff" },
            ];
            const theme = colorThemes[index % colorThemes.length];
            const cardStyle = {
              "--card-accent": theme.color,
              "--card-light": theme.light,
            };

            return (
              <motion.div
                key={item.id || item.sourceId || index}
                className="knowledge-card"
                variants={itemVariants}
                onClick={() => handleCardClick(item)}
                style={{ cursor: "pointer" }}
              >
                {/* @ts-expect-error - CSS 변수는 React.CSSProperties에 포함되지 않음 */}
                <div style={cardStyle}>
                  <div className="knowledge-card-header">
                    <img
                      src={`https://picsum.photos/400/200?random=${
                        item.id || item.sourceId || index
                      }`}
                      alt={item.title || "Knowledge card"}
                      className="knowledge-card-image"
                      onError={(e) => {
                        // 이미지 로드 실패 시 대체 이미지
                        const target = e.target;
                        if (target instanceof HTMLImageElement) {
                          target.src = `https://picsum.photos/400/200?random=${Date.now()}`;
                        }
                      }}
                    />
                    <div className="knowledge-card-overlay"></div>
                  </div>
                  <div className="knowledge-card-body">
                    <h4>{item.title || "제목 없음"}</h4>
                    <p className="knowledge-card-description">
                      {item.oneLineDefinition || "설명 없음"}
                    </p>
                    <div className="knowledge-card-stats">
                      {item.corePoints && item.corePoints.length > 0 && (
                        <div className="knowledge-card-badge">
                          <span className="badge-icon">✨</span>
                          <span>핵심 포인트 {item.corePoints.length}개</span>
                        </div>
                      )}
                      {item.commonMistakes &&
                        item.commonMistakes.length > 0 && (
                          <div className="knowledge-card-badge">
                            <span className="badge-icon">⚠️</span>
                            <span>
                              흔한 실수 {item.commonMistakes.length}개
                            </span>
                          </div>
                        )}
                    </div>
                    {item.sourceType && (
                      <div className="knowledge-card-source">
                        <span className="source-icon">
                          {item.sourceType === "CHAT" ? "💬" : "📝"}
                        </span>
                        <span>
                          {item.sourceType === "CHAT"
                            ? "챗봇"
                            : item.sourceType}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              </motion.div>
            );
          }
          // 더미 데이터인 경우 (문자열)
          const colorThemes = [
            { color: "#6c63ff", light: "#f5f4ff" },
            { color: "#4a5568", light: "#f7fafc" },
            { color: "#2d3748", light: "#f7fafc" },
            { color: "#718096", light: "#f7fafc" },
          ];
          const theme = colorThemes[index % colorThemes.length];
          const cardStyle = {
            "--card-accent": theme.color,
            "--card-light": theme.light,
          };

          return (
            <motion.div
              key={item}
              className="knowledge-card"
              variants={itemVariants}
              whileHover={{ scale: 1.03, y: -8, rotateY: 2 }}
            >
              {/* @ts-expect-error - CSS 변수는 React.CSSProperties에 포함되지 않음 */}
              <div style={cardStyle}>
                <div className="knowledge-card-header">
                  <img
                    src={`https://picsum.photos/400/200?random=${index}`}
                    alt={item}
                    className="knowledge-card-image"
                  />
                  <div className="knowledge-card-overlay"></div>
                </div>
                <div className="knowledge-card-body">
                  <h4>{item}</h4>
                  <p className="knowledge-card-description">
                    개념 요약 미리보기...
                  </p>
                </div>
              </div>
            </motion.div>
          );
        })}
      </motion.div>
    </section>
  );
}
