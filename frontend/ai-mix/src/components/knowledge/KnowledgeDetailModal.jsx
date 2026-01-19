import { useEffect, useState, useRef } from "react";
import { createPortal } from "react-dom";
import { motion } from "framer-motion";
import { X } from "lucide-react";
import axiosInstance from "../../api/axiosInstance";
import { toast } from "react-toastify";
import {
  Eye,
  ThumbsUp,
  Share2,
  Heart,
  Info,
  BookOpen,
  Sparkles,
  Edit,
  Trash2,
  Copy,
  Check,
  Download,
  Image as ImageIcon,
  Link2,
} from "lucide-react";
import { HiLightBulb, HiKey, HiXMark, HiLink } from "react-icons/hi2";
import { Tooltip } from "react-tooltip";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import LoginModal from "@/components/modal/LoginModal";
import ToggleSwitch from "@/components/common/ToggleSwitch";
import "@styles/pages/knowledge-detail.css";
import "@styles/components/knowledge-detail-modal.css";
import "@styles/pages/chat.css";

export default function KnowledgeDetailModal({
  card: initialCard,
  onClose,
  onUpdate,
}) {
  const { user, isAuthenticated } = useAuthStore();
  const { showLoginModal, setShowLoginModal } = useUIStore();
  const [card, setCard] = useState(initialCard);
  const [relatedCards, setRelatedCards] = useState([]);
  const [mistakes, setMistakes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isFlipped, setIsFlipped] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [isLiking, setIsLiking] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isCopied, setIsCopied] = useState(false);
  const [editedCardData, setEditedCardData] = useState(null);
  const frontCardRef = useRef(null);
  const backCardRef = useRef(null);
  const cardContainerRef = useRef(null);

  useEffect(() => {
    const fetchCardDetail = async () => {
      if (!initialCard) return;

      setLoading(true);
      try {
        const slug = initialCard.slug || initialCard.id;
        const response = await axiosInstance.get(`/knowledge/cards/${slug}`);

        const responseData = response.data;

        if (!responseData || !responseData.card) {
          console.error("유효하지 않은 응답 구조:", responseData);
          toast.error("카드 정보를 불러올 수 없습니다.");
          onClose();
          return;
        }

        const cardData = responseData.card;
        const relatedCardsData = responseData.relatedCards || [];
        const mistakesData = responseData.mistakes || [];

        // 디버깅: 카드 API 응답 구조 확인
        console.log("카드 API 응답:", {
          "카드 ID": cardData?.id,
          contributorId: cardData?.contributorId,
          authorId: cardData?.authorId,
          contributorEmail: cardData?.contributorEmail,
          authorEmail: cardData?.authorEmail,
          "카드 전체 키": cardData ? Object.keys(cardData) : [],
        });

        setCard(cardData);
        setRelatedCards(relatedCardsData);
        setMistakes(mistakesData);

        // 초기 좋아요 상태 설정
        const initialIsLiked = Boolean(cardData.isLiked);
        setIsLiked(initialIsLiked);
      } catch (error) {
        console.error("카드 상세 정보 불러오기 실패:", error);
        toast.error("카드 정보를 불러오는 중 오류가 발생했습니다.");
        onClose();
      } finally {
        setLoading(false);
      }
    };

    fetchCardDetail();
  }, [initialCard, onClose]);

  // 카드 높이 동적 조정
  useEffect(() => {
    if (
      card &&
      frontCardRef.current &&
      backCardRef.current &&
      cardContainerRef.current
    ) {
      const adjustCardHeight = () => {
        const viewportHeight = window.innerHeight;
        const frontHeight = frontCardRef.current.scrollHeight;
        const backHeight = backCardRef.current.scrollHeight;
        const contentHeight = Math.max(frontHeight, backHeight);

        // 뷰포트 높이의 90%를 최대값으로 설정
        const maxViewportHeight = viewportHeight * 0.9;

        // 컨텐츠 높이와 뷰포트 제한 중 작은 값 사용
        const finalHeight = Math.min(contentHeight, maxViewportHeight);

        // 최소 높이 보장
        const minHeight = 500;
        const adjustedHeight = Math.max(finalHeight, minHeight);

        if (cardContainerRef.current) {
          cardContainerRef.current.style.height = `${adjustedHeight}px`;
        }
        if (frontCardRef.current) {
          frontCardRef.current.style.height = `${adjustedHeight}px`;
        }
        if (backCardRef.current) {
          backCardRef.current.style.height = `${adjustedHeight}px`;
        }
      };

      // 초기 조정
      adjustCardHeight();

      // 리사이즈 이벤트 리스너 (디바운싱 적용)
      let resizeTimeout;
      const handleResize = () => {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(adjustCardHeight, 150);
      };

      window.addEventListener("resize", handleResize);

      return () => {
        window.removeEventListener("resize", handleResize);
        clearTimeout(resizeTimeout);
      };
    }
  }, [card, isFlipped]);

  // 카드 데이터가 변경될 때 좋아요 상태 동기화
  useEffect(() => {
    if (card) {
      const shouldBeLiked = Boolean(card.isLiked);
      setIsLiked(shouldBeLiked);
    }
  }, [card?.isLiked]);

  // 작성자 확인: 로그인한 사용자가 카드 작성자인 경우만 true
  const isOwner = (() => {
    if (!card || !user) {
      if (import.meta.env.DEV) {
        console.log("작성자 확인 실패: card 또는 user가 없음", {
          hasCard: !!card,
          hasUser: !!user,
        });
      }
      return false;
    }

    // ID 비교 (숫자 또는 문자열 모두 처리)
    const userId = user.id;
    const userEmail = user.email;
    const userNickname = user.nickname;

    const cardContributorId = card.contributorId;
    const cardAuthorId = card.authorId;
    const cardContributorEmail = card.contributorEmail;
    const cardAuthorEmail = card.authorEmail;
    const cardContributorNickname = card.contributorNickname;
    const cardAuthorNickname = card.authorNickname;

    // ID 비교 (타입 변환 포함)
    const isContributorById =
      userId != null &&
      cardContributorId != null &&
      String(userId) === String(cardContributorId);

    const isAuthorById =
      userId != null &&
      cardAuthorId != null &&
      String(userId) === String(cardAuthorId);

    // 이메일 비교 (대소문자 무시)
    const isContributorByEmail =
      userEmail &&
      cardContributorEmail &&
      userEmail.toLowerCase().trim() ===
        cardContributorEmail.toLowerCase().trim();

    const isAuthorByEmail =
      userEmail &&
      cardAuthorEmail &&
      userEmail.toLowerCase().trim() === cardAuthorEmail.toLowerCase().trim();

    // 닉네임 비교 (대소문자 무시, 공백 제거)
    const isContributorByNickname =
      userNickname &&
      cardContributorNickname &&
      userNickname.toLowerCase().trim() ===
        cardContributorNickname.toLowerCase().trim();

    const isAuthorByNickname =
      userNickname &&
      cardAuthorNickname &&
      userNickname.toLowerCase().trim() ===
        cardAuthorNickname.toLowerCase().trim();

    // ID, 이메일, 닉네임 중 하나라도 일치하면 작성자
    const isContributor =
      isContributorById || isContributorByEmail || isContributorByNickname;
    const isAuthor = isAuthorById || isAuthorByEmail || isAuthorByNickname;
    const result = isContributor || isAuthor;

    // 디버깅 정보 출력 (항상 출력)
    console.log("작성자 확인:", {
      "카드 ID": card.id,
      "카드 contributorId": cardContributorId,
      "카드 authorId": cardAuthorId,
      "카드 contributorEmail": cardContributorEmail,
      "카드 authorEmail": cardAuthorEmail,
      "카드 contributorNickname": cardContributorNickname,
      "카드 authorNickname": cardAuthorNickname,
      "로그인 사용자 ID": userId,
      "로그인 사용자 이메일": userEmail,
      "로그인 사용자 닉네임": userNickname,
      isContributorById: isContributorById,
      isAuthorById: isAuthorById,
      isContributorByEmail: isContributorByEmail,
      isAuthorByEmail: isAuthorByEmail,
      isContributorByNickname: isContributorByNickname,
      isAuthorByNickname: isAuthorByNickname,
      "최종 결과 (isOwner)": result,
    });

    return result;
  })();

  // 수정 핸들러
  const handleEdit = (e) => {
    e.stopPropagation();
    if (!isAuthenticated()) {
      setShowLoginModal(true);
      return;
    }
    if (!card || !isOwner) {
      toast.error("작성자만 수정할 수 있습니다.", {
        position: "top-right",
        autoClose: 2000,
        style: { zIndex: 1000001 },
      });
      return;
    }

    // 현재 카드 데이터로 편집 데이터 초기화
    setEditedCardData({
      title: card.title || "",
      oneLineDefinition: card.oneLineDefinition || "",
      corePoints: card.corePoints || [],
      commonMistakes: card.commonMistakes || [],
      isPublished: card.isPublished !== false,
    });
    setShowEditModal(true);
  };

  // 삭제 핸들러
  const handleDelete = (e) => {
    e.stopPropagation();
    if (!isAuthenticated()) {
      setShowLoginModal(true);
      return;
    }
    if (!card || !isOwner) {
      toast.error("작성자만 삭제할 수 있습니다.", {
        position: "top-right",
        autoClose: 2000,
        style: { zIndex: 1000001 },
      });
      return;
    }
    setShowDeleteModal(true);
  };

  // 삭제 확인 핸들러
  const handleConfirmDelete = async () => {
    if (!card || !card.id) return;

    setIsDeleting(true);
    try {
      await axiosInstance.delete(`/knowledge/cards/${card.id}`);
      toast.success("카드가 삭제되었습니다.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000001 },
      });
      setShowDeleteModal(false);

      // 부모 컴포넌트에 삭제 알림
      if (onUpdate) {
        onUpdate(null, card.id); // 삭제된 카드 ID 전달
      }

      onClose();
    } catch (error) {
      console.error("카드 삭제 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "카드 삭제에 실패했습니다.";
      toast.error(errorMessage, {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000001 },
      });
    } finally {
      setIsDeleting(false);
    }
  };

  // 카드 데이터 수정 핸들러
  const handleCardDataChange = (field, value) => {
    setEditedCardData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAddArrayItem = (field) => {
    setEditedCardData((prev) => ({
      ...prev,
      [field]: [...(prev[field] || []), ""],
    }));
  };

  const handleUpdateArrayItem = (field, index, value) => {
    setEditedCardData((prev) => {
      const newArray = [...(prev[field] || [])];
      newArray[index] = value;
      return {
        ...prev,
        [field]: newArray,
      };
    });
  };

  const handleRemoveArrayItem = (field, index) => {
    setEditedCardData((prev) => {
      const newArray = [...(prev[field] || [])];
      newArray.splice(index, 1);
      return {
        ...prev,
        [field]: newArray,
      };
    });
  };

  // 카드 수정 확인 핸들러
  const handleConfirmUpdate = async () => {
    if (!card || !card.id || !editedCardData) return;

    // 필수 필드 검증
    if (!editedCardData.oneLineDefinition?.trim()) {
      toast.error("한 줄 정의를 입력해주세요.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000001 },
      });
      return;
    }

    setIsUpdating(true);
    try {
      // 빈 문자열 제거 및 데이터 정리
      const cleanedCorePoints = (editedCardData.corePoints || []).filter(
        (point) => point && point.trim()
      );
      const cleanedCommonMistakes = (
        editedCardData.commonMistakes || []
      ).filter((mistake) => mistake && mistake.trim());

      // API 요청 데이터 구성 (null로 보내면 해당 필드는 수정되지 않음)
      const requestData = {
        title: editedCardData.title?.trim() || null,
        oneLineDefinition: editedCardData.oneLineDefinition.trim(),
        corePoints: cleanedCorePoints.length > 0 ? cleanedCorePoints : null,
        commonMistakes:
          cleanedCommonMistakes.length > 0 ? cleanedCommonMistakes : null,
        isPublished: editedCardData.isPublished !== false,
      };

      const response = await axiosInstance.put(
        `/knowledge/cards/${card.id}`,
        requestData
      );

      // 카드 데이터 업데이트
      setCard(response.data);

      toast.success("카드가 수정되었습니다.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000001 },
      });

      setShowEditModal(false);
      setEditedCardData(null);

      // 부모 컴포넌트에 업데이트 알림
      if (onUpdate) {
        onUpdate(response.data);
      }
    } catch (error) {
      console.error("카드 수정 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "카드 수정에 실패했습니다.";
      toast.error(errorMessage, {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000001 },
      });
    } finally {
      setIsUpdating(false);
    }
  };

  // 공유 URL 생성
  const getShareUrl = () => {
    if (!card) return "";
    const slug = card.slug || card.id;
    return `${window.location.origin}/knowledge/cards/${slug}`;
  };

  // 클립보드 복사 함수
  const handleCopyToClipboard = async () => {
    const url = getShareUrl();
    try {
      await navigator.clipboard.writeText(url);
      setIsCopied(true);
      toast.success("URL이 클립보드에 복사되었습니다!", {
        position: "top-right",
        autoClose: 2000,
        style: { zIndex: 1000001 },
      });
      setTimeout(() => setIsCopied(false), 2000);
    } catch (error) {
      // 클립보드 API가 실패하면 fallback 사용
      const textArea = document.createElement("textarea");
      textArea.value = url;
      textArea.style.position = "fixed";
      textArea.style.opacity = "0";
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand("copy");
        setIsCopied(true);
        toast.success("URL이 클립보드에 복사되었습니다!", {
          position: "top-right",
          autoClose: 2000,
          style: { zIndex: 1000001 },
        });
        setTimeout(() => setIsCopied(false), 2000);
      } catch (err) {
        toast.error("복사에 실패했습니다.", {
          position: "top-right",
          autoClose: 2000,
          style: { zIndex: 1000001 },
        });
      }
      document.body.removeChild(textArea);
    }
  };

  // 공유 버튼 클릭 핸들러
  const handleShare = (e) => {
    e.stopPropagation();
    setShowShareModal(true);
  };

  // 이미지 저장 함수 (UI만, 기능은 추후 구현)
  const handleSaveImage = () => {
    toast.info("이미지 저장 기능은 준비 중입니다.", {
      position: "top-right",
      autoClose: 2000,
      style: { zIndex: 1000001 },
    });
  };

  // 좋아요 토글 함수
  const handleLikeToggle = async (e) => {
    e.stopPropagation();

    if (!isAuthenticated()) {
      setShowLoginModal(true);
      return;
    }

    if (!card || !card.id || isLiking) {
      return;
    }

    setIsLiking(true);
    try {
      const response = await axiosInstance.post(
        `/knowledge/cards/${card.id}/like`
      );

      const { isLiked: newIsLiked, upvoteCount: newUpvoteCount } =
        response.data || {};

      setCard((prevCard) => ({
        ...prevCard,
        isLiked: newIsLiked,
        upvoteCount: newUpvoteCount,
      }));

      setIsLiked(newIsLiked);

      if (newIsLiked) {
        toast.success("좋아요를 눌렀습니다!");
      } else {
        toast.info("좋아요를 취소했습니다.");
      }
    } catch (error) {
      console.error("좋아요 처리 실패:", error);
      toast.error("좋아요 처리 중 오류가 발생했습니다.");
    } finally {
      setIsLiking(false);
    }
  };

  // ESC 키로 모달 닫기
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [onClose]);

  const modalContent = (
    <>
      {showLoginModal ? null : loading ? (
        <motion.div
          className="knowledge-modal-backdrop"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
        >
          <motion.div
            className="knowledge-modal-container"
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.9, y: 20 }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="knowledge-detail-loading">불러오는 중...</div>
          </motion.div>
        </motion.div>
      ) : !card ? null : (
        <motion.div
          className="knowledge-modal-backdrop"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
        >
          <motion.div
            className="knowledge-modal-container"
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.9, y: 20 }}
            transition={{ type: "spring", damping: 25, stiffness: 300 }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* 플립 카드 */}
            <div
              ref={cardContainerRef}
              className={`knowledge-flip-card ${isFlipped ? "flipped" : ""}`}
              onClick={() => setIsFlipped(!isFlipped)}
            >
              {/* 카드 앞면 */}
              <div ref={frontCardRef} className="knowledge-flip-card-front">
                <div className="holographic-effect"></div>

                {/* 카드 헤더 + 정보 버튼 + 수정/삭제 버튼 */}
                <div className="card-header card-header-with-close">
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "8px",
                      flex: 1,
                    }}
                  >
                    <div className="card-title">
                      {card.title || "제목 없음"}
                    </div>
                    <button
                      type="button"
                      className="card-title-info-icon"
                      onClick={(e) => {
                        e.stopPropagation();
                      }}
                      data-tooltip-id="knowledge-card-info-tooltip"
                      data-tooltip-html="핵심 개념을 카드 형태로 정리하고 공유하세요.<br />챗봇 대화, QnA, 배틀에서 자동으로 생성할 수 있습니다."
                      aria-label="정보"
                    >
                      <Info size={18} />
                    </button>
                  </div>
                  {/* 수정/삭제 버튼 (작성자만 표시) */}
                  {isOwner && (
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                      }}
                    >
                      <button
                        type="button"
                        onClick={handleEdit}
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "4px",
                          padding: "6px 10px",
                          background: "#6c63ff",
                          color: "#fff",
                          border: "none",
                          borderRadius: "6px",
                          cursor: "pointer",
                          fontSize: "12px",
                          fontWeight: "600",
                          transition: "background 0.2s",
                        }}
                        onMouseEnter={(e) => {
                          if (e.target instanceof HTMLButtonElement) {
                            e.target.style.background = "#5a52e5";
                          }
                        }}
                        onMouseLeave={(e) => {
                          if (e.target instanceof HTMLButtonElement) {
                            e.target.style.background = "#6c63ff";
                          }
                        }}
                        title="수정"
                      >
                        <Edit size={14} />
                      </button>
                      <button
                        type="button"
                        onClick={handleDelete}
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "4px",
                          padding: "6px 10px",
                          background: "#fca5a5",
                          color: "#fff",
                          border: "none",
                          borderRadius: "6px",
                          cursor: "pointer",
                          fontSize: "12px",
                          fontWeight: "600",
                          transition: "background 0.2s",
                        }}
                        onMouseEnter={(e) => {
                          if (e.target instanceof HTMLButtonElement) {
                            e.target.style.background = "#f87171";
                          }
                        }}
                        onMouseLeave={(e) => {
                          if (e.target instanceof HTMLButtonElement) {
                            e.target.style.background = "#fca5a5";
                          }
                        }}
                        title="삭제"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  )}
                </div>

                <Tooltip
                  id="knowledge-card-info-tooltip"
                  place="bottom"
                  className="knowledge-info-tooltip"
                />

                {/* 한 줄 정의 */}
                {card.oneLineDefinition && (
                  <div className="one-line-def">
                    <span className="one-line-def-label">
                      <HiLightBulb
                        size={14}
                        style={{ marginRight: "6px", verticalAlign: "middle" }}
                      />
                      한 줄 정의
                    </span>
                    {card.oneLineDefinition}
                  </div>
                )}

                {/* 핵심 포인트 */}
                {card.corePoints &&
                  Array.isArray(card.corePoints) &&
                  card.corePoints.length > 0 && (
                    <div className="key-points">
                      <h3>
                        <HiKey
                          size={16}
                          style={{
                            marginRight: "6px",
                            verticalAlign: "middle",
                          }}
                        />
                        핵심 포인트
                      </h3>
                      <ul>
                        {card.corePoints.map((point, index) => (
                          <li key={index}>{point || ""}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                {/* 자주 틀리는 오해 */}
                {(mistakes.length > 0 ||
                  (card.commonMistakes &&
                    Array.isArray(card.commonMistakes) &&
                    card.commonMistakes.length > 0)) && (
                  <div className="misconception">
                    <h3>
                      <HiXMark
                        size={16}
                        style={{ marginRight: "6px", verticalAlign: "middle" }}
                      />
                      자주 틀리는 오해
                    </h3>
                    {(mistakes.length > 0
                      ? mistakes
                      : card.commonMistakes || []
                    ).map((mistake, index) => {
                      const mistakeText =
                        typeof mistake === "string"
                          ? mistake
                          : mistake.description || mistake.content || mistake;
                      return (
                        <div key={index}>
                          <div className="misconception-text">
                            {mistakeText || ""}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}

                {/* 관련 개념 태그 */}
                {relatedCards.length > 0 && (
                  <div style={{ marginBottom: "10px" }}>
                    <div
                      style={{
                        fontSize: "12px",
                        color: "rgba(0,0,0,0.6)",
                        marginBottom: "8px",
                        fontWeight: "600",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                      }}
                    >
                      <HiLink size={14} />
                      관련 개념
                    </div>
                    <div className="related-concepts">
                      {relatedCards.slice(0, 4).map((relatedCard, index) => (
                        <span key={index} className="concept-tag">
                          {relatedCard.title}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {/* 액션 버튼 */}
                <div
                  className="card-actions-section"
                  onClick={(e) => e.stopPropagation()}
                >
                  <div className="card-actions">
                    <button
                      className={`card-action-btn card-like-btn ${
                        isLiked ? "liked" : ""
                      }`}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleLikeToggle(e);
                      }}
                      disabled={isLiking}
                      title="좋아요"
                    >
                      <Heart
                        size={18}
                        fill={isLiked ? "currentColor" : "none"}
                      />
                      <span>좋아요 {card?.upvoteCount || 0}</span>
                    </button>
                    <button
                      className="card-action-btn card-share-btn"
                      onClick={handleShare}
                      title="공유하기"
                    >
                      <Share2 size={18} />
                      <span>공유</span>
                    </button>
                  </div>
                  {/* 플립 안내 */}
                  <div className="flip-hint">💫 클릭하여 뒤집기</div>
                </div>
              </div>

              {/* 카드 뒷면 */}
              <div ref={backCardRef} className="knowledge-flip-card-back">
                <div className="card-back-content">
                  {/* 백 헤더 */}
                  <div className="back-header">
                    <div className="back-title">
                      카드 정보
                      <button
                        type="button"
                        className="card-title-info-icon"
                        onClick={(e) => e.stopPropagation()}
                        data-tooltip-id="knowledge-card-back-info-tooltip"
                        data-tooltip-html="카드의 상세 정보와 통계를 확인하세요."
                      >
                        <Info size={18} />
                      </button>
                    </div>
                    {/* 수정/삭제 버튼 (작성자만 표시) */}
                    {isOwner && (
                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "6px",
                        }}
                      >
                        <button
                          type="button"
                          onClick={handleEdit}
                          style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "4px",
                            padding: "6px 10px",
                            background: "#6c63ff",
                            color: "#fff",
                            border: "none",
                            borderRadius: "6px",
                            cursor: "pointer",
                            fontSize: "12px",
                            fontWeight: "600",
                            transition: "background 0.2s",
                          }}
                          onMouseEnter={(e) => {
                            if (e.target instanceof HTMLButtonElement) {
                              e.target.style.background = "#5a52e5";
                            }
                          }}
                          onMouseLeave={(e) => {
                            if (e.target instanceof HTMLButtonElement) {
                              e.target.style.background = "#6c63ff";
                            }
                          }}
                          title="수정"
                        >
                          <Edit size={14} />
                        </button>
                        <button
                          type="button"
                          onClick={handleDelete}
                          style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "4px",
                            padding: "6px 10px",
                            background: "#fca5a5",
                            color: "#fff",
                            border: "none",
                            borderRadius: "6px",
                            cursor: "pointer",
                            fontSize: "12px",
                            fontWeight: "600",
                            transition: "background 0.2s",
                          }}
                          onMouseEnter={(e) => {
                            if (e.target instanceof HTMLButtonElement) {
                              e.target.style.background = "#f87171";
                            }
                          }}
                          onMouseLeave={(e) => {
                            if (e.target instanceof HTMLButtonElement) {
                              e.target.style.background = "#fca5a5";
                            }
                          }}
                          title="삭제"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    )}
                    <Tooltip
                      id="knowledge-card-back-info-tooltip"
                      place="bottom"
                      className="knowledge-info-tooltip"
                    />
                  </div>

                  {/* 뒷면 중간 컨텐츠 */}
                  <div className="back-middle-content">
                    {/* 통계 정보 */}
                    <div className="back-stats-section">
                      {card.viewCount !== undefined && (
                        <div className="back-stat-card stat-view">
                          <div className="back-stat-label">조회수</div>
                          <div className="back-stat-value">
                            {card.viewCount}
                          </div>
                        </div>
                      )}
                      {card.upvoteCount !== undefined && (
                        <div className="back-stat-card stat-upvote">
                          <div className="back-stat-label">좋아요</div>
                          <div className="back-stat-value">
                            {card.upvoteCount}
                          </div>
                        </div>
                      )}
                      {card.difficultyLevel && (
                        <div className="back-stat-card stat-difficulty">
                          <div className="back-stat-label">난이도</div>
                          <div className="back-stat-value">
                            LV.{card.difficultyLevel}
                          </div>
                        </div>
                      )}
                    </div>

                    {/* 작성자 정보 */}
                    <div className="author-info">
                      <h3
                        style={{
                          color: "#7d5cf6",
                          fontSize: "14px",
                          marginBottom: "10px",
                          display: "flex",
                          alignItems: "center",
                          gap: "6px",
                          fontWeight: 600,
                          letterSpacing: "-0.01em",
                        }}
                      >
                        <HiKey
                          size={16}
                          style={{
                            marginRight: "0px",
                            verticalAlign: "middle",
                          }}
                        />
                        작성자 정보
                      </h3>
                      {card.contributorNickname && (
                        <div className="info-row">
                          <span className="info-label">작성자</span>
                          <span className="info-value">
                            {card.contributorNickname}
                          </span>
                        </div>
                      )}
                      {card.createdAt && (
                        <div className="info-row">
                          <span className="info-label">작성일</span>
                          <span className="info-value">
                            {new Date(card.createdAt).toLocaleDateString(
                              "ko-KR",
                              {
                                year: "numeric",
                                month: "long",
                                day: "numeric",
                              }
                            )}
                          </span>
                        </div>
                      )}
                      {card.categories && (
                        <div className="info-row">
                          <span className="info-label">카테고리</span>
                          <span className="info-value">
                            {Array.isArray(card.categories)
                              ? card.categories.join(", ")
                              : card.categories}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* 출처 정보 */}
                    {card.sourceType && (
                      <div className="source-info">
                        <h3
                          style={{
                            color: "#7d5cf6",
                            fontSize: "14px",
                            marginBottom: "10px",
                            display: "flex",
                            alignItems: "center",
                            gap: "6px",
                            fontWeight: 600,
                            letterSpacing: "-0.01em",
                          }}
                        >
                          <HiLink
                            size={16}
                            style={{
                              marginRight: "0px",
                              verticalAlign: "middle",
                            }}
                          />
                          출처
                        </h3>
                        <div className="source-item">
                          {card.sourceType === "CHAT"
                            ? "챗봇"
                            : card.sourceType}
                        </div>
                      </div>
                    )}

                    {/* 학습 팁 */}
                    <div className="learning-tip">
                      <div className="learning-tip-header">
                        <Sparkles size={16} className="learning-tip-icon" />
                        <h3 className="learning-tip-title">학습 팁</h3>
                      </div>
                      <div className="learning-tip-content">
                        {card.difficultyLevel === 1 && (
                          <p>
                            기초 개념을 탄탄히 다지고, 예제를 통해 실습해보세요.
                          </p>
                        )}
                        {card.difficultyLevel === 2 && (
                          <p>
                            핵심 포인트를 중심으로 학습하고, 실제 프로젝트에
                            적용해보세요.
                          </p>
                        )}
                        {card.difficultyLevel === 3 && (
                          <p>
                            심화 내용을 이해하기 위해 관련 자료를 추가로
                            참고하세요.
                          </p>
                        )}
                        {!card.difficultyLevel && (
                          <p>정기적으로 복습하여 장기 기억으로 전환하세요.</p>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* 액션 버튼 */}
                  <div
                    className="card-actions-section"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <div className="card-actions">
                      <button
                        className={`card-action-btn card-like-btn ${
                          isLiked ? "liked" : ""
                        }`}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleLikeToggle(e);
                        }}
                        disabled={isLiking}
                        title="좋아요"
                      >
                        <Heart
                          size={18}
                          fill={isLiked ? "currentColor" : "none"}
                        />
                        <span>좋아요 {card?.upvoteCount || 0}</span>
                      </button>
                      <button
                        className="card-action-btn card-share-btn"
                        onClick={handleShare}
                        title="공유하기"
                      >
                        <Share2 size={18} />
                        <span>공유</span>
                      </button>
                    </div>
                    {/* 플립 안내 */}
                    <div className="flip-hint">💫 다시 클릭하여 앞면 보기</div>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}
    </>
  );

  // Portal을 사용하여 body에 직접 렌더링
  return (
    <>
      {typeof document !== "undefined"
        ? createPortal(modalContent, document.body)
        : null}
      {/* 로그인 모달 - 별도 Portal로 렌더링 */}
      {showLoginModal &&
        typeof document !== "undefined" &&
        createPortal(
          <LoginModal
            onClose={() => setShowLoginModal(false)}
            onSuccess={() => {
              // 로그인 성공 후 필요한 작업 수행
            }}
          />,
          document.body
        )}

      {/* 수정 모달 */}
      {showEditModal &&
        editedCardData &&
        typeof document !== "undefined" &&
        createPortal(
          <motion.div
            className="chat-knowledge-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => {
              setShowEditModal(false);
              setEditedCardData(null);
            }}
            style={{ zIndex: 1000002 }}
          >
            <motion.div
              className="chat-knowledge-modal-box"
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 20 }}
              transition={{ type: "spring", damping: 25 }}
              onClick={(e) => e.stopPropagation()}
            >
              <button
                className="chat-knowledge-modal-close"
                onClick={() => {
                  setShowEditModal(false);
                  setEditedCardData(null);
                }}
              >
                <X size={20} />
              </button>

              <h3>지식 카드 수정</h3>

              {editedCardData ? (
                <>
                  <div className="knowledge-card-preview">
                    {/* 제목 */}
                    <div className="form-group">
                      <label>제목</label>
                      <input
                        type="text"
                        className="card-edit-input"
                        value={editedCardData.title}
                        onChange={(e) =>
                          handleCardDataChange("title", e.target.value)
                        }
                        placeholder="제목을 입력하세요"
                      />
                    </div>

                    {/* 한 줄 정의 */}
                    <div className="form-group">
                      <label>한 줄 정의 *</label>
                      <textarea
                        className="card-edit-textarea"
                        value={editedCardData.oneLineDefinition}
                        onChange={(e) =>
                          handleCardDataChange(
                            "oneLineDefinition",
                            e.target.value
                          )
                        }
                        placeholder="한 줄 정의를 입력하세요"
                        rows={3}
                      />
                    </div>

                    {/* 핵심 포인트 */}
                    <div className="form-group">
                      <label>핵심 포인트</label>
                      {editedCardData.corePoints &&
                      editedCardData.corePoints.length > 0 ? (
                        <div className="array-edit-container">
                          {editedCardData.corePoints.map((point, index) => (
                            <div key={index} className="array-edit-item">
                              <input
                                type="text"
                                className="card-edit-input"
                                value={point}
                                onChange={(e) =>
                                  handleUpdateArrayItem(
                                    "corePoints",
                                    index,
                                    e.target.value
                                  )
                                }
                                placeholder={`핵심 포인트 ${index + 1}`}
                              />
                              <button
                                type="button"
                                className="array-remove-btn"
                                onClick={() =>
                                  handleRemoveArrayItem("corePoints", index)
                                }
                                title="삭제"
                              >
                                <X size={16} />
                              </button>
                            </div>
                          ))}
                        </div>
                      ) : null}
                      <button
                        type="button"
                        className="array-add-btn"
                        onClick={() => handleAddArrayItem("corePoints")}
                      >
                        + 핵심 포인트 추가
                      </button>
                    </div>

                    {/* 자주 틀리는 오해 */}
                    <div className="form-group">
                      <label>자주 틀리는 오해</label>
                      {editedCardData.commonMistakes &&
                      editedCardData.commonMistakes.length > 0 ? (
                        <div className="array-edit-container">
                          {editedCardData.commonMistakes.map(
                            (mistake, index) => (
                              <div key={index} className="array-edit-item">
                                <input
                                  type="text"
                                  className="card-edit-input"
                                  value={mistake}
                                  onChange={(e) =>
                                    handleUpdateArrayItem(
                                      "commonMistakes",
                                      index,
                                      e.target.value
                                    )
                                  }
                                  placeholder={`자주 틀리는 오해 ${index + 1}`}
                                />
                                <button
                                  type="button"
                                  className="array-remove-btn"
                                  onClick={() =>
                                    handleRemoveArrayItem(
                                      "commonMistakes",
                                      index
                                    )
                                  }
                                  title="삭제"
                                >
                                  <X size={16} />
                                </button>
                              </div>
                            )
                          )}
                        </div>
                      ) : null}
                      <button
                        type="button"
                        className="array-add-btn"
                        onClick={() => handleAddArrayItem("commonMistakes")}
                      >
                        + 자주 틀리는 오해 추가
                      </button>
                    </div>

                    {/* 공개/비공개 설정 */}
                    <div className="form-group">
                      <label>공개 설정</label>
                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "12px",
                        }}
                      >
                        <ToggleSwitch
                          checked={editedCardData.isPublished !== false}
                          onChange={(checked) =>
                            handleCardDataChange("isPublished", checked)
                          }
                        />
                        <span style={{ fontSize: "14px", color: "#666" }}>
                          {editedCardData.isPublished !== false
                            ? "공개"
                            : "비공개"}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div
                    style={{ display: "flex", gap: "10px", marginTop: "20px" }}
                  >
                    <button
                      className="chat-knowledge-modal-btn"
                      onClick={handleConfirmUpdate}
                      style={{
                        background: "#6c63ff",
                        flex: 1,
                      }}
                      disabled={isUpdating}
                    >
                      {isUpdating ? "수정 중..." : "수정하기"}
                    </button>
                    <button
                      className="chat-knowledge-modal-btn"
                      onClick={() => {
                        setShowEditModal(false);
                        setEditedCardData(null);
                      }}
                      style={{
                        background: "#6b7280",
                        flex: 1,
                      }}
                      disabled={isUpdating}
                    >
                      취소
                    </button>
                  </div>
                </>
              ) : (
                <div className="analyzing-container">
                  <p style={{ color: "#666" }}>데이터를 불러오는 중...</p>
                </div>
              )}
            </motion.div>
          </motion.div>,
          document.body
        )}

      {/* 공유 하단 시트 */}
      {showShareModal &&
        typeof document !== "undefined" &&
        createPortal(
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setShowShareModal(false)}
            style={{
              position: "fixed",
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: "rgba(0, 0, 0, 0.3)",
              zIndex: 1000003,
              display: "flex",
              alignItems: "flex-end",
              justifyContent: "center",
            }}
          >
            <motion.div
              initial={{ y: "100%" }}
              animate={{ y: 0 }}
              exit={{ y: "100%" }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              onClick={(e) => e.stopPropagation()}
              style={{
                background: "#fff",
                borderTopLeftRadius: "20px",
                borderTopRightRadius: "20px",
                padding: "24px 20px 32px",
                width: "100%",
                maxWidth: "600px",
                boxShadow: "0 -4px 20px rgba(0, 0, 0, 0.15)",
                position: "relative",
              }}
            >
              {/* 삼각형 포인터 */}
              <div
                style={{
                  position: "absolute",
                  top: "-8px",
                  left: "50%",
                  transform: "translateX(-50%)",
                  width: 0,
                  height: 0,
                  borderLeft: "8px solid transparent",
                  borderRight: "8px solid transparent",
                  borderBottom: "8px solid #fff",
                }}
              />

              {/* 공유 옵션 버튼들 */}
              <div
                style={{
                  display: "flex",
                  gap: "16px",
                  justifyContent: "center",
                  alignItems: "center",
                }}
              >
                <button
                  onClick={handleSaveImage}
                  style={{
                    width: "80px",
                    height: "80px",
                    borderRadius: "16px",
                    border: "1px solid #e5e7eb",
                    background: "#fff",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "6px",
                    cursor: "pointer",
                    transition: "all 0.2s",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.background = "#f3f4f6";
                    e.currentTarget.style.transform = "translateY(-2px)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.background = "#fff";
                    e.currentTarget.style.transform = "translateY(0)";
                  }}
                  title="이미지 저장"
                >
                  <ImageIcon size={28} color="#7d5cf6" />
                  <span
                    style={{
                      fontSize: "12px",
                      color: "#666",
                      fontWeight: "500",
                    }}
                  >
                    이미지 저장
                  </span>
                </button>

                <button
                  onClick={handleCopyToClipboard}
                  style={{
                    width: "80px",
                    height: "80px",
                    borderRadius: "16px",
                    border: "1px solid #e5e7eb",
                    background: isCopied ? "#10b981" : "#fff",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "6px",
                    cursor: "pointer",
                    transition: "all 0.2s",
                  }}
                  onMouseEnter={(e) => {
                    if (!isCopied) {
                      e.currentTarget.style.background = "#f3f4f6";
                      e.currentTarget.style.transform = "translateY(-2px)";
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!isCopied) {
                      e.currentTarget.style.background = "#fff";
                      e.currentTarget.style.transform = "translateY(0)";
                    }
                  }}
                  title="URL 복사"
                >
                  {isCopied ? (
                    <>
                      <Check size={28} color="#fff" />
                      <span
                        style={{
                          fontSize: "12px",
                          color: "#fff",
                          fontWeight: "500",
                        }}
                      >
                        복사됨
                      </span>
                    </>
                  ) : (
                    <>
                      <Link2 size={28} color="#7d5cf6" />
                      <span
                        style={{
                          fontSize: "12px",
                          color: "#666",
                          fontWeight: "500",
                        }}
                      >
                        URL 복사
                      </span>
                    </>
                  )}
                </button>
              </div>
            </motion.div>
          </motion.div>,
          document.body
        )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal &&
        typeof document !== "undefined" &&
        createPortal(
          <div
            style={{
              position: "fixed",
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: "rgba(0, 0, 0, 0.5)",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              zIndex: 1000002,
            }}
            onClick={() => setShowDeleteModal(false)}
          >
            <div
              style={{
                background: "#fff",
                borderRadius: "16px",
                padding: "24px",
                maxWidth: "400px",
                width: "90%",
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <h2
                style={{
                  marginBottom: "16px",
                  fontSize: "20px",
                  fontWeight: "600",
                }}
              >
                카드 삭제
              </h2>
              <p style={{ color: "#666", marginBottom: "24px" }}>
                정말로 이 카드를 삭제하시겠습니까?
                <br />
                <span style={{ fontSize: "14px", color: "#999" }}>
                  삭제된 카드는 복구할 수 없습니다.
                </span>
              </p>
              <div
                style={{
                  display: "flex",
                  gap: "10px",
                  justifyContent: "flex-end",
                }}
              >
                <button
                  onClick={() => setShowDeleteModal(false)}
                  style={{
                    padding: "10px 20px",
                    background: "#e5e7eb",
                    color: "#374151",
                    border: "none",
                    borderRadius: "8px",
                    cursor: "pointer",
                    fontWeight: "600",
                  }}
                >
                  취소
                </button>
                <button
                  onClick={handleConfirmDelete}
                  disabled={isDeleting}
                  style={{
                    padding: "10px 20px",
                    background: "#ef4444",
                    color: "#fff",
                    border: "none",
                    borderRadius: "8px",
                    cursor: isDeleting ? "not-allowed" : "pointer",
                    fontWeight: "600",
                    opacity: isDeleting ? 0.6 : 1,
                  }}
                >
                  {isDeleting ? "삭제 중..." : "삭제"}
                </button>
              </div>
            </div>
          </div>,
          document.body
        )}
    </>
  );
}
