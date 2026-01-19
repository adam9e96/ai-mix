import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import axiosInstance from "../../api/axiosInstance";
import { Eye, ThumbsUp } from "lucide-react";
import KnowledgeDetailModal from "./KnowledgeDetailModal";

export default function KnowledgeSidebar() {
  const [viewRanking, setViewRanking] = useState([]);
  const [upvoteRanking, setUpvoteRanking] = useState([]);
  const [activeRanking, setActiveRanking] = useState("views"); // 'views' or 'upvotes'
  const [loadingViews, setLoadingViews] = useState(false);
  const [loadingUpvotes, setLoadingUpvotes] = useState(false);
  const [selectedCard, setSelectedCard] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // 랭킹 데이터 가져오기
  const fetchRanking = async (type) => {
    if (type === "views") {
      setLoadingViews(true);
    } else {
      setLoadingUpvotes(true);
    }

    try {
      // 백엔드 API 엔드포인트 결정
      const endpoint =
        type === "views"
          ? "/knowledge/cards/top10/views"
          : "/knowledge/cards/top10/likes";

      const response = await axiosInstance.get(endpoint);

      // API 응답 데이터 처리
      const data = response.data || {};
      // 배열이거나 content 필드에 배열이 있는 경우 처리
      const cardsData = Array.isArray(data)
        ? data
        : data.content || data.data || data.cards || [];

      if (type === "views") {
        setViewRanking(cardsData);
      } else {
        setUpvoteRanking(cardsData);
      }
    } catch (error) {
      console.error(`${type} 랭킹 불러오기 실패:`, error);
      console.error("에러 상세:", error.response?.data);

      // 에러 발생 시 빈 배열로 설정
      if (type === "views") {
        setViewRanking([]);
      } else {
        setUpvoteRanking([]);
      }
    } finally {
      if (type === "views") {
        setLoadingViews(false);
      } else {
        setLoadingUpvotes(false);
      }
    }
  };

  useEffect(() => {
    fetchRanking("views");
    fetchRanking("upvotes");
  }, []);

  const handleCardClick = (card) => {
    setSelectedCard(card);
    setShowDetailModal(true);
  };

  const currentRanking =
    activeRanking === "views" ? viewRanking : upvoteRanking;

  return (
    <aside className="knowledge-sidebar">
      {/* 랭킹 섹션 */}
      <div className="knowledge-ranking-section">
        <div className="ranking-tabs">
          <button
            className={`ranking-tab ${
              activeRanking === "views" ? "active" : ""
            }`}
            onClick={() => setActiveRanking("views")}
          >
            <Eye size={16} />
            조회수
          </button>
          <button
            className={`ranking-tab ${
              activeRanking === "upvotes" ? "active" : ""
            }`}
            onClick={() => setActiveRanking("upvotes")}
          >
            <ThumbsUp size={16} />
            추천수
          </button>
        </div>

        {(activeRanking === "views" ? loadingViews : loadingUpvotes) ? (
          <div className="ranking-loading">불러오는 중...</div>
        ) : (
          <div className="ranking-list">
            {currentRanking.length > 0 ? (
              currentRanking.map((card, index) => (
                <div
                  key={card.id || index}
                  className="ranking-item"
                  onClick={() => handleCardClick(card)}
                >
                  <div className="ranking-rank">
                    {index + 1 <= 3 ? (
                      <span className={`rank-badge rank-${index + 1}`}>
                        {index + 1}
                      </span>
                    ) : (
                      <span className="rank-number">{index + 1}</span>
                    )}
                  </div>
                  <div className="ranking-content">
                    <div className="ranking-title">
                      {card.title || "제목 없음"}
                    </div>
                    <div className="ranking-count">
                      {activeRanking === "views" ? (
                        <>
                          <Eye size={14} />
                          <span>{card.viewCount || 0}</span>
                        </>
                      ) : (
                        <>
                          <ThumbsUp size={14} />
                          <span>{card.upvoteCount || 0}</span>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="ranking-empty">랭킹 데이터가 없습니다.</div>
            )}
          </div>
        )}
      </div>

      {/* 카드 상세 모달 */}
      <AnimatePresence>
        {showDetailModal && selectedCard && (
          <KnowledgeDetailModal
            card={selectedCard}
            onClose={() => {
              setShowDetailModal(false);
              setSelectedCard(null);
            }}
            onUpdate={() => {
              // 사이드바에서는 업데이트 후 특별한 작업이 필요 없음
            }}
          />
        )}
      </AnimatePresence>
    </aside>
  );
}
