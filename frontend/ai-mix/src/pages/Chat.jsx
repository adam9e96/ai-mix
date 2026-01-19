/*chat */

import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import "../styles/pages/chat.css";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeRaw from "rehype-raw";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import {
  coldarkDark,
  nord,
  oneDark,
  oneLight,
} from "react-syntax-highlighter/dist/esm/styles/prism";

import BattleButton from "../components/battle/BattleButton";
import BattleModal from "../components/battle/BattleModal";
import LoginModal from "../components/modal/LoginModal";
import LoadingDots from "../components/common/LoadingDots";
import ToggleSwitch from "../components/common/ToggleSwitch";

import {
  Ellipsis,
  X,
  ChevronLeft,
  ChevronRight,
  Copy,
  Check,
  BookOpen,
} from "lucide-react";
import { IoInformationCircleSharp } from "react-icons/io5";

import { useParams, useNavigate } from "react-router-dom";
import TypingText from "../components/chat/TypingText";
import { useBattleStore } from "../stores/battle.store";
import { useChatStore } from "../stores/chat.store";
import { useUIStore } from "../stores/ui.store";
import { useAuthStore } from "../stores/auth.store";
import axiosInstance from "../api/axiosInstance";
import { toast, ToastContainer } from "react-toastify";
import { Tooltip } from "react-tooltip";

