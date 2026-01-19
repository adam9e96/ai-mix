import { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import { toast } from "react-toastify";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import LoginModal from "@/components/modal/LoginModal";
import { Tooltip } from "react-tooltip";
import {
  ArrowLeft,
  BookOpen,
  AlertCircle,
  MessageSquare,
  Eye,
  ThumbsUp,
  User,
  Calendar,
  Link2,
  Share2,
  Heart,
  Edit,
  Trash2,
  Copy,
  Check,
  X,
  Info,
  Sparkles,
  Download,
  Image as ImageIcon,
} from "lucide-react";
import { HiLightBulb, HiKey, HiXMark, HiLink } from "react-icons/hi2";
import "@styles/pages/knowledge-detail.css";

export default function KnowledgeDetail() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();
  const { showLoginModal, setShowLoginModal } = useUIStore();
  const [card, setCard] = useState(null);
  const [relatedCards, setRelatedCards] = useState([]);
  const [mistakes, setMistakes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isFlipped, setIsFlipped] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [isLiking, setIsLiking] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [isCopied, setIsCopied] = useState(false);
  const frontCardRef = useRef(null);
  const backCardRef = useRef(null);
  const cardContainerRef = useRef(null);

  useEffect(() => {
    const fetchCardDetail = async () => {
      setLoading(true);
      try {
        const response = await axiosInstance.get(`/knowledge/cards/${slug}`);

        // 새로운 API 응답 구조 처리
        const responseData = response.data;

        if (!responseData || !responseData.card) {
          console.error("유효하지 않은 응답 구조:", responseData);
          toast.error("카드 정보를 불러올 수 없습니다.");
          navigate("/knowledge");
          return;
        }

        const cardData = responseData.card;
        const relatedCardsData = responseData.relatedCards || [];
        const mistakesData = responseData.mistakes || [];

        setCard(cardData);
        setRelatedCards(relatedCardsData);
        setMistakes(mistakesData);

        // 초기 좋아요 상태 설정 (API에서 받아온 값 사용)
        // isLiked가 명시적으로 true일 때만 true로 설정
        const initialIsLiked = Boolean(cardData.isLiked);
        setIsLiked(initialIsLiked);
      } catch (error) {
        console.error("카드 상세 정보 불러오기 실패:", error);
        console.error("에러 상세:", error.response?.data);
        const errorMessage =
          error.response?.data?.message ||
          error.response?.data?.error ||
          "카드 정보를 불러오는 중 오류가 발생했습니다.";
        toast.error(errorMessage);
        navigate("/knowledge");
      } finally {
        setLoading(false);
      }
    };

    if (slug) {
      fetchCardDetail();
    }
  }, [slug, navigate]);

  // 카드 높이 동적 조정
  useEffect(() => {
    if (
      card &&
      frontCardRef.current &&
      backCardRef.current &&
      cardContainerRef.current
    ) {
      const adjustCardHeight = () => {
        const frontHeight = frontCardRef.current.scrollHeight;
        const backHeight = backCardRef.current.scrollHeight;
        const maxHeight = Math.max(frontHeight, backHeight);

        if (cardContainerRef.current) {
          cardContainerRef.current.style.height = `${maxHeight}px`;
        }
        if (frontCardRef.current) {
          frontCardRef.current.style.height = `${maxHeight}px`;
        }
        if (backCardRef.current) {
          backCardRef.current.style.height = `${maxHeight}px`;
        }
      };

      // 초기 조정
      adjustCardHeight();

      // 리사이즈 이벤트 리스너
      window.addEventListener("resize", adjustCardHeight);

      return () => {
        window.removeEventListener("resize", adjustCardHeight);
      };
    }
  }, [card, isFlipped]);

  // 카드 데이터가 변경될 때 좋아요 상태 동기화
  useEffect(() => {
    if (card) {
      // card.isLiked 값이 변경될 때마다 상태 동기화
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
      });
      return;
    }
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
      });
      return;
    }
    setShowDeleteModal(true);
  };

  // 삭제 확인 핸들러 (UI만, API는 나중에)
  const handleConfirmDelete = async () => {
    // TODO: 삭제 API 호출
    // try {
    //   await axiosInstance.delete(`/knowledge/cards/${card.id}`);
    //   toast.success("카드가 삭제되었습니다.");
    //   navigate("/knowledge");
    // } catch (error) {
    //   toast.error("카드 삭제에 실패했습니다.");
    // }
    console.log("삭제 API 호출 예정:", card?.id);
    setShowDeleteModal(false);
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
        });
        setTimeout(() => setIsCopied(false), 2000);
      } catch (err) {
        toast.error("복사에 실패했습니다.", {
          position: "top-right",
          autoClose: 2000,
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

      // 카드 상태 업데이트
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
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "좋아요 처리 중 오류가 발생했습니다.";
      toast.error(errorMessage);
    } finally {
      setIsLiking(false);
    }
  };

  if (loading) {
    return (
      <div className="knowledge-detail-wrapper">
        <div className="knowledge-detail-container">
          <div className="knowledge-detail-loading">불러오는 중...</div>
        </div>
      </div>
    );
  }

  if (!card) {
    console.log("card가 null입니다. loading:", loading);
    return (
      <div className="knowledge-detail-wrapper">
        <div className="knowledge-detail-container">
          <div className="knowledge-detail-error">
            카드 정보를 찾을 수 없습니다.
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="knowledge-detail-wrapper">
      <div className="knowledge-detail-container">
        {/* 헤더 영역 (뒤로가기 버튼만) */}
        <div
          style={{
            display: "flex",
            justifyContent: "flex-start",
            alignItems: "center",
            width: "100%",
            maxWidth: "450px",
            marginBottom: "20px",
          }}
        >
          <button
            className="knowledge-detail-back"
            onClick={() => navigate("/knowledge")}
          >
            <ArrowLeft size={18} />
            <span>목록으로</span>
          </button>
        </div>

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
                <div className="card-title">{card.title || "제목 없음"}</div>
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
                      style={{ marginRight: "6px", verticalAlign: "middle" }}
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
                  <Heart size={18} fill={isLiked ? "currentColor" : "none"} />
                  <span>좋아요 {card?.upvoteCount || 0}</span>
                </button>
                <button
                  className="card-action-btn card-share-btn"
                  onClick={handleShare}
                  title="공유하기"
                  style={{
                    background:
                      "linear-gradient(135deg, #f6f3ff 0%, #f2f0ff 100%)",
                    border: "1px solid rgba(125, 92, 246, 0.3)",
                    color: "#5f4fff",
                    opacity: 1,
                  }}
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
                      <div className="back-stat-value">{card.viewCount}</div>
                    </div>
                  )}
                  {card.upvoteCount !== undefined && (
                    <div className="back-stat-card stat-upvote">
                      <div className="back-stat-label">좋아요</div>
                      <div className="back-stat-value">{card.upvoteCount}</div>
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
                        {new Date(card.createdAt).toLocaleDateString("ko-KR", {
                          year: "numeric",
                          month: "long",
                          day: "numeric",
                        })}
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
                      {card.sourceType === "CHAT" ? "챗봇" : card.sourceType}
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
                        심화 내용을 이해하기 위해 관련 자료를 추가로 참고하세요.
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
                    <Heart size={18} fill={isLiked ? "currentColor" : "none"} />
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
      </div>

      {/* 로그인 모달 */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onSuccess={() => {
            // 로그인 성공 후 필요한 작업 수행
          }}
        />
      )}

      {/* 수정 모달 (UI만) */}
      {showEditModal && (
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
            zIndex: 10000,
          }}
          onClick={() => setShowEditModal(false)}
        >
          <div
            style={{
              background: "#fff",
              borderRadius: "16px",
              padding: "24px",
              maxWidth: "500px",
              width: "90%",
              maxHeight: "80vh",
              overflow: "auto",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2
              style={{
                marginBottom: "20px",
                fontSize: "20px",
                fontWeight: "600",
              }}
            >
              지식 카드 수정
            </h2>
            <p style={{ color: "#666", marginBottom: "20px" }}>
              수정 기능은 곧 제공될 예정입니다.
            </p>
            <div
              style={{
                display: "flex",
                gap: "10px",
                justifyContent: "flex-end",
              }}
            >
              <button
                onClick={() => setShowEditModal(false)}
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
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 공유 하단 시트 */}
      {showShareModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: "rgba(0, 0, 0, 0.3)",
            zIndex: 10001,
            display: "flex",
            alignItems: "flex-end",
            justifyContent: "center",
            animation: "fadeIn 0.2s ease-out",
          }}
          onClick={() => setShowShareModal(false)}
        >
          <div
            style={{
              background: "#fff",
              borderTopLeftRadius: "20px",
              borderTopRightRadius: "20px",
              padding: "24px 20px 32px",
              width: "100%",
              maxWidth: "600px",
              boxShadow: "0 -4px 20px rgba(0, 0, 0, 0.15)",
              position: "relative",
              animation: "slideUp 0.3s ease-out",
            }}
            onClick={(e) => e.stopPropagation()}
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
                  style={{ fontSize: "12px", color: "#666", fontWeight: "500" }}
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
          </div>

          <style>{`
            @keyframes slideUp {
              from {
                transform: translateY(100%);
              }
              to {
                transform: translateY(0);
              }
            }
            @keyframes fadeIn {
              from {
                opacity: 0;
              }
              to {
                opacity: 1;
              }
            }
          `}</style>
        </div>
      )}

      {/* 삭제 확인 모달 (UI만) */}
      {showDeleteModal && (
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
            zIndex: 10000,
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
                style={{
                  padding: "10px 20px",
                  background: "#ef4444",
                  color: "#fff",
                  border: "none",
                  borderRadius: "8px",
                  cursor: "pointer",
                  fontWeight: "600",
                }}
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
