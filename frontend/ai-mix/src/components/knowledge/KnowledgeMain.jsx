import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Link } from "react-router-dom";
import KnowledgeSection from "./KnowledgeSection";
import KnowledgeDetailModal from "./KnowledgeDetailModal";
import axiosInstance from "../../api/axiosInstance";
import { toast } from "react-toastify";
import { ChevronLeft, ChevronRight, MessageCircle } from "lucide-react";
import { IoInformationCircleSharp } from "react-icons/io5";
import { Tooltip } from "react-tooltip";
import { FaRectangleList, FaBook } from "react-icons/fa6";
import { HiLightBulb, HiKey, HiXMark, HiLink } from "react-icons/hi2";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import LoginModal from "@/components/modal/LoginModal";

export default function KnowledgeMain() {
  const { isAuthenticated } = useAuthStore();
  const { showLoginModal, setShowLoginModal } = useUIStore();
  const [allCards, setAllCards] = useState([]);
  const [myCards, setMyCards] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("all-cards");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalCount, setTotalCount] = useState(0);
  const [selectedCard, setSelectedCard] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [allCardsPageInfo, setAllCardsPageInfo] = useState({
    page: 0,
    size: 9,
    totalPages: 1,
    totalElements: 0,
  });
  const [myCardsPageInfo, setMyCardsPageInfo] = useState({
    page: 0,
    size: 9,
    totalPages: 1,
    totalElements: 0,
  });

  // 전체 카드 가져오기 (공개된 카드만)
  const fetchAllCards = async (page = 0) => {
    setLoading(true);
    try {
      const response = await axiosInstance.get("/knowledge/cards", {
        params: {
          page: page,
          size: 9,
          isPublished: true, // 공개된 카드만
        },
      });

      // Spring Page 객체 구조 처리
      const data = response.data || {};
      const pageData = data.page || data;

      const cardsData = Array.isArray(data)
        ? data
        : data.content || pageData.content || data.data || [];

      const newPageInfo = {
        page: Number.isFinite(Number(pageData.number))
          ? Number(pageData.number)
          : page,
        size: Number.isFinite(Number(pageData.size))
          ? Number(pageData.size)
          : 9,
        totalPages: Number.isFinite(Number(pageData.totalPages))
          ? Number(pageData.totalPages)
          : 1,
        totalElements: Number.isFinite(Number(pageData.totalElements))
          ? Number(pageData.totalElements)
          : cardsData.length,
      };

      setAllCards(cardsData);
      setAllCardsPageInfo(newPageInfo);
    } catch (error) {
      console.error("전체 카드 불러오기 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "카드를 불러오는 중 오류가 발생했습니다.";
      toast.error(errorMessage);
      setAllCards([]);
    } finally {
      setLoading(false);
    }
  };

  // 내 카드 가져오기 (공개/비공개 모두)
  const fetchMyCards = async (page = 0) => {
    setLoading(true);
    try {
      const response = await axiosInstance.get("/knowledge/cards/my-cards", {
        params: {
          page: page,
          size: 9,
        },
      });

      // Spring Page 객체 구조 처리
      const data = response.data || {};
      const pageData = data.page || data;

      const cardsData = Array.isArray(data)
        ? data
        : data.content || pageData.content || data.data || [];

      const newPageInfo = {
        page: Number.isFinite(Number(pageData.number))
          ? Number(pageData.number)
          : page,
        size: Number.isFinite(Number(pageData.size))
          ? Number(pageData.size)
          : 9,
        totalPages: Number.isFinite(Number(pageData.totalPages))
          ? Number(pageData.totalPages)
          : 1,
        totalElements: Number.isFinite(Number(pageData.totalElements))
          ? Number(pageData.totalElements)
          : cardsData.length,
      };

      setMyCards(cardsData);
      setMyCardsPageInfo(newPageInfo);
    } catch (error) {
      console.error("내 카드 불러오기 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "내 카드를 불러오는 중 오류가 발생했습니다.";
      toast.error(errorMessage);
      setMyCards([]);
    } finally {
      setLoading(false);
    }
  };

  // 전체 카드 개수 가져오기
  const fetchTotalCount = async () => {
    try {
      const response = await axiosInstance.get("/knowledge/cards/count");
      const count = response.data?.totalCount || response.data?.count || 0;
      setTotalCount(count);
    } catch (error) {
      console.error("카드 개수 불러오기 실패:", error);
      // 에러 발생 시 기본값 유지
    }
  };

  useEffect(() => {
    // 컴포넌트 마운트 시 전체 카드 개수 가져오기
    fetchTotalCount();
  }, []);

  useEffect(() => {
    if (activeTab === "all-cards") {
      setCurrentPage(0);
      fetchAllCards(0);
    } else if (activeTab === "my-cards") {
      setCurrentPage(0);
      // 로그인된 경우에만 내 카드 가져오기
      if (isAuthenticated()) {
        fetchMyCards(0);
      } else {
        setMyCards([]);
        setLoading(false);
      }
    }
  }, [activeTab]);

  useEffect(() => {
    if (activeTab === "all-cards") {
      fetchAllCards(currentPage);
    } else if (activeTab === "my-cards") {
      // 로그인된 경우에만 내 카드 가져오기
      if (isAuthenticated()) {
        fetchMyCards(currentPage);
      }
    }
  }, [currentPage]);

  const tabs = [
    {
      id: "all-cards",
      label: (
        <>
          <FaRectangleList size={16} />
          <span> 전체 카드</span>
        </>
      ),
    },
    {
      id: "my-cards",
      label: (
        <>
          <FaBook size={16} />
          <span> 내 카드</span>
        </>
      ),
    },
  ];

  const renderContent = () => {
    const getCurrentCards = () => {
      return activeTab === "all-cards" ? allCards : myCards;
    };

    const getCurrentPageInfo = () => {
      return activeTab === "all-cards" ? allCardsPageInfo : myCardsPageInfo;
    };

    const getTitle = () => {
      return activeTab === "all-cards" ? (
        <>
          <FaRectangleList size={16} />
          <span> 전체 카드</span>
        </>
      ) : (
        <>
          <FaBook size={16} />
          <span> 내 카드</span>
        </>
      );
    };

    const getEmptyMessage = () => {
      return activeTab === "all-cards"
        ? "공개된 카드가 없습니다."
        : "아직 저장한 카드가 없습니다.";
    };

    if (loading) {
      return (
        <div
          style={{
            textAlign: "center",
            padding: "3rem",
            color: "#666",
            fontSize: "1.1rem",
          }}
        >
          불러오는 중...
        </div>
      );
    }

    // "내 카드" 탭이고 로그인되지 않은 경우 빈 상태 메시지 표시
    if (activeTab === "my-cards" && !isAuthenticated()) {
      return (
        <div className="knowledge-empty-state">
          <MessageCircle size={48} className="knowledge-empty-state-icon" />
          <p className="knowledge-empty-state-text">로그인이 필요합니다.</p>
          <p className="knowledge-empty-state-subtext">
            로그인 후 내 카드를 확인하세요!
          </p>
          <button
            className="knowledge-empty-state-button"
            onClick={() => setShowLoginModal(true)}
          >
            로그인하기
          </button>
        </div>
      );
    }

    const currentCards = getCurrentCards();
    const currentPageInfo = getCurrentPageInfo();

    if (currentCards.length > 0) {
      return (
        <>
          <KnowledgeSection
            title={getTitle()}
            cards={currentCards}
            onCardClick={(card) => {
              setSelectedCard(card);
              setShowDetailModal(true);
            }}
          />
          {currentPageInfo.totalPages > 1 && (
            <div className="knowledge-pagination">
              <button
                className="knowledge-pagination-btn"
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage === 0}
                aria-label="이전 페이지"
              >
                <ChevronLeft size={20} />
              </button>
              <span className="knowledge-pagination-info">
                {currentPage + 1} / {currentPageInfo.totalPages}
              </span>
              <button
                className="knowledge-pagination-btn"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage >= currentPageInfo.totalPages - 1}
                aria-label="다음 페이지"
              >
                <ChevronRight size={20} />
              </button>
            </div>
          )}
        </>
      );
    }

    return (
      <div
        style={{
          textAlign: "center",
          padding: "3rem",
          color: "#666",
          fontSize: "1.1rem",
        }}
      >
        {getEmptyMessage()}
      </div>
    );
  };

  // 숫자 포맷팅 (천 단위 콤마)
  const formatNumber = (num) => {
    return num.toLocaleString("ko-KR");
  };

  return (
    <main className="knowledge-main">
      {/* 헤더 */}
      <div className="knowledge-header">
        <div className="knowledge-title-wrapper">
          <Link
            to="/knowledge"
            className="knowledge-title-link"
            onClick={(e) => {
              if (window.location.pathname === "/knowledge") {
                e.preventDefault();
                window.location.reload();
              }
            }}
          >
            <h2 className="knowledge-title">지식카드 목록</h2>
          </Link>
          <button
            className="knowledge-info-icon"
            data-tooltip-id="knowledge-info-tooltip"
            data-tooltip-html="핵심 개념을 카드 형태로 정리하고 공유하세요.<br />챗봇 대화, QnA, 배틀에서 자동으로 생성할 수 있습니다."
          >
            <IoInformationCircleSharp size={20} />
          </button>
          <Tooltip
            id="knowledge-info-tooltip"
            place="bottom"
            className="knowledge-info-tooltip"
          />
        </div>
      </div>

      <div className="knowledge-summary">
        <FaBook
          size={16}
          style={{ marginRight: "6px", verticalAlign: "middle" }}
        />
        지금까지 축적된 지식 <strong>{formatNumber(totalCount)}</strong>개
      </div>

      {/* 탭 네비게이션 */}
      <div className="knowledge-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`knowledge-tab ${activeTab === tab.id ? "active" : ""}`}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* 탭 컨텐츠 */}
      <AnimatePresence mode="wait">
        <motion.div
          key={activeTab}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -20 }}
          transition={{ duration: 0.3 }}
          className="knowledge-tab-content"
        >
          {renderContent()}
        </motion.div>
      </AnimatePresence>

      {/* 카드 상세 모달 */}
      <AnimatePresence>
        {showDetailModal && selectedCard && (
          <KnowledgeDetailModal
            card={selectedCard}
            onClose={() => {
              setShowDetailModal(false);
              setSelectedCard(null);
            }}
            onUpdate={(updatedCard, deletedCardId) => {
              if (deletedCardId) {
                // 카드 삭제된 경우 목록에서 제거
                if (activeTab === "all-cards") {
                  setAllCards((prev) =>
                    prev.filter((c) => c.id !== deletedCardId)
                  );
                  setAllCardsPageInfo((prev) => ({
                    ...prev,
                    totalElements: Math.max(0, prev.totalElements - 1),
                  }));
                } else if (activeTab === "my-cards") {
                  setMyCards((prev) =>
                    prev.filter((c) => c.id !== deletedCardId)
                  );
                  setMyCardsPageInfo((prev) => ({
                    ...prev,
                    totalElements: Math.max(0, prev.totalElements - 1),
                  }));
                }
                setShowDetailModal(false);
                setSelectedCard(null);
              } else if (updatedCard) {
                // 카드 수정된 경우 목록에서 업데이트
                if (activeTab === "all-cards") {
                  setAllCards((prev) =>
                    prev.map((c) => (c.id === updatedCard.id ? updatedCard : c))
                  );
                } else if (activeTab === "my-cards") {
                  setMyCards((prev) =>
                    prev.map((c) => (c.id === updatedCard.id ? updatedCard : c))
                  );
                }
                // 선택된 카드도 업데이트
                setSelectedCard(updatedCard);
              }
            }}
          />
        )}
      </AnimatePresence>

      {/* 로그인 모달 */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onSuccess={() => {
            // 로그인 성공 후 내 카드 탭이면 카드 다시 불러오기
            if (activeTab === "my-cards") {
              fetchMyCards(0);
            }
          }}
        />
      )}
    </main>
  );
}