export default function ChatPage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const { getBattleSummaries, toggleShowSummaries, isShowSummaries } =
    useBattleStore();

  // Auth Store
  const { user, loadUser, isAuthenticated } = useAuthStore();

  // Chat Store
  const {
    sessions,
    currentSession,
    messages,
    input,
    loading,
    newTitle,
    fetchSessions,
    loadMessages,
    createSession: createSessionAction,
    deleteSession,
    renameSession,
    onSend,
    setCurrentSession,
    setInput,
    setNewTitle,
  } = useChatStore();

  const isLogin = isAuthenticated();

  // UI Store
  const {
    showModal,
    modalType,
    showDeleteModal,
    showLoginModal,
    sidebarOpen,
    dropdownId,
    battleOpen,
    copiedCodeId,
    sessionToDelete,
    setShowModal,
    setModalType,
    setShowDeleteModal,
    setShowLoginModal,
    setSidebarOpen,
    setDropdownId,
    setBattleOpen,
    setCopiedCodeId,
    setSessionToDelete,
  } = useUIStore();

  const scrollRef = useRef(null);
  const didRun = useRef(false);
  const [isCreatingCard, setIsCreatingCard] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [showKnowledgeCardModal, setShowKnowledgeCardModal] = useState(false);
  const [cardData, setCardData] = useState(null);
  const [editedCardData, setEditedCardData] = useState(null);

  /* ================================
     로그인 체크 및 세션 목록 로드
  ================================= */
  useEffect(() => {
    if (didRun.current) return;
    didRun.current = true;

    const initChat = async () => {
      await loadUser();
      // 로그인되어 있으면 세션 목록 가져오기
      if (isAuthenticated()) {
        await fetchSessions();
      }
    };
    initChat();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /* ================================
     세션 메시지 로드
  ================================= */
  useEffect(() => {
    if (sessionId) {
      setCurrentSession(sessionId);
      loadMessages(sessionId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionId]);

  /* ================================
     새 세션 생성 / 제목 수정
  ================================= */
  const createSession = () => {
    if (!isLogin) {
      setShowLoginModal(true);
      return;
    }
    setModalType("create");
    setNewTitle("");
    setShowModal(true);
  };

  const renameSessionOpen = (id) => {
    setModalType("rename");
    setCurrentSession(id);
    setNewTitle(sessions.find((s) => s.id === id)?.title || "");
    setShowModal(true);
  };

  /* ================================
     세션 삭제
  ================================= */
  const openDeleteModal = (id) => {
    setSessionToDelete(id);
    setShowDeleteModal(true);
    setDropdownId(null);
  };

  const handleDeleteSession = async () => {
    if (!sessionToDelete) return;
    await deleteSession(sessionToDelete);
    setShowDeleteModal(false);
    setSessionToDelete(null);
  };

  const confirmModal = async () => {
    setShowModal(false);

    if (modalType === "create") {
      // 제목이 입력되었으면 새 세션 생성
      if (newTitle.trim()) {
        const newSessionId = await createSessionAction(newTitle);
        if (newSessionId) {
          // URL 업데이트
          window.history.replaceState(null, "", `/chat/${newSessionId}`);
        } else {
          // 실패 시 기존 동작 유지
          window.history.replaceState(null, "", `/chat`);
        }
      } else {
        // 제목이 없으면 기존 동작
        setCurrentSession(null);
        setNewTitle("");
        window.history.replaceState(null, "", `/chat`);
      }
      return;
    }

    if (modalType === "rename") {
      if (!newTitle.trim() || !currentSession) return;
      await renameSession(currentSession, newTitle);
      setNewTitle("");
    }
  };

  /* ================================
     GPT 전송 (onSend는 스토어에서 처리)
  ================================= */

  /* ================================
     마크다운 렌더링 컴포넌트
  ================================= */
  const renderMarkdown = (text, msgId) => {
    return (
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeRaw]}
        components={{
          code({ node, className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || "");
            const language = match ? match[1] : "";
            const codeString = String(children).replace(/\n$/, "");

            // 코드블록인 경우 (pre 안에 있는 경우)
            if (language) {
              const codeId = `code-${msgId}-${codeString
                .slice(0, 20)
                .replace(/\s/g, "")}`;

              const handleCopy = async () => {
                if (!codeString) return;
                try {
                  await navigator.clipboard.writeText(codeString);
                  setCopiedCodeId(codeId);
                  setTimeout(() => setCopiedCodeId(null), 2000);
                } catch (err) {
                  console.error("복사 실패:", err);
                  const textArea = document.createElement("textarea");
                  textArea.value = codeString;
                  document.body.appendChild(textArea);
                  textArea.select();
                  document.execCommand("copy");
                  document.body.removeChild(textArea);
                  setCopiedCodeId(codeId);
                  setTimeout(() => setCopiedCodeId(null), 2000);
                }
              };

              return (
                <div className="code-block-wrapper">
                  <SyntaxHighlighter
                    language={language}
                    style={oneDark}
                    PreTag="div"
                    customStyle={{
                      margin: 0,
                      padding: "18px 20px",
                      borderRadius: "10px",
                      fontSize: "14px",
                      lineHeight: "1.6",
                    }}
                  >
                    {codeString}
                  </SyntaxHighlighter>
                  {codeString && (
                    <button
                      className="code-copy-btn"
                      onClick={handleCopy}
                      title="코드 복사"
                    >
                      {copiedCodeId === codeId ? (
                        <Check size={16} />
                      ) : (
                        <Copy size={16} />
                      )}
                    </button>
                  )}
                </div>
              );
            }

            // 인라인 코드인 경우
            return (
              <code className={className} {...props}>
                {children}
              </code>
            );
          },
          pre({ node, children, ...props }) {
            return <>{children}</>;
          },
          ul({ node, children, ...props }) {
            return <ul {...props}>{children}</ul>;
          },
          ol({ node, children, ...props }) {
            return <ol {...props}>{children}</ol>;
          },
          li({ node, children, ...props }) {
            return <li {...props}>{children}</li>;
          },
        }}
      >
        {text}
      </ReactMarkdown>
    );
  };

  /* ================================
     자동 스크롤
  ================================= */
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, currentSession]);

  /* ================================
     드롭다운 외부 클릭
  ================================= */
  useEffect(() => {
    const handle = (e) => {
      if (
        !e.target.closest(".session-more") &&
        !e.target.closest(".session-dropdown")
      ) {
        setDropdownId(null);
      }
    };
    document.addEventListener("click", handle);
    return () => document.removeEventListener("click", handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /* ================================
     지식카드 생성 모달 열기 및 미리보기 생성
  ================================= */
  const handleOpenKnowledgeCardModal = async () => {
    if (!currentSession) {
      toast.error("세션이 없습니다.");
      return;
    }

    if (!isLogin) {
      setShowLoginModal(true);
      return;
    }

    setIsAnalyzing(true);
    setShowKnowledgeCardModal(true);
    setCardData(null);

    try {
      // 1단계: 미리보기 생성
      const preview = await axiosInstance.post(
        "/knowledge/cards/from-chat/preview",
        {
          sessionId: currentSession,
        }
      );

      // 2단계: 미리보기 모달 표시 및 편집 가능한 데이터 초기화
      setCardData(preview.data);
      setEditedCardData({
        title: preview.data.title || "",
        oneLineDefinition: preview.data.oneLineDefinition || "",
        corePoints: preview.data.corePoints || [],
        commonMistakes: preview.data.commonMistakes || [],
        isPublished: true, // 기본값: 공개
      });
    } catch (error) {
      console.error("대화 분석 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "대화 분석 중 오류가 발생했습니다.";
      toast.error(errorMessage, {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000000 },
      });
      setShowKnowledgeCardModal(false);
    } finally {
      setIsAnalyzing(false);
    }
  };

  /* ================================
     지식카드 저장 확인
  ================================= */
  const handleConfirmSaveKnowledgeCard = async () => {
    if (!editedCardData) {
      toast.error("분석된 데이터가 없습니다.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000000 },
      });
      return;
    }

    if (!currentSession) {
      toast.error("세션이 없습니다.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000000 },
      });
      return;
    }

    // 필수 필드 검증
    if (!editedCardData.title?.trim()) {
      toast.error("제목을 입력해주세요.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000000 },
      });
      return;
    }

    if (!editedCardData.oneLineDefinition?.trim()) {
      toast.error("한 줄 정의를 입력해주세요.", {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000000 },
      });
      return;
    }

    setIsCreatingCard(true);
    try {
      // 3단계: 사용자 확인 후 저장
      // QnA와 동일한 구조로 맞춤 (generateImage 필드 제거)
      // 빈 문자열 제거 및 데이터 정리
      const cleanedCorePoints = (editedCardData.corePoints || []).filter(
        (point) => point && point.trim()
      );
      const cleanedCommonMistakes = (
        editedCardData.commonMistakes || []
      ).filter((mistake) => mistake && mistake.trim());

      const requestData = {
        title: editedCardData.title.trim(),
        oneLineDefinition: editedCardData.oneLineDefinition.trim(),
        corePoints: cleanedCorePoints,
        commonMistakes: cleanedCommonMistakes,
        sourceType: "CHAT",
        sourceId: currentSession,
        isPublished: editedCardData.isPublished !== false, // 기본값 true, false일 때만 비공개
      };

      // 디버깅을 위한 요청 데이터 로그
      console.log(
        "지식카드 저장 요청 데이터:",
        JSON.stringify(requestData, null, 2)
      );
      console.log(
        "currentSession 타입 및 값:",
        typeof currentSession,
        currentSession
      );

      const response = await axiosInstance.post(
        "/knowledge/cards/from-chat",
        requestData
      );

      // 토스트 알림 표시 (지식카드 목록으로 이동 버튼 포함)
      toast.success(
        ({ closeToast }) => (
          <div
            style={{ display: "flex", flexDirection: "column", gap: "10px" }}
          >
            <div
              style={{ fontSize: "14px", fontWeight: "500", color: "#1f2937" }}
            >
              지식 카드가 생성되었습니다.
            </div>
            <button
              onClick={() => {
                navigate("/knowledge");
                closeToast();
              }}
              style={{
                padding: "8px 16px",
                background: "#5a52e5",
                color: "#fff",
                border: "none",
                borderRadius: "6px",
                cursor: "pointer",
                fontSize: "13px",
                fontWeight: "600",
                alignSelf: "flex-start",
                transition: "background 0.2s",
                boxShadow: "0 2px 8px rgba(90, 82, 229, 0.3)",
              }}
              onMouseEnter={(e) => {
                if (e.target instanceof HTMLButtonElement) {
                  e.target.style.background = "#4a42d5";
                }
              }}
              onMouseLeave={(e) => {
                if (e.target instanceof HTMLButtonElement) {
                  e.target.style.background = "#5a52e5";
                }
              }}
            >
              지식카드 목록으로 이동
            </button>
          </div>
        ),
        {
          position: "top-right",
          autoClose: 5000,
          style: { zIndex: 1000000 },
        }
      );
      setShowKnowledgeCardModal(false);
      setCardData(null);
      setEditedCardData(null);

      // 이미지 생성 완료 확인을 위한 폴링
      // 응답 구조 확인: id, cardId, card.id, slug 등 다양한 형태 가능
      const cardIdentifier =
        response.data?.slug ||
        response.data?.id ||
        response.data?.cardId ||
        response.data?.card?.slug ||
        response.data?.card?.id;

      if (cardIdentifier) {
        // 이미지 생성이 완료될 때까지 폴링 (최대 30초)
        let attempts = 0;
        const maxAttempts = 15; // 15번 시도 (2초 간격으로 30초)
        let pollInterval = null;
        let shouldStopPolling = false;

        const checkImageGeneration = async () => {
          // 폴링 중단 플래그 확인
          if (shouldStopPolling) {
            return true;
          }

          try {
            const cardResponse = await axiosInstance.get(
              `/knowledge/cards/${cardIdentifier}`
            );
            const card = cardResponse.data?.card || cardResponse.data;

            // 이미지가 생성되었는지 확인
            if (card?.imageUrl || card?.image) {
              // 이미지 생성 완료 알림 표시
              toast.info("이미지 생성된 카드", {
                position: "top-right",
                autoClose: 3000,
                hideProgressBar: false,
                pauseOnHover: true,
              });
              shouldStopPolling = true;
              if (pollInterval) {
                clearInterval(pollInterval);
              }
              return true;
            }
            return false;
          } catch (error) {
            // 404 에러가 발생하면 카드를 찾을 수 없으므로 폴링 중단
            if (error.response?.status === 404) {
              console.warn("카드를 찾을 수 없습니다. 폴링을 중단합니다.");
              shouldStopPolling = true;
              if (pollInterval) {
                clearInterval(pollInterval);
              }
              return true; // 중단을 의미
            }
            // 다른 에러는 조용히 처리 (로그만 출력하지 않음)
            return false;
          }
        };

        // 첫 번째 시도는 즉시 실행
        const firstCheck = async () => {
          const isComplete = await checkImageGeneration();
          if (isComplete || shouldStopPolling) return;

          // 첫 번째 시도 후 2초마다 확인
          pollInterval = setInterval(async () => {
            attempts++;
            const isComplete = await checkImageGeneration();

            if (isComplete || shouldStopPolling || attempts >= maxAttempts) {
              if (pollInterval) {
                clearInterval(pollInterval);
              }
            }
          }, 2000);
        };

        firstCheck();
      }

      // 성공 시 지식백과 페이지로 이동할 수도 있습니다
      // navigate("/knowledge");
    } catch (error) {
      console.error("지식카드 저장 실패:", error);
      console.error("에러 응답 상세:", {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message,
      });
      // 중복 감지를 위한 전체 에러 데이터 로깅
      console.log(
        "전체 에러 데이터 (중복 감지용):",
        JSON.stringify(error.response?.data, null, 2)
      );

      // 서버에서 반환한 상세 에러 메시지 추출
      const errorData = error.response?.data;
      const status = error.response?.status;
      let errorMessage = "지식카드 저장 중 오류가 발생했습니다.";

      if (errorData) {
        // 다양한 형태의 에러 메시지 처리
        const rawMessage =
          errorData.message ||
          errorData.error ||
          errorData.msg ||
          errorData.detail ||
          (typeof errorData === "string" ? errorData : "");

        // 에러 응답 전체를 문자열로 변환하여 중복 키워드 검색
        const errorDataString = JSON.stringify(errorData).toLowerCase();
        const rawMessageLower = rawMessage.toLowerCase();

        // 중복 관련 키워드 확인 (백엔드 에러 메시지 기반)
        const duplicateKeywords = [
          "이미 지식 카드가 생성되었습니다",
          "이미 생성된",
          "이미 생성",
          "이미 존재",
          "중복",
          "duplicate",
          "already exists",
          "already created",
          "해당 uuid",
          "해당 uuid로 이미",
        ];

        // 중복 키워드가 있는지 확인
        const isDuplicate =
          status === 409 ||
          status === 500 ||
          duplicateKeywords.some(
            (keyword) =>
              rawMessageLower.includes(keyword.toLowerCase()) ||
              errorDataString.includes(keyword.toLowerCase())
          );

        if (isDuplicate) {
          errorMessage = "중복된 카드";
        } else {
          errorMessage = rawMessage || errorMessage;
        }
      } else if (status === 500 || status === 409) {
        // 에러 데이터가 없어도 500이나 409면 중복으로 간주
        errorMessage = "중복된 카드";
      }

      toast.error(errorMessage, {
        position: "top-right",
        autoClose: 3000,
        style: { zIndex: 1000000 },
      });
    } finally {
      setIsCreatingCard(false);
    }
  };

  /* ================================
     카드 데이터 수정 핸들러
  ================================= */
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

  /* ================================
     렌더링
  ================================= */
  return (
    <>
      {battleOpen && (
        <BattleModal id={currentSession} onClose={() => setBattleOpen(false)} />
      )}

      {/* 로그인 모달 */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onSuccess={() => {
            // 로그인 성공 후 필요한 작업 수행
            // 예: 세션 목록 새로고침 등
            if (isAuthenticated()) {
              fetchSessions();
            }
          }}
        />
      )}

      {/* 삭제 확인 모달 */}
      <AnimatePresence>
        {showDeleteModal && (
          <motion.div
            className="chat-delete-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => {
              setShowDeleteModal(false);
              setSessionToDelete(null);
            }}
          >
            <motion.div
              className="chat-delete-modal-box"
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 20 }}
              transition={{ type: "spring", damping: 25 }}
              onClick={(e) => e.stopPropagation()}
            >
              <button
                className="chat-delete-modal-close"
                onClick={() => {
                  setShowDeleteModal(false);
                  setSessionToDelete(null);
                }}
              >
                <X size={20} />
              </button>

              <h3>채팅방 삭제</h3>

              <div style={{ marginBottom: "20px" }}>
                <p style={{ marginBottom: "12px", color: "#666" }}>
                  정말로 이 채팅방을 삭제하시겠습니까?
                </p>
                <div
                  style={{
                    background:
                      "linear-gradient(135deg, #f0f2ff 0%, #e8ebff 100%)",
                    padding: "14px 16px",
                    borderRadius: "12px",
                    border: "1px solid rgba(108, 99, 255, 0.15)",
                    boxShadow: "0 2px 8px rgba(108, 99, 255, 0.1)",
                  }}
                >
                  <div
                    style={{
                      fontSize: "0.85rem",
                      color: "#666",
                      marginBottom: "6px",
                    }}
                  >
                    삭제할 채팅방
                  </div>
                  <div
                    style={{
                      fontSize: "16px",
                      fontWeight: 600,
                      color: "#6c63ff",
                    }}
                  >
                    {sessions.find((s) => s.id === sessionToDelete)?.title ||
                      "채팅방"}
                  </div>
                </div>
                <p
                  style={{
                    marginTop: "12px",
                    fontSize: "0.85rem",
                    color: "#999",
                  }}
                >
                  삭제된 채팅은 복구할 수 없습니다.
                </p>
              </div>

              <div style={{ display: "flex", gap: "10px" }}>
                <button
                  className="chat-delete-modal-btn"
                  onClick={handleDeleteSession}
                  style={{
                    background: "#f97373",
                    flex: 1,
                  }}
                >
                  삭제
                </button>
                <button
                  className="chat-delete-modal-btn"
                  onClick={() => {
                    setShowDeleteModal(false);
                    setSessionToDelete(null);
                  }}
                  style={{
                    background: "#6b7280",
                    flex: 1,
                  }}
                >
                  취소
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 세션 생성/수정 모달 */}
      {showModal && (
        <div className="chat-session-modal-backdrop">
          <div className="chat-session-modal-box">
            <button
              className="chat-session-modal-close"
              onClick={() => setShowModal(false)}
            >
              <X size={20} />
            </button>

            <h3>{modalType === "create" ? "새 대화 제목" : "제목 수정"}</h3>

            <input
              className="chat-session-modal-input"
              placeholder="자동 생성됨"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  confirmModal();
                }
              }}
              autoFocus
            />

            <button className="chat-session-modal-btn" onClick={confirmModal}>
              확인
            </button>
          </div>
        </div>
      )}

      {/* 지식카드 생성 모달 */}
      <AnimatePresence>
        {showKnowledgeCardModal && (
          <motion.div
            className="chat-knowledge-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => {
              setShowKnowledgeCardModal(false);
              setIsCreatingCard(false);
              setIsAnalyzing(false);
              setCardData(null);
              setEditedCardData(null);
            }}
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
                  setShowKnowledgeCardModal(false);
                  setIsCreatingCard(false);
                  setIsAnalyzing(false);
                  setCardData(null);
                  setEditedCardData(null);
                }}
              >
                <X size={20} />
              </button>

              <h3>지식카드 생성</h3>

              {isAnalyzing ? (
                <div className="analyzing-container">
                  <LoadingDots />
                  <p style={{ marginTop: "20px", color: "#666" }}>
                    챗봇 대화를 분석 중입니다...
                  </p>
                </div>
              ) : editedCardData ? (
                <>
                  <div className="knowledge-card-preview">
                    {/* 제목 */}
                    <div className="form-group">
                      <label>제목 *</label>
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

                    {/* 흔한 실수 */}
                    <div className="form-group">
                      <label>흔한 실수</label>
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
                                  placeholder={`흔한 실수 ${index + 1}`}
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
                        + 흔한 실수 추가
                      </button>
                    </div>

                    {/* 공개/비공개 설정 */}
                    <div className="form-group">
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          padding: "12px 0",
                          borderTop: "1px solid #e5e7eb",
                          marginTop: "8px",
                        }}
                      >
                        <div>
                          <label
                            style={{
                              display: "block",
                              marginBottom: "4px",
                              fontWeight: 600,
                              color: "#374151",
                            }}
                          >
                            공개 설정
                          </label>
                          <span
                            style={{
                              fontSize: "0.875rem",
                              color: "#6b7280",
                            }}
                          >
                            {editedCardData.isPublished !== false
                              ? "모든 사용자가 볼 수 있습니다"
                              : "나만 볼 수 있습니다"}
                          </span>
                        </div>
                        <ToggleSwitch
                          checked={editedCardData.isPublished !== false}
                          onChange={(checked) => {
                            handleCardDataChange("isPublished", checked);
                          }}
                        />
                      </div>
                    </div>
                  </div>

                  <div
                    style={{ display: "flex", gap: "10px", marginTop: "20px" }}
                  >
                    <button
                      className="chat-knowledge-modal-btn"
                      onClick={handleConfirmSaveKnowledgeCard}
                      style={{
                        background: "#6c63ff",
                        flex: 1,
                      }}
                      disabled={isCreatingCard}
                    >
                      {isCreatingCard ? "저장 중..." : "저장하기"}
                    </button>
                    <button
                      className="chat-knowledge-modal-btn"
                      onClick={() => {
                        setShowKnowledgeCardModal(false);
                        setIsCreatingCard(false);
                        setCardData(null);
                        setEditedCardData(null);
                      }}
                      style={{
                        background: "#6b7280",
                        flex: 1,
                      }}
                      disabled={isCreatingCard}
                    >
                      취소
                    </button>
                  </div>
                </>
              ) : (
                <div className="analyzing-container">
                  <p style={{ color: "#666" }}>분석 결과를 불러오는 중...</p>
                </div>
              )}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="chat-layout">
        {/* 사이드바 */}
        <motion.aside
          className={`chat-sidebar ${sidebarOpen ? "open" : "closed"}`}
          initial={false}
          animate={{
            x: sidebarOpen ? 0 : -280,
            opacity: sidebarOpen ? 1 : 0,
          }}
          transition={{ type: "spring", stiffness: 300, damping: 30 }}
        >
          <div className="sidebar-header">
            <div className="sidebar-title-group">
              <h2>대화</h2>
              {isLogin && (
                <button className="sidebar-add" onClick={createSession}>
                  ＋
                </button>
              )}
            </div>

            <button
              className="sidebar-collapse-btn"
              onClick={() => setSidebarOpen(false)}
            >
              <ChevronLeft size={18} />
            </button>
          </div>

          <motion.div
            className="session-list"
            initial="hidden"
            animate="visible"
            variants={{
              hidden: { opacity: 0 },
              visible: {
                opacity: 1,
                transition: {
                  staggerChildren: 0.05,
                },
              },
            }}
          >
            {sessions.map((s) => (
              <motion.div
                key={s.id}
                className={`session-item ${
                  currentSession === s.id ? "active" : ""
                }`}
                variants={{
                  hidden: { opacity: 0, y: 10 },
                  visible: { opacity: 1, y: 0 },
                }}
                onClick={() => {
                  // 드롭다운이 열려있으면 클릭 무시
                  if (dropdownId === s.id) return;
                  setCurrentSession(s.id);
                  loadMessages(s.id);
                  window.history.replaceState(null, "", `/chat/${s.id}`);
                }}
              >
                <span>{s.title}</span>

                <div className="more-wrapper">
                  <button
                    className="session-more"
                    onClick={(e) => {
                      e.stopPropagation();
                      setDropdownId(dropdownId === s.id ? null : s.id);
                    }}
                  >
                    <Ellipsis size={18} />
                  </button>

                  {dropdownId === s.id && (
                    <div
                      className="session-dropdown"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <div onClick={() => renameSessionOpen(s.id)}>
                        제목 수정
                      </div>
                      <div
                        className="delete-item"
                        onClick={() => openDeleteModal(s.id)}
                      >
                        삭제
                      </div>
                    </div>
                  )}
                </div>
              </motion.div>
            ))}
          </motion.div>
        </motion.aside>

        {!sidebarOpen && (
          <button
            className="sidebar-open-btn"
            onClick={() => setSidebarOpen(true)}
          >
            <ChevronRight size={20} />
          </button>
        )}

        {/* 채팅 영역 */}
        <div className={`chat-area ${sidebarOpen ? "" : "expanded"}`}>
          <div className="chat-header">
            <div className="chat-header-top">
              <div className="chat-title-wrapper">
                <h1>
                  {currentSession
                    ? sessions.find((s) => s.id === currentSession)?.title ||
                      "AI-MIX 챗봇"
                    : newTitle.trim()
                    ? newTitle
                    : "AI-MIX 챗봇"}
                </h1>
                <button
                  className="chat-info-icon"
                  data-tooltip-id="chat-info-tooltip"
                  data-tooltip-html="챗봇과 대화하며 궁금한 내용을 질문하세요. 대화 내용은 자동으로 저장되며, 나중에 배틀 문제로 변환하거나 지식카드로 만들 수 있습니다."
                >
                  <IoInformationCircleSharp size={20} />
                </button>
                <Tooltip
                  id="chat-info-tooltip"
                  place="bottom"
                  className="chat-info-tooltip"
                />
              </div>
              {currentSession &&
                (messages[currentSession] || []).length > 0 &&
                isLogin && (
                  <button
                    className="knowledge-card-btn"
                    onClick={handleOpenKnowledgeCardModal}
                    disabled={isCreatingCard}
                    title="이 대화를 지식카드로 저장"
                  >
                    <BookOpen size={18} />
                    지식카드 저장
                  </button>
                )}
            </div>
            <p>무엇이든 물어보세요</p>
          </div>

          <div className="chat-messages" ref={scrollRef}>
            {(() => {
              const currentMessages = messages[currentSession ?? "temp"] || [];
              const hasMessages = currentMessages.length > 0;

              if (!currentSession && !hasMessages) {
                /* ==========================
     세션이 없을 때 환영 화면
  ========================== */
                return (
                  <div className="chat-welcome">
                    <div className="welcome-content">
                      <div className="welcome-icon">
                        <svg
                          width="80"
                          height="80"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="1.5"
                        >
                          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                        </svg>
                      </div>
                      <h2 className="welcome-title">
                        새로운 대화를 시작해보세요
                      </h2>
                      <p className="welcome-description">
                        AI-MIX 챗봇과 함께 궁금한 것을 물어보고
                        <br />
                        다양한 주제에 대해 대화해보세요
                      </p>
                      <button
                        className="welcome-start-btn"
                        onClick={createSession}
                      >
                        새 대화 시작하기
                      </button>
                    </div>
                  </div>
                );
              }

              if (currentSession && !hasMessages) {
                /* ==========================
     세션은 있지만 메시지가 없을 때
  ========================== */
                return (
                  <div className="chat-empty">
                    <div className="empty-content">
                      <div className="empty-icon">💬</div>
                      <p className="empty-text">
                        메시지를 입력하여 대화를 시작하세요
                      </p>
                    </div>
                  </div>
                );
              }

              /* ==========================
     메시지가 있을 때
  ========================== */
              const battleSummaries = getBattleSummaries(currentSession);
              const showSummaries = isShowSummaries(currentSession);

              return (
                <>
                  {currentMessages.map((msg, index) => {
                    return (
                      <motion.div
                        key={msg.id}
                        className={`msg ${msg.role}`}
                        initial={{ opacity: 0, y: 10, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        transition={{ duration: 0.3, delay: index * 0.05 }}
                      >
                        <div className="bubble">
                          {msg.typing ? (
                            /* ==========================
     타이핑 중 → 점 애니메이션
  ========================== */
                            <LoadingDots />
                          ) : (
                            /* ==========================
     일반 메시지 (기존 메시지 + 최신 메시지)
     → Markdown + 코드블록 스타일
  ========================== */
                            renderMarkdown(msg.text, msg.id)
                          )}
                        </div>
                      </motion.div>
                    );
                  })}

                  {/* 배틀 전적 표시 (메시지 아래) */}
                  {battleSummaries.length > 0 && (
                    <div className="battle-summaries-container">
                      <button
                        className="battle-summaries-toggle"
                        onClick={() => toggleShowSummaries(currentSession)}
                      >
                        ⚔ 배틀 전적 {showSummaries ? "▼" : "▶"} (
                        {battleSummaries.length})
                      </button>
                      {showSummaries && (
                        <div className="battle-summaries-list">
                          {battleSummaries.map((summary, idx) => (
                            <div
                              key={
                                summary.battleId ||
                                `${currentSession}-summary-${idx}`
                              }
                              className="battle-summary-item"
                            >
                              <span
                                className={`battle-result-badge ${summary.result?.toLowerCase()}`}
                              >
                                {summary.result === "WIN" && "🏆"}
                                {summary.result === "LOSE" && "💀"}
                                {summary.result === "DRAW" && "🤝"}
                              </span>
                              <span className="battle-summary-text">
                                정답률 {summary.correctRate?.toFixed(1)}% | 평균{" "}
                                {summary.averageScore?.toFixed(1)}점
                              </span>
                              {summary.battleId && (
                                <button
                                  className="battle-detail-btn"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    navigate(
                                      `/mypage/battles/${summary.battleId}`
                                    );
                                  }}
                                >
                                  상세보기
                                </button>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </>
              );
            })()}
          </div>

          <div className="chat-input">
            <input
              value={input}
              placeholder="메시지 입력..."
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  if (!isLogin) {
                    setShowLoginModal(true);
                    return;
                  }
                  onSend(isLogin);
                }
              }}
            />
            <button
              onClick={() => {
                if (!isLogin) {
                  setShowLoginModal(true);
                  return;
                }
                onSend(isLogin);
              }}
            >
              전송
            </button>
          </div>
        </div>

        {/* 배틀하기 버튼 (하단 고정) */}
        {currentSession &&
          (messages[currentSession] || []).length > 0 &&
          isLogin && (
            <div className="battle-btn-wrapper">
              <BattleButton
                onClick={() => {
                  if (!isLogin) {
                    setShowLoginModal(true);
                    return;
                  }
                  setBattleOpen(true);
                }}
              />
            </div>
          )}
      </div>
    </>
  );
}
